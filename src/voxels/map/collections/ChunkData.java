package voxels.map.collections;

import voxels.block.*;
import voxels.map.*;

public interface ChunkData {

    public default BlockType get(Coord3 pos) {
    	return get(pos.x, pos.y, pos.z);
    }
    
    public BlockType get(int x, int y, int z);
    
    public default void set(BlockType obj, Coord3 pos) {
    	set(obj, pos.x, pos.y, pos.z);
    }
    
    public void set(BlockType obj, int x, int y, int z);
 
    public boolean indexWithinBounds(int x, int y, int z);

	public default boolean indexWithinBounds(Coord3 pos) {
		return indexWithinBounds(pos.x, pos.y, pos.z);
	}
}
