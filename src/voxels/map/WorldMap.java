package voxels.map;

import static voxels.map.BlockType.*;

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
	
	public byte getBlock(Coord3 woco) {
		//TODO: implement me;
		return DIRT.dataValue;
	}
	
	public void generateChunk(Coord3 position) {
		//TODO: implement me
	}
}
