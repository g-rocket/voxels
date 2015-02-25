package voxels.map;

import static voxels.util.StaticUtils.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import voxels.block.*;
import voxels.generate.*;

import com.jme3.material.*;
import com.jme3.scene.*;

import de.schlichtherle.truezip.nio.file.*;

/*
 * Deals with the overarching structure (knows about ALL the chunks)
 */
public class WorldMap {
	public final Coord3 chunkSize;
	private HashMap<Coord3, Chunk> map = new HashMap<Coord3, Chunk>();
	private final Node worldNode;
	public final Material blockMaterial;
	public final TerrainGenerator terrainGenerator;
	private final TPath savePath;
	public final ExecutorService exec = Executors.newWorkStealingPool();
	public final GeneratorExecutor generatorExec = new GeneratorExecutor(Runtime.getRuntime().availableProcessors());
	public final Executor renderExec;
	public volatile boolean chunksShouldUnload = true;
	public final Supplier<List<Coord3>> playersLocations;
	public final Coord3 unloadDistance;
	public final Coord3 loadDistance;
	
	public class GeneratorExecutor {
		private Map<Coord3, ChunkGenerationProcess> queuedProcesses = new HashMap<>();
		private Set<GeneratorExecutorThread> threads = new HashSet<>();
		private int numThreadsWaiting = 0;
		private boolean stopping = false;
		
		public class ChunkGenerationProcess {
			public final Runnable process;
			public final IntSupplier priority;
			
			public ChunkGenerationProcess(Runnable process, IntSupplier priority) {
				this.process = process;
				this.priority = priority;
			}
		}
		
		private class GeneratorExecutorThread extends Thread {
			@Override
			public void run() {
				while(true) {
					Runnable process = null;
					synchronized (GeneratorExecutor.this) {
						if(queuedProcesses.isEmpty()) {
							numThreadsWaiting++;
							try {
								GeneratorExecutor.this.wait();
							} catch (InterruptedException e) {
								synchronized (threads) {
									threads.remove(this);
									if(threads.isEmpty()) threads.notifyAll();
								}
								return;
							}
							numThreadsWaiting--;
						} else {
							int maxPriority = Integer.MIN_VALUE;
							for(ChunkGenerationProcess cgp: queuedProcesses.values()) {
								int cgpPriority = cgp.priority.getAsInt();
								if(cgpPriority > maxPriority) {
									maxPriority = cgpPriority;
									process = cgp.process;
								}
							}
						}
					}
					if(process != null) process.run();
					if(stopping) {
						synchronized (threads) {
							threads.remove(this);
							if(threads.isEmpty()) threads.notifyAll();
						}
						return;
					}
				}
			}
		}
		
		private GeneratorExecutor(int maxConcurrentProcesses) {
			for(int i = 0; i < maxConcurrentProcesses; i++) {
				GeneratorExecutorThread t = new GeneratorExecutorThread();
				threads.add(t);
				t.start();
			}
		}
		
		public boolean addProcess(Coord3 chunkPos, Runnable generationProcess, IntSupplier priority) {
			boolean wasEmpty = queuedProcesses.put(chunkPos, new ChunkGenerationProcess(generationProcess, priority)) == null;
			if(numThreadsWaiting > 0) {
				synchronized (this) {
					this.notify();
				}
			}
			return wasEmpty;
		}
		
		public void stop(boolean blockUntilStopped) {
			stopping = true;
			for(GeneratorExecutorThread t: threads) {
				t.interrupt();
			}
			if(blockUntilStopped) {
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public WorldMap(Node worldNode, Material blockMaterial, File saveFile, Executor renderThreadExecutor, Supplier<List<Coord3>> playersLocations) {
		this.savePath = new TPath(saveFile);
		try {
			FileSystems.newFileSystem(savePath, this.getClass().getClassLoader());
			//if(Files.notExists(savePath.getParent())) Files.createDirectories(savePath.getParent());
			if(Files.notExists(savePath)) Files.createDirectories(savePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(Files.isReadable(savePath.resolve("world"))) {
			System.out.println();
			byte[] props = null;
			try {
				props = Files.readAllBytes(savePath.resolve("world"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			int i = 0;
			chunkSize = new Coord3(
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24),
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24),
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24));
			unloadDistance = new Coord3(
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24),
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24),
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24));
			loadDistance = new Coord3(
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24),
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24),
					props[i++] | (props[i++]<<8) | (props[i++]<<16) | (props[i++]<<24));
			terrainGenerator = new TerrainGenerator(
					props[i++] | (props[i++]<<8l) | (props[i++]<<16l) | (props[i++]<<24l) |
					(props[i++]<<32l) | (props[i++]<<40l) | (props[i++]<<48l) | (props[i++]<<56l));
		} else {
			chunkSize = new Coord3(32,32,16);
			unloadDistance = new Coord3(3,3,2);
			loadDistance = new Coord3(2,2,1);
			terrainGenerator = new TerrainGenerator();
		}
		this.worldNode = worldNode;
		this.blockMaterial = blockMaterial;
		this.renderExec = renderThreadExecutor;
		this.playersLocations = playersLocations;
		
