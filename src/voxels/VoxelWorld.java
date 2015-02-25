package voxels;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import voxels.entity.*;
import voxels.map.*;

import com.jme3.app.*;
import com.jme3.asset.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.system.*;
import com.jme3.texture.*;


public class VoxelWorld extends SimpleApplication {
	private MaterialLibrarian materialLibrarian;
	private World world;
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
    	world.simpleUpdate(secondsPerFrame);
    }

    @Override
    public void simpleInitApp() {
    	viewPort.setBackgroundColor(ColorRGBA.Blue);
        materialLibrarian = new MaterialLibrarian(assetManager);
        setUpTheCam();
		String worldFile = System.getProperty("user.home")+System.getProperty("file.separator")+
				"voxelWorld"+System.getProperty("file.separator")+new Random().nextLong()+".zip";
		world = new World(getStateManager(),
					rootNode, materialLibrarian.getTexturedBlockMaterial(), new File(worldFile), renderThreadExecutor);
		//world.addPlayer(new MainPlayer(cam));
		world.addPlayer(new FakePlayer(cam));
        //cam.setLocation(new Coord3(0,0,0).asVector());
	}
    
    private void setUpKeys(InputListener player) {
      inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
      inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
      inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
      inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
      inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
      inputManager.addListener(player, "Left");
      inputManager.addListener(player, "Right");
      inputManager.addListener(player, "Up");
      inputManager.addListener(player, "Down");
      inputManager.addListener(player, "Jump");
    }
    
	private static void ScreenSettings(VoxelWorld app, boolean fullScreen) {
		GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		GraphicsDevice biggestScreen = null;
		int maxPixelsSoFar = 0;
		for(GraphicsDevice screen: screens){ // find biggest screen
			DisplayMode d = screen.getDisplayMode();
			if(d.getWidth() * d.getHeight() > maxPixelsSoFar && (!fullScreen || screen.isFullScreenSupported())){
				biggestScreen = screen;
				maxPixelsSoFar = d.getWidth() * d.getHeight();
			}
		}
		DisplayMode d = biggestScreen.getDisplayMode();
		AppSettings settings = new AppSettings(true);
		float scale_screen = fullScreen ? 1f : .8f;
		Vector2f screenDims = new Vector2f((int)(d.getWidth() * scale_screen ), (int)(d.getHeight() * scale_screen ));
		settings.setResolution((int)screenDims.x,(int) screenDims.y);
		settings.setFrequency(d.getRefreshRate());
		settings.setBitsPerPixel(d.getBitDepth());
		if (fullScreen) {
			settings.setFullscreen(biggestScreen.isFullScreenSupported());
		}
		app.setSettings(settings);
		app.setShowSettings(false);
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
