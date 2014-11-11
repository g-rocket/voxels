package voxels.generate;

import java.util.*;

import voxels.map.*;

public class TerrainGenerater {
	Random r = new Random();
	public BlockType getBlockAtPosistion(Coord3 pos) {
		r.setSeed((long)pos.x | ((long)pos.y << 32));
		r.nextLong(); // makes it look more random
		if(pos.z < 1 + r.nextInt(4)) return BlockType.UNKNOWN;
		if(pos.z < 4 + r.nextInt(8)) return BlockType.ROCK;
		int dtop = 8 + r.nextInt(8);
		if(pos.z < dtop) return BlockType.DIRT;
		if(pos.z == dtop) return BlockType.GRASS;
		return BlockType.AIR;
	}
}
