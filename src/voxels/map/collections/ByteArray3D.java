package voxels.map.collections;

import voxels.map.Coord3;

public class ByteArray3D implements ChunkData {
    private Coord3 size;
    @SuppressWarnings("unused")
    private int SIZE_BITS_X,SIZE_BITS_Y,SIZE_BITS_Z;
    private byte data[];
    
    public ByteArray3D(Coord3 size) {
        this.size = size;
        getBitSizes(size);
        data = new byte[size.x*size.y*size.z];
    }
    
    private void getBitSizes(Coord3 _size) {
        SIZE_BITS_X = LogBase2(_size.x);
        SIZE_BITS_Y = LogBase2(_size.y);
        SIZE_BITS_Z = LogBase2(_size.z);
    }
    
    private static int LogBase2(int n) { // assumes positive integer power of two
    	for(int i = 30; i >= 0; i--) {
    		if((1 << i) == n) return i;
    	}
    	throw new IllegalArgumentException("i is not a positive integer power of two");
    }
 
    public byte get(Coord3 pos) {
        return get(pos.x, pos.y, pos.z);//convenience method.
    }
    
    /*
     * This bitwise index look up is the same as [y * (size.x*size.z) + z * (size.x) + x]
     */
    public byte get(int x, int y, int z) {
        return data[y*(size.x*size.z) + z*size.x + x];
    }
    
    public void set(byte obj, Coord3 pos) {
        set(obj, pos.x, pos.y, pos.z); //convenience method. no need to change.
    }
    
    public void set(byte obj, int x, int y, int z) {
    	data[y*(size.x*size.z) + z*size.x + x] = obj;
    }
 
    public boolean indexWithinBounds(int x, int y, int z) {
        return x < size.x && y < size.y && z < size.z;
    }
}