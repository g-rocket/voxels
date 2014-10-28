package voxels.generate;

import com.jme3.renderer.*;
import com.jme3.scene.*;
import com.jme3.scene.control.*;

import voxels.map.*;
import voxels.map.collections.*;
import voxels.meshconstruction.*;
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
	
	public BlockType getBlock(Coord3 blockPos) {
		return getBlockLocal(
				mod(blockPos.x, world.chunkSize.x),
				mod(blockPos.y, world.chunkSize.y),
				mod(blockPos.z, world.chunkSize.z));
	}
	
	private BlockType getBlockLocal(int x, int y, int z) {
		return BlockType.getBlock(data.get(x, y, z));
	}

	private BlockType getBlockLocal(Coord3 localPos) {
		return getBlockLocal(localPos.x, localPos.y, localPos.z);
	}

	@Override
	protected void controlUpdate(float tpf) {
		if(meshDirty) {
			buildMesh();
			meshDirty = false;
		}
	}

	private void buildMesh() {
        MeshSet mset = new MeshSet();
        
        for(int x = 0; x < world.chunkSize.x; x++) {
        	for(int y = 0; y < world.chunkSize.y; y++) {
        		for(int z = 0; z < world.chunkSize.z; z++) {
        			BlockType block = getBlockLocal(x, y, z);
        			// maybe add textures to mesh
        		}
        	}
        }
        
        MeshBuilder.applyMeshSet(mset, getGeometry().getMesh());

	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		// do nothing
	}
	
	public Geometry getGeometry() {
        Geometry geom = (Geometry) getSpatial(); // an AbstractControl method
        if (geom == null) {
            Mesh mesh = new Mesh(); // placeholder mesh to be filled later
            mesh.setDynamic(); // hint to openGL that the mesh may change occasionally
            mesh.setMode(Mesh.Mode.Triangles); // GL draw mode 
            geom = new Geometry("chunk_geometry", mesh);
            geom.addControl(this);
        }
        return geom;
    }
}
