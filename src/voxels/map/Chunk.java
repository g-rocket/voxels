package voxels.map;

import java.awt.*;

import com.jme3.renderer.*;
import com.jme3.scene.*;
import com.jme3.scene.control.*;

import voxels.block.*;
import voxels.block.texture.*;
import voxels.generate.*;
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
	
	public Chunk(Coord3 position, WorldMap world, TerrainGenerator terrainGenerator) {
		this.position = position;
		this.world = world;
		data = new LazyGeneratedChunkData(world.chunkSize, position, terrainGenerator);
		blocks = new UnmodifiableChunkData(data);
		meshDirty = true;
	}

	@Override
	protected void controlUpdate(float tpf) {
		if(meshDirty) {
			buildMesh();
			meshDirty = false;
		}
	}

	public Iterable<Coord3> blocksPoss() {
		return Coord3.range(position.times(world.chunkSize), position.plus(new Coord3(1,1,1)).times(world.chunkSize));
	}
	
	private void buildMesh() {
        MeshSet mset = new MeshSet();
        for(Coord3 blockPos: blocksPoss()){
        	BlockType block = data.get(blockPos);
        	for(Direction dir: Direction.values()) {
        		if(block.isOpaque &&
        				(!data.indexWithinBounds(blockPos.plus(dir)) ||
        				 !data.get(blockPos.plus(dir)).isOpaque)) {
        			BlockMeshUtil.addFaceMeshData(blockPos, block, mset, dir, .5f+(blockPos.z)/256f);
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
            geom = new Geometry("chunk_"+position+"_geometry", mesh);
            geom.setMaterial(world.blockMaterial);
            geom.addControl(this);
        }
        return geom;
    }
}
