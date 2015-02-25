package voxels;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import voxels.entity.*;
import voxels.map.*;

import com.jme3.app.state.*;
import com.jme3.bullet.*;
import com.jme3.bullet.control.*;
import com.jme3.material.*;
import com.jme3.scene.*;

public class World {
	private final WorldMap map;
	private final BulletAppState physics;
	private List<Player> players = new ArrayList<>();

    public void simpleUpdate(float secondsPerFrame) {
    	for(Player p: players) {
    		map.loadChunksAroundCamera(p.getBlockLocation());
    	}
    }
	
	public World(AppStateManager parentASM, Node worldNode, Material blockMaterial, File saveFile, Executor renderThreadExecutor) {
		this.map = new WorldMap(worldNode, blockMaterial, saveFile, renderThreadExecutor, () -> {
			return new AbstractList<Coord3>() {
				@Override
				public Coord3 get(int index) {
					return new Coord3(players.get(index).getLocation());
				}

				@Override
				public int size() {
					return players.size();
				}
			};
		});
		physics = new BulletAppState();
		parentASM.attach(physics);
	}

	public void addPlayer(Player player) {
		players.add(player);
		if(player instanceof PhysicsControl) {
			physics.getPhysicsSpace().add(player);
		}
		if(player instanceof PhysicsTickListener) {
			physics.getPhysicsSpace().addTickListener((PhysicsTickListener)player);
		}
	}
}
