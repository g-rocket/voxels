package voxels;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import voxels.block.texture.*;
import voxels.entity.*;
import voxels.map.*;

import com.google.auto.value.*;
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
			if(!onGround(e)) {
				force.addLocal(gravity);
				e.setAtBlockSurfaceHeight(false);
			}
			// add any other relevant forces
			e.applyForce(force);
			
			Vector3f velocity = e.getVelocity();
			Vector3f newLoc = e.getNextLocation(dt);
			Coord3 newBlock = new Coord3(newLoc);
			
			if(map.getBlock(newBlock).isSolid) {
				//TODO: what if we moved through a block?
				FaceIntersectionPoint intersectionWithBlock = getIntersectionWithBlock(e.getLocation(), velocity, newLoc.clone());
				System.out.println("collided with "+newBlock+" at "+intersectionWithBlock);
				if(intersectionWithBlock != null) {
					newLoc = intersectionWithBlock.intersectionPoint();
					//TODO: zero velocity in the direction we hit
					if(intersectionWithBlock.face().equals(Direction.ZPOS)) e.setAtBlockSurfaceHeight(true);
				}
			}
			
			e.setLocation(newLoc);
			System.out.println("at "+newLoc);
		}
	}
	
	private boolean onGround(Entity e) {
		if(!e.atBlockSurfaceHeight()) return false;
		return map.getBlock(new Coord3(e.getLocation().subtract(0, 0, 1))).isSolid;
	}
	
	private FaceIntersectionPoint getIntersectionWithBlock(Vector3f origin, Vector3f vector, Vector3f block) {
		block.x = (float)Math.floor(block.x);
		block.y = (float)Math.floor(block.y);
		block.z = (float)Math.floor(block.z);
		Direction[] facesToCheck = new Direction[3];
		int i = 0;
		if(vector.x < 0) {
			facesToCheck[i++] = Direction.XPOS;
		} else if(vector.x > 0) {
			facesToCheck[i++] = Direction.XNEG;
		}
		if(vector.y < 0) {
			facesToCheck[i++] = Direction.YPOS;
		} else if(vector.y > 0) {
			facesToCheck[i++] = Direction.YNEG;
		}
		if(vector.z < 0) {
			facesToCheck[i++] = Direction.ZPOS;
		} else if(vector.z > 0) {
			facesToCheck[i++] = Direction.ZNEG;
		}
		for(Direction face: facesToCheck) {
			if(face == null) return null;
			Vector3f intersectionWithFace = getIntersectionWithFace(origin, vector, block, face);
			if(intersectionWithFace != null) return FaceIntersectionPoint.create(face, intersectionWithFace);
		}
		return null;
	}
	
	@AutoValue
	abstract static class FaceIntersectionPoint {
		public abstract Direction face();
		public abstract Vector3f intersectionPoint();
		public static FaceIntersectionPoint create(Direction face, Vector3f intersectionPoint) {
			return new AutoValue_World_FaceIntersectionPoint(face, intersectionPoint);
		}
	}
	
	private Vector3f getIntersectionWithFace(Vector3f origin, Vector3f vector, Vector3f block, Direction face) {
		System.out.printf("colliding with %s\n", block);
		System.out.printf("face at %s\n", block.add(face.offset));
		System.out.printf("origin at %s\n", origin);
		origin = origin.subtract(block.add(face.offset));
		System.out.printf("origin translated to %s\n", origin);
		
		Vector3f retval = origin.subtract(vector.mult(face.getPrimaryComponent(origin) / face.getPrimaryComponent(vector)));
		
		Vector2f faceIntersectLocation = face.getSecondaryComponents(retval);
		System.out.printf("collided at %s, %.3f\n", faceIntersectLocation, face.getPrimaryComponent(retval));
		if(faceIntersectLocation.x >= 1 || faceIntersectLocation.y >= 1 ||
		   faceIntersectLocation.x <  0 || faceIntersectLocation.y <  0) {
			return null;
		}
		
		retval.addLocal(block.add(face.offset));
		
		float lengthSquared = origin.subtractLocal(retval).lengthSquared();
		if(lengthSquared < 0 || lengthSquared > vector.lengthSquared()) return null;
		
		return retval;
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
		gravity = new Vector3f(0,0,-1);
	}

	public void addPlayer(Player player) {
		addEntity(player);
		players.add(player);
	}
	
	public void addEntity(Entity entity) {
		entities.add(entity);
	}
}
