package voxels.generate;

import com.jme3.renderer.*;
import com.jme3.scene.control.*;

import voxels.map.*;
import voxels.map.collections.*;
import static voxels.util.StaticUtils.*;

/*
 * Deals with a single chunk of information
 */
public class Chunk extends AbstractControl {
	public final Coord3 position;
	private final WorldMap world;
	private final ChunkData data;
	public final ChunkData blocks;
	public boolean meshDirty;
	
	public Chunk(Coord3 position, WorldMap world) {
		this.position = position;
		this.world = world;
		data = new ByteArray3D(world.chunkSize);
		blocks = new ShiftedUnmodifiableChunkData(data, position.dot(world.chunkSize));
		meshDirty = true;
	}
	
	public byte getBlockAtPos(Coord3 pos) {
		return data.get(
				mod(pos.x, world.chunkSize.x),
				mod(pos.y, world.chunkSize.y),
				mod(pos.z, world.chunkSize.z));
	}

	@Override
	protected void controlUpdate(float tpf) {
		if(meshDirty) {
			buildMesh();
			meshDirty = false;
		}
	}

	private void buildMesh() {
		//TODO: implement me
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		// do nothing
	}
}
