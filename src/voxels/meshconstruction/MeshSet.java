package voxels.meshconstruction;

import java.util.ArrayList;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class MeshSet {
    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Integer> indices = new ArrayList<Integer>();
    public ArrayList<Vector2f> uvs = new ArrayList<Vector2f>();
    public ArrayList<Vector2f> texMapOffsets = new ArrayList<Vector2f>();
    public ArrayList<Float> colors = new ArrayList<Float>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public int triIndex = 0;
}

