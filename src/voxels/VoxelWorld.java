package voxels;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import voxels.block.*;
import voxels.block.texture.*;
import voxels.map.*;
import voxels.meshconstruction.*;

import com.jme3.app.*;
import com.jme3.asset.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.debug.*;
import com.jme3.scene.shape.*;
import com.jme3.system.*;
import com.jme3.texture.*;


public class VoxelWorld extends SimpleApplication {
	private MaterialLibrarian materialLibrarian;
	private WorldMap world;
	private ExecutorService renderThreadExecutor = new ExecutorService() {
		class CallableRunnable<T> implements Callable<T>{
			private final T result;
			private final Runnable task;
			
			public CallableRunnable(Runnable task, T result) {
				this.task = task;
				this.result = result;
			}
			
			@Override
			public T call() throws Exception {
				task.run();
				return result;
			}
			
		}
		
		@Override
		public void execute(Runnable task) {
			VoxelWorld.this.enqueue(new CallableRunnable<Object>(task, null));
		}
		
		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			return VoxelWorld.this.enqueue(new CallableRunnable<T>(task, result));
		}
		
		@Override
		public Future<?> submit(Runnable task) {
			return VoxelWorld.this.enqueue(new CallableRunnable<Object>(task, null));
		}
		
		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return VoxelWorld.this.enqueue(task);
		}
		
		@Override
		public List<Runnable> shutdownNow() {
			throw new UnsupportedOperationException("this synthetic executor doesn't know how to shut down");
		}
		
		@Override
		public void shutdown() {
			throw new UnsupportedOperationException("this synthetic executor doesn't know how to shut down");
		}
		
		@Override
		public boolean isTerminated() {
			return false;
		}
		
		@Override
		public boolean isShutdown() {
			return false;
		}
		
		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
				long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			throw new UnsupportedOperationException("this synthetic executor doesn't support timeouts");
		}
		
		/**
		 * just runs the first task
		 */
		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			if(tasks.isEmpty()) return null;
			return submit(tasks.iterator().next()).get();
		}
		
		@Override
		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException {
			throw new UnsupportedOperationException("this synthetic executor doesn't support timeouts");
		}
		
		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
				throws InterruptedException {
			List<Future<T>> futures = new ArrayList<>(tasks.size());
			for(Callable<T> task: tasks) {
				futures.add(VoxelWorld.this.enqueue(task));
			}
			return futures;
		}
		
		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit)
				throws InterruptedException {
			throw new UnsupportedOperationException("this synthetic executor doesn't know how to shut down");
		}
	};
	
	
    @Override
    public void simpleUpdate(float secondsPerFrame) {
    	Coord3 cameraPos = new Coord3(cam.getLocation());
    	//System.out.println("The camera is at "+cam.getLocation());
    	//System.out.println("We resolved it as "+cameraPos);
    	//System.out.println("This maps to the chunk at "+cameraPos.divBy(world.chunkSize));
    	world.loadChunksAroundCamera(cameraPos);
    }

    @Override
    public void simpleInitApp() {
    	viewPort.setBackgroundColor(ColorRGBA.Blue);
        materialLibrarian = new MaterialLibrarian(assetManager);
        setUpTheCam();
		String worldFile = System.getProperty("user.home")+System.getProperty("file.separator")+
				"voxelWorld"+System.getProperty("file.separator")+new Random().nextLong()+".zip";
		world = new WorldMap(rootNode, materialLibrarian.getTexturedBlockMaterial(), new File(worldFile), renderThreadExecutor);
        cam.setLocation(new Coord3(0,0,0).asVector());
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
			BlockMeshUtil.addFaceMeshData(new Coord3(0,0,1), BlockType.GRASS, mset, dir, (float)Math.random());
			BlockMeshUtil.addFaceMeshData(new Coord3(0,0,0), BlockType.DIRT, mset, dir, 1);
			BlockMeshUtil.addFaceMeshData(new Coord3(0,0,-1), BlockType.ROCK, mset, dir, .5f);
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
