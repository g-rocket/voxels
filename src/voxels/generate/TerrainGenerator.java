package voxels.generate;

import java.io.*;
import java.util.*;

import voxels.block.*;
import voxels.map.*;

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
	}
}
