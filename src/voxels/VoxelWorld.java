package voxels;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import voxels.map.*;
import voxels.meshconstruction.*;

import com.google.common.primitives.Ints;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.*;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;

/**
 * Created by didyouloseyourdog on 8/10/14.
 */
public class VoxelWorld extends SimpleApplication {
    private MaterialLibrarian materialLibrarian;
    private WorldMap world;

    @Override
    public void simpleUpdate(float secondsPerFrame) {
    	
    }

    @Override
    public void simpleInitApp() {
        materialLibrarian = new MaterialLibrarian(assetManager);
        setUpTheCam();
        world = new WorldMap(rootNode, materialLibrarian.getTexturedBlockMaterial());
        for(Coord3 c: Coord3.range(new Coord3(0,0,0), new Coord3(2,2,1))) {
            world.getChunk(c);
        }
        attachCoordinateAxes(Vector3f.ZERO);
    }

    private static void ScreenSettings(VoxelWorld app, boolean fullScreen) {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();
        int SCREEN_MODE=0; // note: there are usually several, let's pick the first
        AppSettings settings = new AppSettings(true);
        float scale_screen = fullScreen ? 1f : .6f;
        Vector2f screenDims = new Vector2f((int)(modes[SCREEN_MODE].getWidth() * scale_screen ),(int)(modes[SCREEN_MODE].getHeight() * scale_screen ));
        settings.setResolution((int)screenDims.x,(int) screenDims.y);
        settings.setFrequency(modes[SCREEN_MODE].getRefreshRate());
        settings.setBitsPerPixel(modes[SCREEN_MODE].getBitDepth());
        if (fullScreen) {
            settings.setFullscreen(device.isFullScreenSupported());
        }
        app.setSettings(settings);
        app.setShowSettings(false);
    }
    
    private void makeADemoMeshAndAdditToTheRootNode() {
        Mesh m = new com.jme3.scene.shape.Torus(50, 50, 13, 20);//Cylinder(12,24,5,11);
        Geometry g = new Geometry("demo geom", m);
        g.setMaterial(materialLibrarian.getBlockMaterial());
        rootNode.attachChild(g);
        Geometry g2 = new Geometry("geom 2",new Sphere(50,50,13));
        g2.setMaterial(materialLibrarian.getBlockMaterial());
        rootNode.attachChild(g2);
        attachCoordinateAxes(Vector3f.ZERO);
    }

    private void addTestBlock() {
    	for(Direction dir: Direction.values()) {
    		System.out.println(dir);
    		MeshSet mset = new MeshSet();
    		BlockMeshUtil.addFaceMeshData(new Coord3(0,0,1), BlockType.GRASS, mset, dir);
    		BlockMeshUtil.addFaceMeshData(new Coord3(0,0,0), BlockType.DIRT, mset, dir);
    		BlockMeshUtil.addFaceMeshData(new Coord3(0,0,-1), BlockType.ROCK, mset, dir);
        	//Mesh testMesh = new Mesh();
    		//ApplyMeshSet(mset, testMesh);
    		//Geometry someGeometry = new Geometry("test geom", testMesh);
    		//someGeometry.setMaterial(materialLibrarian.getBlockMaterial());
    		//rootNode.attachChild(someGeometry);
    		
    		Mesh texturedTestMesh = new Mesh();
    		MeshBuilder.applyMeshSet(mset, texturedTestMesh);
    		Geometry someTexturedGeometry = new Geometry("test geom", texturedTestMesh);
    		someTexturedGeometry.setMaterial(materialLibrarian.getTexturedBlockMaterial());
    		rootNode.attachChild(someTexturedGeometry);
    	}
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
        ScreenSettings(app, false); // setup setting to prevent jme3 settings screen from showing
        app.start(); // start the game
    }

    public class MaterialLibrarian {
        private Material blockMaterial;
		private Material texturedBlockMaterial;
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
        
        public Material getTexturedBlockMaterial() {
            if (texturedBlockMaterial == null) {
                Material mat = new Material(_assetManager, "BlockTex2.j3md");
                Texture blockTex = _assetManager.loadTexture("dog_64d_.jpg");
                blockTex.setMagFilter(Texture.MagFilter.Nearest);
                blockTex.setWrap(Texture.WrapMode.Repeat);
                mat.setTexture("ColorMap", blockTex);
                texturedBlockMaterial = mat;
            }
            return texturedBlockMaterial;
        }
    }


}
