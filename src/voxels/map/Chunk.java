package voxels.map;

import java.util.*;

import com.google.common.collect.*;
import com.jme3.renderer.*;
import com.jme3.scene.*;
import com.jme3.scene.control.*;

import voxels.block.*;
import voxels.block.texture.*;
import voxels.generate.*;
import voxels.map.*;
import voxels.map.collections.*;
import voxels.meshconstruction.*;
import voxels.util.*;
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
		this.position = position.times(world.chunkSize);
		this.world = world;
		data = new LazyGeneratedChunkData(world.chunkSize, this.position, terrainGenerator);
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
		return Coord3.range(position, position.plus(world.chunkSize));
	}
	
	private class IterableStartedQueue<E> extends AbstractQueue<E> {
		private final Iterator<E> starter;
		private final Queue<E> backing = new LinkedList<>();

		public IterableStartedQueue(Iterator<E> starter) {
			this.starter = starter;
		}

		public IterableStartedQueue(Iterable<E> starter) {
			this.starter = starter.iterator();
		}
		
		@Override
		public boolean offer(E e) {
			return backing.offer(e);
		}

		@Override
		public E poll() {
			return starter.hasNext()? starter.next(): backing.poll();
		}

		@Override
		public E peek() {
			throw new UnsupportedOperationException("Can't peek an iterable");
		}

		@Override
		public Iterator<E> iterator() {
			return new Iterator<E>() {

				@Override
				public boolean hasNext() {
					return starter.hasNext() || !backing.isEmpty();
				}

				@Override
				public E next() {
					return poll();
				}
				
			};
		}

		@Override
		public int size() {
			return Integer.MAX_VALUE; // unknown
		}
	}
	
	private void buildMesh() {
        MeshSet mset = new MeshSet();
        Coord3 csm1 = world.chunkSize.minus(new Coord3(1,1,1));
		@SuppressWarnings("unchecked")
		Queue<Coord3> toMesh = new IterableStartedQueue<>(Iterables.concat(
				Coord3.range(position, position.plus(world.chunkSize.times(new Coord3(1,1,0))).plus(new Coord3(0,0,1))),
				Coord3.range(position, position.plus(world.chunkSize.times(new Coord3(1,0,1))).plus(new Coord3(0,1,0))),
				Coord3.range(position, position.plus(world.chunkSize.times(new Coord3(0,1,1))).plus(new Coord3(1,0,0))),
				Coord3.range(position.plus(csm1.times(new Coord3(0,0,1))), position.plus(world.chunkSize)),
				Coord3.range(position.plus(csm1.times(new Coord3(0,1,0))), position.plus(world.chunkSize)),
				Coord3.range(position.plus(csm1.times(new Coord3(1,0,0))), position.plus(world.chunkSize))
		));
		Set<Coord3> meshed = new HashSet<>();
        for(Coord3 blockPos: toMesh){
        	meshed.add(blockPos);
        	BlockType block = data.get(blockPos);
        	for(Direction dir: Direction.values()) {
        		Coord3 blockPos2 = blockPos.plus(dir);
        		BlockType block2 = data.get(blockPos2);
        		if(block.isOpaque) {
        			if(!data.indexWithinBounds(blockPos2) || !block2.isOpaque) {
        				BlockMeshUtil.addFaceMeshData(blockPos, block, mset, dir, .5f+(blockPos.z)/256f);
        			}
        		} else {
            		if(data.indexWithinBounds(blockPos2) && !meshed.contains(blockPos2)) {
            			toMesh.add(blockPos2);
            		}
        		}
        	}
        }
        MeshBuilder.applyMeshSet(mset, getGeometry().getMesh());
        System.out.print(".");
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
