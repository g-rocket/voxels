package voxels.map.collections;

import voxels.block.*;
import voxels.map.*;

public class UnmodifiableChunkData implements ChunkData {
	private ChunkData data;
	
	public UnmodifiableChunkData(ChunkData data) {
		this.data = data;
	}

	@Override
	public BlockType get(int x, int y, int z) {
		return data.get(x, y, z);
	}

	@Override
	public void set(BlockType obj, int x, int y, int z) {
		throw new UnsupportedOperationException("You can't call 'set' on an unmodifiable chunk data");
	}

	@Override
	public boolean indexWithinBounds(int x, int y, int z) {
		return data.indexWithinBounds(x, y, z);
	}

}
