package voxels.map.collections;

import voxels.block.*;
import voxels.generate.*;
import voxels.map.*;

public class LazyGeneratedChunkData implements ChunkData {
	private final ByteArray3D data;
	private final Coord3 shift;
	private final TerrainGenerator generator;
	
	public LazyGeneratedChunkData(Coord3 size, Coord3 shift, TerrainGenerator generator) {
		data = new ByteArray3D(size);
		this.shift = shift;
		this.generator = generator;
	}

	@Override
	public BlockType get(Coord3 pos) {
		return get(pos.x, pos.y, pos.z);
	}

	@Override
	public BlockType get(int x, int y, int z) {
		BlockType block = data.get(x,y,z);
		if(block.equals(BlockType.UNKNOWN)) {
			block = generator.getBlockAtPosistion(new Coord3(x - shift.x, y - shift.y, z + shift.z)); //TODO: what's up with z+?
		}
		return block;
	}

	@Override
	public void set(BlockType obj, Coord3 pos) {
		data.set(obj, pos);
	}

	@Override
	public void set(BlockType obj, int x, int y, int z) {
		data.set(obj, x, y, z);
	}

	@Override
	public boolean indexWithinBounds(int x, int y, int z) {
		return data.indexWithinBounds(x, y, z);
	}

}
