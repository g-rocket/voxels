package voxels.map.collections;

import voxels.block.*;
import voxels.map.*;

public class ShiftedUnmodifiableChunkData implements ChunkData {
	private ChunkData data;
	private Coord3 shift;
	
	public ShiftedUnmodifiableChunkData(ChunkData data, Coord3 shift) {
		this.data = data;
		this.shift = shift;
	}

	@Override
	public BlockType get(Coord3 pos) {
		return get(pos.x, pos.y, pos.z);
	}

	@Override
	public BlockType get(int x, int y, int z) {
		return data.get(x - shift.x, y - shift.y, z - shift.z);
	}

	@Override
	public void set(BlockType obj, Coord3 pos) {
		throw new UnsupportedOperationException("You can't call 'set' on an unmodifiable chunk data");
	}

	@Override
	public void set(BlockType obj, int x, int y, int z) {
		throw new UnsupportedOperationException("You can't call 'set' on an unmodifiable chunk data");
	}

	@Override
	public boolean indexWithinBounds(int x, int y, int z) {
		return data.indexWithinBounds(x - shift.x, y - shift.y, z - shift.z);
	}

}
