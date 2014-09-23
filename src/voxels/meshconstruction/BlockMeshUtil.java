package voxels.meshconstruction;

import java.util.*;

import com.jme3.math.*;

import voxels.map.Coord3;
import voxels.map.Direction;

public class BlockMeshUtil {
	private static List<Vector2f> uvs = Arrays.asList(
			new Vector2f(0,0),
			new Vector2f(0,.25f),
			new Vector2f(.25f,.25f),
			new Vector2f(.25f,0)
	);
	
	private static final int[] faceIndices = new int[] {0,3,2, 0,2,1};

	/*
	 * Make four verts,
	 * 6 indices and 4 UV vector2s
	 * add them to mesh Set
	 */
	public static void AddFaceMeshData(Coord3 pos, MeshSet mset, Direction direction, int triIndexStart)
	{
		addFaceVerticesToMesh(mset, pos, direction);
		addUVsForDirection(mset, direction);
		addIndicesForDirection(mset, triIndexStart);
	}
	
	private static void addIndicesForDirection(MeshSet mset, int triIndexStart) {
        for (int i : faceIndices) {
            mset.indices.add(i + triIndexStart);
        }
    }

	private static void addUVsForDirection(MeshSet mset, Direction direction) {
		mset.uvs.addAll(uvs);
	}

	private static void addFaceVerticesToMesh(MeshSet mset, Coord3 pos, Direction direction) {
		for(Vector3f corner: direction.getCorners(pos)) mset.vertices.add(corner);
	}
	
}
