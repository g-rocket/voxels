package voxels.map;

import static voxels.map.BlockType.*;
import static voxels.util.StaticUtils.*;

import java.util.*;

import voxels.generate.*;

/*
 * Deals with the overarching structure (knows about ALL the chunks)
 */
public class WorldMap {
	public final Coord3 chunkSize;
	private HashMap<Coord3, Chunk> map = new HashMap<Coord3, Chunk>();
	
	public WorldMap() {
		chunkSize = new Coord3(16,16,16);
	}
	
	public BlockType getBlock(Coord3 blockPos) {
		return getChunk(chunkPos(blockPos)).getBlock(blockPos);
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
		Chunk c = new Chunk(chunkPos, this);
		//TODO: generate some terrain
		map.put(chunkPos, c);
		return c;
	}
}
