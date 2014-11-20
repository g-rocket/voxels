package voxels.map.collections;

import voxels.block.*;
import voxels.map.*;

public interface ChunkData {

    public BlockType get(Coord3 pos);
    
    public BlockType get(int x, int y, int z);
    
    public void set(BlockType obj, Coord3 pos);
    
    public void set(BlockType obj, int x, int y, int z);
 
    public boolean indexWithinBounds(int x, int y, int z);
}
