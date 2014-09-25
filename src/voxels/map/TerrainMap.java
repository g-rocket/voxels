package voxels.map;

import static voxels.map.BlockType.*;

public class TerrainMap {
	
	public byte getBlock(Coord3 woco) {
		return (byte) DIRT.ordinal();
	}
}
