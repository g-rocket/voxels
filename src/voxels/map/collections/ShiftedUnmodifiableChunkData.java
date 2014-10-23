package voxels.map.collections;

import voxels.map.*;

public class ShiftedUnmodifiableChunkData implements ChunkData {
	private ChunkData data;
	private Coord3 shift;
	
	public ShiftedUnmodifiableChunkData(ChunkData data, Coord3 shift) {
		this.data = data;
		this.shift = shift;
	}

	@Override
	public byte get(Coord3 pos) {
		return data.get(pos);
	}

	@Override
	public byte get(int x, int y, int z) {
		return data.get(x, y, z);
	}

	@Override
	public void set(byte obj, Coord3 pos) {
		throw new UnsupportedOperationException("You can't call 'set' on an unmodifyable chunk data");
	}

	@Override
	public void set(byte obj, int x, int y, int z) {
		throw new UnsupportedOperationException("You can't call 'set' on an unmodifyable chunk data");
	}

	@Override
	public boolean indexWithinBounds(int x, int y, int z) {
		return data.indexWithinBounds(x - shift.x, y - shift.y, z - shift.z);
	}

}