		/*new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					synchronized (chunksToLoad) {
						if(chunksToLoad.isEmpty()) {
							try {
								chunksToLoad.wait();
							} catch (InterruptedException e) {
								System.err.println("Interrupted while waiting for chunks that need loading");
							}
						}
						if(chunksToLoad.isEmpty()) continue;
						Coord3 chunkToLoad = chunksToLoad.pop();
						if(isLoaded(chunkToLoad)) {
							getChunk(chunkToLoad);
						}
						exec.submit(new Runnable() {
							@Override
							public void run() {
								getChunk(chunksToLoad.pop());
							}
						});
					}
				}
			}
		});//.start();*/
	}
	
	public BlockType getBlock(Coord3 blockPos) {
		return getChunk(chunkPos(blockPos)).blocks.get(blockPos);
	}
	
	private Coord3 chunkPos(Coord3 blockPos) {
		return new Coord3(
				floorDiv(blockPos.x, chunkSize.x), 
				floorDiv(blockPos.x, chunkSize.x), 
				floorDiv(blockPos.x, chunkSize.x));
	}
	
	
	/**
	 * Get a chunk, loading or generating it if necessary
	 * Will block until the chunk is loaded
	 * @param chunkPos where to get the chunk from (in chunk coordinates)
	 * @return the chunk
	 */
	public Chunk getChunk(Coord3 chunkPos) {
		Chunk c = map.get(chunkPos);
		if(c != null) {
			//System.out.println("already loaded chunk at "+chunkPos);
			c.setNeeded();
			return c;
		} else {
			try {
				TPath chunkSave = savePath.resolve(chunkPos.toString());
				if(Files.isReadable(chunkSave)) {
					//System.out.println("loading chunk at "+chunkPos);
					return readChunk(chunkPos, chunkSave);
				} else {
					//System.out.println("generating chunk at "+chunkPos);
					return generateChunk(chunkPos);
				}
			} catch (IOException e) {
				System.err.println("error loading chunk at "+chunkPos);
				e.printStackTrace();
				return generateChunk(chunkPos);
			}
			
		}
	}
	
