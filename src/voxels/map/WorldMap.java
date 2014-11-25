package voxels.map;

import static voxels.util.StaticUtils.*;

import java.util.*;

import voxels.block.*;
import voxels.generate.*;

import com.jme3.material.*;
import com.jme3.scene.*;

/*
 * Deals with the overarching structure (knows about ALL the chunks)
 */
public class WorldMap {
	public final Coord3 chunkSize;
	private HashMap<Coord3, Chunk> map = new HashMap<Coord3, Chunk>();
	private final Node worldNode;
	public final Material blockMaterial;
	public final TerrainGenerator terrainGenerator;
	
	public WorldMap(Node worldNode, Material blockMaterial) {
		chunkSize = new Coord3(32,32,16);
		this.worldNode = worldNode;
		this.blockMaterial = blockMaterial;
		this.terrainGenerator = new TerrainGenerator();
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
			return c;
		} else {
			return generateChunk(chunkPos);
		}
	}
	
	private Chunk generateChunk(Coord3 chunkPos) {
		Chunk c = new Chunk(chunkPos, this, terrainGenerator);
		map.put(chunkPos, c);
		worldNode.attachChild(c.getGeometry());
		return c;
	}
}
