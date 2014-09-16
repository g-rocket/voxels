package voxels;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Cylinder;

/**
 * Created by didyouloseyourdog on 8/10/14.
 */
public class VoxelWorld extends SimpleApplication {
    MaterialLibrarian materialLibrarian;

    @Override
    public void simpleUpdate(float secondsPerFrame) {
    	
    }

    @Override
    public void simpleInitApp() {
        materialLibrarian = new MaterialLibrarian(assetManager);
        setUpTheCam();
        makeADemoMeshAndAdditToTheRootNode();
    }

    private void makeADemoMeshAndAdditToTheRootNode() {
        Mesh m = new Cylinder(12,24,5,11);
        Geometry g = new Geometry("demo geom", m);
        g.setMaterial(materialLibrarian.getBlockMaterial());
        rootNode.attachChild(g);
        attachCoordinateAxes(Vector3f.ZERO);
    }
    
    private void attachCoordinateAxes(Vector3f pos){
		  Arrow arrow = new Arrow(Vector3f.UNIT_X);
		  arrow.setLineWidth(4); // make arrow thicker
		  putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);
		 
		  arrow = new Arrow(Vector3f.UNIT_Y);
		  arrow.setLineWidth(4); // make arrow thicker
		  putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);
		 
		  arrow = new Arrow(Vector3f.UNIT_Z);
		  arrow.setLineWidth(4); // make arrow thicker
		  putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
    }
    	 
	private Geometry putShape(Mesh shape, ColorRGBA color){
	  Geometry g = new Geometry("coordinate axis", shape);
	  Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	  mat.getAdditionalRenderState().setWireframe(true);
	  mat.setColor("Color", color);
	  g.setMaterial(mat);
	  rootNode.attachChild(g);
	  return g;
	}
    
    private void setUpTheCam() {
        flyCam.setMoveSpeed(30);
    }

    /*******************************
     * Program starts here... ******
     *******************************/
    public static void main(String[] args) {
        VoxelWorld app = new VoxelWorld();
        app.start(); // start the game
    }

    public class MaterialLibrarian {
        private Material blockMaterial;
        private AssetManager _assetManager;

        public MaterialLibrarian(AssetManager assetManager_) {
            _assetManager = assetManager_;
        }

        public Material getBlockMaterial() {
            if (blockMaterial == null) {
                Material wireMaterial = new Material(assetManager, "/Common/MatDefs/Misc/Unshaded.j3md");
                wireMaterial.setColor("Color", ColorRGBA.Green);
                wireMaterial.getAdditionalRenderState().setWireframe(true);
                wireMaterial.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
                blockMaterial = wireMaterial;
            }
            return blockMaterial;
        }
    }


}
