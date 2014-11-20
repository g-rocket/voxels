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
		block = data.get(
				StaticUtils.mod(x, size.x), 
				StaticUtils.mod(y, size.y), 
				StaticUtils.mod(z, size.z));
		if(block.equals(BlockType.UNKNOWN)) {
			Coord3 pos = new Coord3(x, y, z);
			block = generator.getBlockAtPosistion(pos);
			if(indexWithinBounds(x, y, z)) set(block, pos);
		}
		return block;
	}
	
	@Override
	public void set(BlockType obj, Coord3 pos) {
		//System.out.println(pos+" "+position+" "+pos.minus(position));
		data.set(obj, pos.minus(position));
	}
	
	@Override
	public void set(BlockType obj, int x, int y, int z) {
		set(obj, new Coord3(x, y, z));
	}
	
	@Override
	public boolean indexWithinBounds(Coord3 pos) {
		return data.indexWithinBounds(pos.minus(position));
	}
	
	@Override
	public boolean indexWithinBounds(int x, int y, int z) {
		return indexWithinBounds(new Coord3(x, y, z));
	}

}
