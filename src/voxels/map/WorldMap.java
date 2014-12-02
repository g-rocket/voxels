package voxels.map;

import static voxels.util.StaticUtils.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

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
	
	public WorldMap(Node worldNode, Material blockMaterial, File saveFile) {
		chunkSize = new Coord3(32,32,16);
		this.worldNode = worldNode;
		this.blockMaterial = blockMaterial;
		this.terrainGenerator = new TerrainGenerator();
		this.savePath = new TPath(saveFile);
		try {
			FileSystems.newFileSystem(savePath, this.getClass().getClassLoader());
			//if(Files.notExists(savePath.getParent())) Files.createDirectories(savePath.getParent());
			if(Files.notExists(savePath)) Files.createDirectories(savePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BlockType getBlock(Coord3 blockPos) {
		return getChunk(chunkPos(blockPos)).blocks.get(blockPos);
	}
	
	private Coord3 chunkPos(Coord3 blockPos) {
		return new Coord3(
				ifdiv(blockPos.x, chunkSize.x), 
				ifdiv(blockPos.x, chunkSize.x), 
				ifdiv(blockPos.x, chunkSize.x));
	}
	
	public Chunk getChunk(Coord3 chunkPos) {
		Chunk c = map.get(chunkPos);
		if(c != null) {
			System.out.println("already loaded chunk at "+chunkPos);
			return c;
		} else {
			try {
				TPath chunkSave = savePath.resolve(chunkPos.toString());
				if(Files.isReadable(chunkSave)) {
					System.out.println("loading chunk at "+chunkPos);
					return loadChunk(chunkPos, chunkSave);
				} else {
					System.out.println("generating chunk at "+chunkPos);
					return generateChunk(chunkPos);
				}
			} catch (IOException e) {
				System.err.println("error loading chunk at "+chunkPos);
				e.printStackTrace();
				return generateChunk(chunkPos);
			}
			
		}
	}
	
	public void unloadAllChunks() {
		for(Coord3 chunkPos: map.keySet()) {
			unloadChunk(chunkPos);
		}
	}
	
	public void unloadChunk(Coord3 chunkPos) {
		Chunk c = map.get(chunkPos);
		if(c == null) {
			System.err.println("Trying to unload a chunk that isn't loaded!");
			return;
		}
		try {
			TPath chunkSave = savePath.resolve(chunkPos.toString());
			if(Files.notExists(chunkSave)) {
				try {
					Files.createFile(chunkSave);
				} catch(FileAlreadyExistsException e) {
					// I don't care
				}
			}
			if(Files.isWritable(chunkSave)) {
				c.save(Files.newOutputStream(chunkSave));
			} else {
				System.err.println("Error saving chunk at "+chunkPos+":");
				System.err.println("File not writeable");
			}
		} catch (IOException e) {
			System.err.println("Error saving chunk at "+chunkPos+":");
			e.printStackTrace();
		}
	}
	
	private Chunk loadChunk(Coord3 chunkPos, TPath chunkSave) throws IOException {
		Chunk c = new Chunk(chunkPos, this, terrainGenerator, new ByteArrayInputStream(Files.readAllBytes(chunkSave)));
		map.put(chunkPos, c);
		worldNode.attachChild(c.getGeometry());
		return c;
	}

	private Chunk generateChunk(Coord3 chunkPos) {
		Chunk c = new Chunk(chunkPos, this, terrainGenerator);
		map.put(chunkPos, c);
		worldNode.attachChild(c.getGeometry());
		return c;
	}

	public void loadChunksAroundCamera(Coord3 cameraPos) {
		cameraPos = cameraPos.divBy(chunkSize);
		for(Coord3 c: Coord3.range(cameraPos.minus(new Coord3(2,2,1)), new Coord3(5,5,3))) {
			getChunk(c);
		}
	}
}
