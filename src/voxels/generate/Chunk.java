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
	
	public Chunk(Coord3 position, WorldMap world, TerrainGenerater terrainGenerater) {
		this.position = position;
		this.world = world;
		data = new ByteArray3D(world.chunkSize);
		blocks = new ShiftedUnmodifiableChunkData(data, position.dot(world.chunkSize));
		for(int x = 0; x < world.chunkSize.x; x++) {
			for(int y = 0; y < world.chunkSize.y; y++) {
				for(int z = 0; z < world.chunkSize.z; z++) {
					data.set(terrainGenerater.getBlockAtPosistion(getGlobalPos(x, y, z)).dataValue, new Coord3(x,y,z));
				}
			}
		}
		meshDirty = true;
	}
	
	public BlockType getBlock(Coord3 blockPos) {
		return getBlockLocal(
				mod(blockPos.x, world.chunkSize.x),
				mod(blockPos.y, world.chunkSize.y),
				mod(blockPos.z, world.chunkSize.z));
	}
	
	private boolean isValidLocal(int x, int y, int z) {
		return data.indexWithinBounds(x, y, z);
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
	
	private Coord3 getGlobalPos(int x, int y, int z) {
		return new Coord3(
				x + (position.x*world.chunkSize.x),
				y + (position.y*world.chunkSize.y),
				z + (position.z*world.chunkSize.z));
	}

	private void buildMesh() {
        MeshSet mset = new MeshSet();
        
        for(int x = 0; x < world.chunkSize.x; x++) {
        	for(int y = 0; y < world.chunkSize.y; y++) {
        		for(int z = 0; z < world.chunkSize.z; z++) {
        			BlockType block = getBlockLocal(x, y, z);
        			for(Direction dir: Direction.values()) {
        				if(block.isOpaque &&
        				   (!isValidLocal(x+dir.dx, y+dir.dy, z+dir.dz) ||
        				   !getBlockLocal(x+dir.dx, y+dir.dy, z+dir.dz).isOpaque)) {
        					BlockMeshUtil.addFaceMeshData(getGlobalPos(x,y,z), block, mset, dir);
        				}
        			}
        		}
        	}
        }
        
        MeshBuilder.applyMeshSet(mset, getGeometry().getMesh());
        /* GAVIN:
         * THE FOLLOWING COMMENTED OUT CODE IS A 'COUSIN' OF mesh.updateBound().
         * THAT'S THE GOOD NEWS. BAD NEWS IS IT DOESN'T SOLVE THE DISAPPEARING MESH PROBLEM!
         * HENCE, JUST LEAVING IT HERE FOR YOUR REFERENCE (IT MIGHT BE GOOD TO KNOW ABOUT NONETHELESS). 
         * PLEASE DELETE THIS NOTE AND THE LINE AS NEEDED. --MEDDLER
         */
        // getGeometry().updateModelBound();
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
            geom.setMaterial(world.blockMaterial);
            geom.addControl(this);
        }
        return geom;
    }
}
