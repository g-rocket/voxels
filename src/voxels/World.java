package voxels;

import java.util.*;

import voxels.entity.*;
import voxels.map.*;

import com.jme3.app.state.*;
import com.jme3.bullet.*;
import com.jme3.bullet.control.*;

public class World {
	private final WorldMap map;
	private final BulletAppState physics;
	private List<Player> players = new ArrayList<>();

    public void simpleUpdate(float secondsPerFrame) {
    	for(Player p: players) {
    		map.loadChunksAroundCamera(p.getBlockLocation());
    	}
    }
	
	public World(WorldMap map, AppStateManager parentASM) {
		this.map = map;
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