	public void unloadWorld() {
		exec.shutdownNow();
		chunksShouldUnload = false;
		for(Iterator<Map.Entry<Coord3, Chunk>> chunkI = map.entrySet().iterator(); chunkI.hasNext();) {
			unloadChunk(chunkI.next().getValue());
			chunkI.remove();
		}
		byte[] props = new byte[88];
		int i = 0;
		props[i++] = (byte)((0xff << 0) & chunkSize.x);
		props[i++] = (byte)((0xff << 8) & chunkSize.x);
		props[i++] = (byte)((0xff << 16) & chunkSize.x);
		props[i++] = (byte)((0xff << 24) & chunkSize.x);
		
		props[i++] = (byte)((0xff << 0) & chunkSize.y);
		props[i++] = (byte)((0xff << 8) & chunkSize.y);
		props[i++] = (byte)((0xff << 16) & chunkSize.y);
		props[i++] = (byte)((0xff << 24) & chunkSize.y);
		
		props[i++] = (byte)((0xff << 0) & chunkSize.z);
		props[i++] = (byte)((0xff << 8) & chunkSize.z);
		props[i++] = (byte)((0xff << 16) & chunkSize.z);
		props[i++] = (byte)((0xff << 24) & chunkSize.z);
		
		props[i++] = (byte)((0xff << 0) & unloadDistance.x);
		props[i++] = (byte)((0xff << 8) & unloadDistance.x);
		props[i++] = (byte)((0xff << 16) & unloadDistance.x);
		props[i++] = (byte)((0xff << 24) & unloadDistance.x);
		
		props[i++] = (byte)((0xff << 0) & unloadDistance.y);
		props[i++] = (byte)((0xff << 8) & unloadDistance.y);
		props[i++] = (byte)((0xff << 16) & unloadDistance.y);
		props[i++] = (byte)((0xff << 24) & unloadDistance.y);
		
		props[i++] = (byte)((0xff << 0) & unloadDistance.z);
		props[i++] = (byte)((0xff << 8) & unloadDistance.z);
		props[i++] = (byte)((0xff << 16) & unloadDistance.z);
		props[i++] = (byte)((0xff << 24) & unloadDistance.z);
		
		props[i++] = (byte)((0xff << 0) & loadDistance.x);
		props[i++] = (byte)((0xff << 8) & loadDistance.x);
		props[i++] = (byte)((0xff << 16) & loadDistance.x);
		props[i++] = (byte)((0xff << 24) & loadDistance.x);
		
		props[i++] = (byte)((0xff << 0) & loadDistance.y);
		props[i++] = (byte)((0xff << 8) & loadDistance.y);
		props[i++] = (byte)((0xff << 16) & loadDistance.y);
		props[i++] = (byte)((0xff << 24) & loadDistance.y);
		
		props[i++] = (byte)((0xff << 0) & loadDistance.z);
		props[i++] = (byte)((0xff << 8) & loadDistance.z);
		props[i++] = (byte)((0xff << 16) & loadDistance.z);
		props[i++] = (byte)((0xff << 24) & loadDistance.z);

		props[i++] = (byte)((0xff << 0l) & terrainGenerator.seed);
		props[i++] = (byte)((0xff << 8l) & terrainGenerator.seed);
		props[i++] = (byte)((0xff << 16l) & terrainGenerator.seed);
		props[i++] = (byte)((0xff << 24l) & terrainGenerator.seed);
		props[i++] = (byte)((0xff << 32l) & terrainGenerator.seed);
		props[i++] = (byte)((0xff << 40l) & terrainGenerator.seed);
		props[i++] = (byte)((0xff << 48l) & terrainGenerator.seed);
		props[i++] = (byte)((0xff << 56l) & terrainGenerator.seed);
		
		try {
			Files.write(savePath.resolve("world"), props, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		worldNode.removeFromParent();
	}
	
	public void loadChunk(Coord3 chunkPos) {
		getChunk(chunkPos);
	}
	
	public boolean isLoaded(Coord3 chunkPos) {
		return map.containsKey(chunkPos);
	}
	
	public void unloadChunk(Coord3 chunkPos) {
		unloadChunk(map.remove(chunkPos));
	}
	
	private void unloadChunk(Chunk c) {
		if(c == null) {
			System.err.println("Trying to unload a chunk that isn't loaded!");
			return;
		}
		try {
			TPath chunkSave = savePath.resolve(c.globalPosition.toString());
			if(Files.notExists(chunkSave)) {
				try {
					Files.createFile(chunkSave);
				} catch(FileAlreadyExistsException e) {
					e.printStackTrace();
				}
			}
			if(Files.isWritable(chunkSave)) {
				c.save(Files.newOutputStream(chunkSave));
			} else {
				System.err.println("Error saving chunk at "+c.globalPosition+":");
				System.err.println("File not writeable");
			}
		} catch (IOException e) {
			System.err.println("Error saving chunk at "+c.globalPosition+":");
			e.printStackTrace();
		}
		renderExec.execute(() -> {
			c.setEnabled(false);
			c.getSpatial().removeFromParent();
		});
	}
	
	private Chunk readChunk(Coord3 chunkPos, TPath chunkSave) throws IOException {
		Chunk c = new Chunk(chunkPos, this, terrainGenerator, new ByteArrayInputStream(Files.readAllBytes(chunkSave)));
		renderExec.execute(() -> {
			map.put(chunkPos, c);
			worldNode.attachChild(c.getGeometry());
		});
		return c;
	}

	private Chunk generateChunk(Coord3 chunkPos) {
		Chunk c = new Chunk(chunkPos, this, terrainGenerator);
		renderExec.execute(() -> {
			map.put(chunkPos, c);
			worldNode.attachChild(c.getGeometry());
		});
		return c;
	}

	public void loadChunksAroundCamera(Coord3 cameraPos) {
		cameraPos = new Coord3(
				floorDiv(cameraPos.x, chunkSize.x),
				floorDiv(cameraPos.y, chunkSize.y),
				floorDiv(cameraPos.z, chunkSize.z));
		for(Coord3 c: Coord3Box.anchorSizeAlignment(cameraPos, new Coord3(3,3,2), Coord3Box.Alignment.CENTER)) {
			loadChunk(c);
		}
	}

	public boolean shouldUnload(Coord3 globalPosition) {
		return false;
	}
}
