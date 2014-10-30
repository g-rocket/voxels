package voxels.meshconstruction;

import java.util.*;

import com.jme3.math.*;

import voxels.map.*;

public class BlockMeshUtil {
	
	private static final int[] faceIndices = new int[] {0,3,2, 0,2,1};

	/*
	 * Make four verts,
	 * 6 indices and 4 UV vector2s
	 * add them to mesh Set
	 */
	public static void AddFaceMeshData(Coord3 pos, BlockType block, MeshSet mset, Direction direction, int triIndexStart) {
		addFaceVerticesToMesh(mset, pos, direction);
		addUVsForDirection(mset, block.getTexture(direction), direction);
		addIndicesForDirection(mset, triIndexStart);
	}
	
	private static void addIndicesForDirection(MeshSet mset, int triIndexStart) {
        for (int i : faceIndices) {
            mset.indices.add(i + triIndexStart);
        }
    }

	private static void addUVsForDirection(MeshSet mset, Vector2f[] uvs, Direction direction) {
		//Vector2f offsetStart = new Vector2f(.25f*(int)(Math.random()*4),.25f*(int)(Math.random()*4));
        mset.uvs.addAll(Arrays.asList(uvs));
	}

	private static void addFaceVerticesToMesh(MeshSet mset, Coord3 pos, Direction direction) {
		for(Vector3f corner: direction.getCorners(pos)) mset.vertices.add(corner);
	}
	
}
