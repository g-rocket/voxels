package voxels.generate;

import java.util.*;

import voxels.map.*;

public class TerrainGenerater {
	Random r = new Random();
	public BlockType getBlockAtPosistion(Coord3 pos) {
		if(r.nextInt(4) < 1) {
			return BlockType.values()[r.nextInt(BlockType.values().length)];
		}
		return BlockType.AIR;
	}
}
