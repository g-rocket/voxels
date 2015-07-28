package voxels.generate;

import java.io.*;
import java.util.*;

import voxels.block.*;
import voxels.map.*;

import com.sudoplay.joise.module.*;

public class TerrainGenerator {
	private final ThreadLocal<Module> terrain;
	public final long seed;
	
	public TerrainGenerator() {
		this(new Random().nextLong());
	}
	
	public TerrainGenerator(long seed) {
		this.seed = seed;
		terrain = new ThreadLocal<Module>() {
			@Override
			protected Module initialValue() {
				return setupNoiseGenerator(seed);
			}
		};
	}
	
	private Module setupNoiseGenerator(long seed) {
		JsonToModule parser;
		parser = new JsonToModule(
				new BufferedReader(
						new InputStreamReader(this.getClass().getResourceAsStream("testTerrain.json"))
				), seed);
		//parser.parse();
		//return parser.getModule("test");
		return parser.getModule("main");
	}

	public BlockType getBlockAtPosition(Coord3 pos) {
		return BlockType.getBlock((int)terrain.get().get(pos.x, pos.y, pos.z));
	}
}
