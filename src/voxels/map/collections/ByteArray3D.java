package voxels.map.collections;

import java.io.*;

import voxels.block.*;
import voxels.map.*;

public class ByteArray3D implements ChunkData {
    private Coord3 size;
    private byte data[];
    
    public ByteArray3D(Coord3 size) {
        this.size = size;
        data = new byte[size.x*size.y*size.z];
    }
    
    public ByteArray3D(Coord3 size, InputStream data) throws IOException {
        this.size = size;
        this.data = new byte[size.x*size.y*size.z];
        load(data);
    }
    
    /*
     * This bitwise index look up is the same as [y * (size.x*size.z) + z * (size.x) + x]
     */
    @Override
	public BlockType get(int x, int y, int z) {
    	//if(!indexWithinBounds(x, y, z)) throw new IndexOutOfBoundsException(String.format("(%d,%d,%d) is invalid",x,y,z));
        return BlockType.getBlock(data[y*(size.x*size.z) + z*size.x + x]);
    }
    
    @Override
	public void set(BlockType obj, int x, int y, int z) {
    	data[y*(size.x*size.z) + z*size.x + x] = obj.dataValue;
    }
 
    @Override
	public boolean indexWithinBounds(int x, int y, int z) {
        return x >= 0 && x < size.x && y >= 0 && y < size.y && z >= 0 && z < size.z;
    }

	@Override
	public void save(OutputStream output) throws IOException {
		output.write(data);
	}

	@Override
	public void load(InputStream input) throws IOException {
		int numBytesReadIn = input.read(data);
		if(numBytesReadIn < data.length) throw new IOException("We appear to be missing some of our chunk");
	}
}