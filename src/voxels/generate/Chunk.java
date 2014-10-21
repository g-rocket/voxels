package voxels.generate;

import voxels.map.*;
import voxels.map.collections.*;
import static voxels.util.StaticUtils.*;

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
		data = new ByteArray3D(SIZE);
		brain = new ChunkBrain();
		meshDirty = true;
	}
	
	public byte getBlockAtPos(Coord3 pos) {
		return data.get(
				mod(pos.x, SIZE.x),
				mod(pos.y, SIZE.y),
				mod(pos.z, SIZE.z));
	}
}
