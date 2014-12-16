package voxels;

import java.util.*;

import com.jme3.app.state.*;
import com.jme3.bullet.*;

import voxels.map.*;
import voxels.entity.*;

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
	}
}
