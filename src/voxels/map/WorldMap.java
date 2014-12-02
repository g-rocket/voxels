package voxels.map;

import static voxels.util.StaticUtils.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

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
	public final Executor exec = Executors.newWorkStealingPool();
	public final Executor renderExec;
	public volatile boolean chunksShouldUnload = true;
	
	public WorldMap(Node worldNode, Material blockMaterial, File saveFile, Executor renderThreadExecutor) {
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
			terrainGenerator = new TerrainGenerator(
					props[i++] | (props[i++]<<8l) | (props[i++]<<16l) | (props[i++]<<24l) |
					(props[i++]<<32l) | (props[i++]<<40l) | (props[i++]<<48l) | (props[i++]<<56l));
		} else {
			chunkSize = new Coord3(32,32,16);
			terrainGenerator = new TerrainGenerator();
		}
		this.worldNode = worldNode;
		this.blockMaterial = blockMaterial;
		this.renderExec = renderThreadExecutor;
		
		/*new Timer(true).schedule(new TimerTask() {
			
			@Override
			public void run() {
				System.out.println("\nunloading chunks");
				chunksShouldUnload = false;
			}
		}, 12000);*/
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
		chunksShouldUnload = false;
		for(Iterator<Map.Entry<Coord3, Chunk>> chunkI = map.entrySet().iterator(); chunkI.hasNext();) {
			unloadChunk(chunkI.next().getValue());
			chunkI.remove();
		}
		byte[] props = new byte[24];
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
		exec.execute(new Runnable() {
			@Override
			public void run() {
				getChunk(chunkPos);
			}
		});
	}
	
	public boolean isLoaded(Coord3 chunkPos) {
		return map.containsKey(chunkPos);
	}
	
	public void unloadChunk(Coord3 chunkPos) {
		unloadChunk(map.remove(chunkPos));
	}
	
	private void unloadChunk(Chunk c) {
		exec.execute(new Runnable() {
			@Override
			public void run() {
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
				renderExec.execute(new Runnable(){
					@Override
					public void run() {
						c.setEnabled(false);
						c.getSpatial().removeFromParent();
					}
				});
			}
		});
	}
	
	private Chunk readChunk(Coord3 chunkPos, TPath chunkSave) throws IOException {
		Chunk c = new Chunk(chunkPos, this, terrainGenerator, new ByteArrayInputStream(Files.readAllBytes(chunkSave)));
		renderExec.execute(new Runnable() {
			@Override
			public void run() {
				map.put(chunkPos, c);
				worldNode.attachChild(c.getGeometry());
			}
		});
		return c;
	}

	private Chunk generateChunk(Coord3 chunkPos) {
		Chunk c = new Chunk(chunkPos, this, terrainGenerator);
		renderExec.execute(new Runnable() {
			@Override
			public void run() {
				map.put(chunkPos, c);
				worldNode.attachChild(c.getGeometry());
			}
		});
		return c;
	}

	public void loadChunksAroundCamera(Coord3 cameraPos) {
		cameraPos = cameraPos.divBy(chunkSize);
		for(Coord3 c: Coord3.range(cameraPos.minus(new Coord3(2,2,1)), new Coord3(4,4,3))) {
			getChunk(c);
		}
	}
}
