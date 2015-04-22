package voxels;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import voxels.entity.*;
import voxels.map.*;

import com.jme3.app.state.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.scene.*;

public class World {
	public final WorldMap map;
	private List<Player> players = new ArrayList<>();
	private List<Entity> entities = new ArrayList<>();
	private Vector3f gravity;

    public void simpleUpdate(float secondsPerFrame) {
    	for(Player p: players) {
    		map.loadChunksAroundCamera(p.getBlockLocation());
    	}
    	updatePhysics(secondsPerFrame);
    }
	
    /**
     * Update the physics for a specific time-interval
     * @param dt The elapsed time in seconds
     */
	private void updatePhysics(float dt) {
		Vector3f force = new Vector3f();
		for(Entity e: entities) {
			force.set(0,0,0);
			if(!onGround(e)) force.add(gravity);
			// add any other relevant forces
			e.applyForce(force);
			
			Vector3f velocity = e.getVelocity();
			Vector3f newLoc = e.getNextLocation(dt);
			//TODO: Don't move into blocks
			//TODO: Zero the velocity in directions that we are hitting blocks
			e.setLocation(newLoc);
		}
	}

	private boolean onGround(Entity e) {
		if(e.getLocation().getZ() != (int)e.getLocation().getZ()) return false;
		return map.getBlock(new Coord3(e.getLocation().subtract(0, 0, 1))).isSolid;
	}

	public World(AppStateManager parentASM, Node worldNode, Material blockMaterial, File saveFile, Executor renderThreadExecutor) {
		this.map = new WorldMap(
			(g) -> {
				worldNode.attachChild(g);
			}, (g) -> {
				//physics.getPhysicsSpace().addCollisionObject(new RigidBodyControl(CollisionShapeFactory.createMeshShape(g), 0));
			}, (c) -> {
				c.setEnabled(false);
				c.getSpatial().removeFromParent();
			}, () -> {
				worldNode.removeFromParent();
			}, blockMaterial, saveFile, renderThreadExecutor, () -> {
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
			}
		);
		gravity = new Vector3f(0,0,-10);
	}

	public void addPlayer(Player player) {
		addEntity(player);
		players.add(player);
	}
	
	public void addEntity(Entity entity) {
		entities.add(entity);
	}
}
