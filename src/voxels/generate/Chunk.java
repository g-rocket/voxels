package voxels.generate;

import voxels.map.*;
import voxels.map.collections.*;

/*
 * Deals with a single chunk of information
 */
public class Chunk {
	public static final Coord3 SIZE = new Coord3(16,16,16);
	public final Coord3 position;
	public final ChunkBrain brain;
	private final WorldMap world;
	private final ByteArray3D data;
	public boolean meshBuilding;
	public boolean meshDirty;
	
	public Chunk(Coord3 position, WorldMap world) {
		this.position = position;
		this.world = world;
		this.data = new ByteArray3D(SIZE);
	}
	
	public byte getBlockAtPos(Coord3 pos) {
		return data.get(
				pos.x % SIZE.x + (pos.x<0? SIZE.x: 0),
				pos.y % SIZE.y + (pos.y<0? SIZE.y: 0),
				pos.z % SIZE.z + (pos.z<0? SIZE.z: 0));
	}
}
