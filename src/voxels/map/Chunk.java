package voxels.map;

import static voxels.map.Coord3.*;

import java.io.*;
import java.util.*;

import voxels.block.*;
import voxels.block.texture.*;
import voxels.generate.*;
import voxels.map.collections.*;
import voxels.meshconstruction.*;
import voxels.util.*;

import com.jme3.renderer.*;
import com.jme3.scene.*;
import com.jme3.scene.control.*;

/*
 * Deals with a single chunk of information
 */
public class Chunk extends AbstractControl {
	public final Coord3 globalPosition;
	public final Coord3 position;
	private final WorldMap world;
	private final ChunkData data;
	public final ChunkData blocks;
	public boolean meshDirty;
	private float ticksSinceNeeded = 0;
	private boolean isUnloading = false;
	
	public Chunk(Coord3 position, WorldMap world, TerrainGenerator terrainGenerator) {
		this.globalPosition = position;
		this.position = position.times(world.chunkSize);
		this.world = world;
		data = new LazyGeneratedChunkData(world.chunkSize, this.position, terrainGenerator);
		buildMesh(false); // pre-generate the terrain
		blocks = new UnmodifiableChunkData(data);
		meshDirty = true;
	}
	
	public Chunk(Coord3 position, WorldMap world, TerrainGenerator terrainGenerator, InputStream data) throws IOException {
		this.globalPosition = position;
		this.position = position.times(world.chunkSize);
		this.world = world;
		this.data = new LazyGeneratedChunkData(world.chunkSize, this.position, terrainGenerator, data);
		blocks = new UnmodifiableChunkData(this.data);
		meshDirty = true;
	}
	
	public void save(OutputStream output) throws IOException {
		data.save(output);
		output.close();
	}

	@Override
	protected void controlUpdate(float tpf) {
		ticksSinceNeeded += tpf;
		if(ticksSinceNeeded > 16f) {
			if(world.isLoaded(globalPosition) && world.shouldUnload(globalPosition) && world.chunksShouldUnload && !isUnloading) {
				isUnloading = true;
				System.out.print("*");
				world.exec.execute(new Runnable() {
					@Override
					public void run() {
						world.unloadChunk(globalPosition);
					}
				});
			}
		}
		if(meshDirty) {
			meshDirty = false;
			world.exec.execute(new Runnable() {
				@Override
				public void run() {
					buildMesh(true);
				}
			});
		}
	}

	public Iterable<Coord3> blocksPoss() {
		return Coord3.range(position, world.chunkSize);
	}
	
	private void buildMesh(boolean doMesh) {
        MeshSet mset = new MeshSet();
        Coord3 csm1 = world.chunkSize.minus(c3(1,1,1));
		List<Coord3> toMesh = new ListStartedList<>(
				Coord3.range(position, world.chunkSize.times(c3(1,1,0)).plus(c3(0,0,1))),
				Coord3.range(position, world.chunkSize.times(c3(1,0,1)).plus(c3(0,1,0))),
				Coord3.range(position, world.chunkSize.times(c3(0,1,1)).plus(c3(1,0,0))),
				Coord3.range(position.plus(csm1.times(c3(0,0,1))), world.chunkSize.times(c3(1,1,0)).plus(c3(0,0,1))),
				Coord3.range(position.plus(csm1.times(c3(0,1,0))), world.chunkSize.times(c3(1,0,1)).plus(c3(0,1,0))),
				Coord3.range(position.plus(csm1.times(c3(1,0,0))), world.chunkSize.times(c3(0,1,1)).plus(c3(1,0,0)))
		);
		Set<Coord3> meshed = new HashSet<>();
        for(Coord3 blockPos: toMesh){
        	if(meshed.contains(blockPos)) continue;
        	meshed.add(blockPos);
        	BlockType block = data.get(blockPos);
        	for(Direction dir: Direction.values()) {
        		Coord3 blockPos2 = blockPos.plus(dir.c3);
        		BlockType block2 = data.get(blockPos2);
        		if(block.isOpaque) {
        			if(!data.indexWithinBounds(blockPos2) || !block2.isOpaque) {
        				if(doMesh) BlockMeshUtil.addFaceMeshData(blockPos, block, mset, dir, .5f+(blockPos.z)/256f);
        			}
        		} else {
            		if(data.indexWithinBounds(blockPos2) && !meshed.contains(blockPos2)) {
            			toMesh.add(blockPos2);
            		}
        		}
        	}
        }
        System.out.print(".");
        if(doMesh) {
        	world.renderExec.execute(new Runnable() {
        		@Override
        		public void run() {
        			MeshBuilder.applyMeshSet(mset, getGeometry().getMesh());
        			getGeometry().updateModelBound();
        		}
        	});
        }
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
            //mesh.setMode(Mesh.Mode.Triangles); // GL draw mode 
            geom = new Geometry("chunk_"+position+"_geometry", mesh);
            geom.setMaterial(world.blockMaterial);
            geom.addControl(this);
        }
        return geom;
    }

	public void setNeeded() {
		ticksSinceNeeded = 0;
	}
}
