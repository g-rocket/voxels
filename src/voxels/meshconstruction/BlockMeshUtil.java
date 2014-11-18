package voxels.meshconstruction;

import java.awt.*;
import java.util.*;

import com.google.common.primitives.*;
import com.jme3.math.*;

import voxels.block.*;
import voxels.block.texture.*;
import voxels.map.*;

public class BlockMeshUtil {
	
	private static final int[] faceIndices = new int[] {0,3,2, 0,2,1};
	private static final Float[] shadowHolder = new Float[16];

	/*
	 * Make four verts,
	 * 6 indices and 4 UV vector2s
	 * add them to mesh Set
	 */
	public static void addFaceMeshData(Coord3 pos, BlockType block, MeshSet mset, Direction direction, float shadow) {
		addFaceVerticesToMesh(mset, pos, direction);
		addUVsForDirection(mset, block.getTexture(direction), direction);
		addIndicesForDirection(mset);
		addShadow(mset, shadow);
	}
	
	private static void addShadow(MeshSet mset, float shadow) {
		Arrays.fill(shadowHolder, shadow);
		mset.colors.addAll(Arrays.asList(shadowHolder));
	}

	private static void addIndicesForDirection(MeshSet mset) {
        for (int i: faceIndices) {
            mset.indices.add(i + mset.triIndex);
        }
        mset.triIndex += 4;
    }

	private static void addUVsForDirection(MeshSet mset, Vector2f[] uvs, Direction direction) {
		//Vector2f offsetStart = new Vector2f(.25f*(int)(Math.random()*4),.25f*(int)(Math.random()*4));
        mset.uvs.addAll(Arrays.asList(uvs));
	}

	private static void addFaceVerticesToMesh(MeshSet mset, Coord3 pos, Direction direction) {
		for(Vector3f corner: direction.getCorners(pos)) mset.vertices.add(corner);
	}
	
}
