package voxels.map.collections;

import voxels.block.*;
import voxels.generate.*;
import voxels.map.*;
import voxels.util.*;

public class LazyGeneratedChunkData implements ChunkData {
	private final ByteArray3D data;
	private final Coord3 size;
	private final Coord3 position;
	private final TerrainGenerator generator;
	
	public LazyGeneratedChunkData(Coord3 size, Coord3 position, TerrainGenerator generator) {
		data = new ByteArray3D(size);
		this.size = size;
		this.position = position;
		this.generator = generator;
	}

	@Override
	public BlockType get(int x, int y, int z) {
		BlockType block;
		try {
			block = data.get(
					StaticUtils.mod(x, size.x), 
					StaticUtils.mod(y, size.y), 
					StaticUtils.mod(z, size.z));
		} catch(IndexOutOfBoundsException e) {
			throw new RuntimeException(String.format("(%d, %d, %d) -> (%d, %d, %d), which is out of bounds",x,y,z,
					StaticUtils.mod(x, size.x), 
					StaticUtils.mod(y, size.y), 
					StaticUtils.mod(z, size.z)),e);
		}
		if(block.equals(BlockType.UNKNOWN)) {
			block = generator.getBlockAtPosistion(new Coord3(x, y, z));
		}
		return block;
	}

	@Override
	public void set(BlockType obj, int x, int y, int z) {
		data.set(obj, x, y, z);
	}

	@Override
	public boolean indexWithinBounds(int x, int y, int z) {
		return data.indexWithinBounds(
				x - (position.x * size.x), 
				y - (position.y * size.y), 
				z - (position.z * size.z));
	}

}
