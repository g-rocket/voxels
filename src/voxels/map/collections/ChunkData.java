package voxels.map.collections;

import voxels.map.*;

public interface ChunkData {

    public byte get(Coord3 pos);
    
    public byte get(int x, int y, int z);
    
    public void set(byte obj, Coord3 pos);
    
    public void set(byte obj, int x, int y, int z);
 
    public boolean indexWithinBounds(int x, int y, int z);
}
