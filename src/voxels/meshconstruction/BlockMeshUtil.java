package voxels.meshconstruction;

import com.jme3.math.Vector3f;

import voxels.map.Coord3;
import voxels.map.Direction;

public class BlockMeshUtil {
	
	/*
	 * Make four verts,
	 * 6 indices and 4 UV vector2s
	 * add them to mesh Set
	 */
	public static void AddFaceMeshData(Coord3 pos, MeshSet mset, Direction direction, int triIndexStart)
	{
		addFaceVerticesToMesh(mset, pos, direction);
		UVsForDirection(mset, direction);
		IndicesForDirection(mset, triIndexStart);
	}

	private static void addFaceVerticesToMesh(MeshSet mset, Coord3 pos, Direction direction) {
		for(Vector3f corner: direction.getCorners(pos)) mset.vertices.add(corner);
	}
	
}
