package voxels.generate;

import java.io.*;
import java.util.*;

import voxels.block.*;
import voxels.map.*;

import com.sudoplay.joise.*;
import com.sudoplay.joise.module.*;

public class TerrainGenerator {
	private final Module terrain;
	
	public TerrainGenerator() {
		this(new Random().nextLong());
	}
	
	public TerrainGenerator(long seed) {
		terrain = setupNoiseGenerator(seed);
	}
	
	private Module setupNoiseGenerator(long seed) {
		JsonToModule parser;
		parser = new JsonToModule(
				new BufferedReader(
						new InputStreamReader(this.getClass().getResourceAsStream("testTerrain.json"))
				), seed);
		parser.parse();
		return parser.getModule("test");
	}

	public BlockType getBlockAtPosistion(Coord3 pos) {
		return BlockType.getBlock((int)terrain.get(pos.x, pos.y, pos.z));
		/*r.setSeed((long)pos.x | ((long)pos.y << 32));
		r.nextLong(); // makes it look more random
		if(pos.z < 1 + r.nextInt(4)) return BlockType.UNKNOWN;
		if(pos.z < 4 + r.nextInt(8)) return BlockType.ROCK;
		int dtop = 8 + r.nextInt(8);
		if(pos.z < dtop) return BlockType.DIRT;
		if(pos.z == dtop) return BlockType.GRASS;
		return BlockType.AIR;*/
	}
}
