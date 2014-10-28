package voxels.meshconstruction;

import voxels.generate.*;
import voxels.map.*;

import com.google.common.primitives.*;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.VertexBuffer.*;
import com.jme3.util.*;

public class MeshBuilder {
    
    public static void applyMeshSet(MeshSet mset, Mesh bigMesh) {
        if (bigMesh == null) {
            throw new NullPointerException("bigMesh can't be null");
        }
        
        bigMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(mset.vertices.toArray(new Vector3f[0])));
        bigMesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(mset.uvs.toArray(new Vector2f[0])));
 
        /* google guava library helps with turning Lists into primitive arrays
        * "Ints" and "Floats" are guava classes.
        * */ 
        bigMesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(Ints.toArray(mset.indices)));
    }
}
