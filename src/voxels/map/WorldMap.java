package voxels.map;

import static voxels.map.BlockType.*;


/*
 * Deals with the overarching structure (knows about ALL the chunks)
 */
public class WorldMap {
	public byte getBlock(Coord3 woco) {
		return (byte) DIRT.ordinal();
	}
}
