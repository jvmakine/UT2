package fi.haju.ut2.ui.render.modules;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

import fi.haju.ut2.ui.MeshUtils;
import fi.haju.ut2.ui.input.InputController;
import fi.haju.ut2.ui.render.octree.OctreeManager;

@Singleton
public class EditModule {

  @Inject private OctreeManager octreeManager;
  
  private Geometry editObject;
  private Node rootNode;
  private Vector3f editPoint = null;
  
  public void setup(Node rootNode, AssetManager assetManager, InputManager inputManager) {
    this.rootNode = rootNode;
    editObject = MeshUtils.makeSimpleMesh(
        new Sphere(6, 6, 0.2f),
        new ColorRGBA(0.4f, 0.7f, 0.3f, 1.0f), assetManager);
    editObject.setModelBound(new BoundingSphere());
    
    inputManager.addListener(new ActionListener() {
      @Override public void onAction(String name, boolean isPressed, float tpf) {
        if (editPoint != null && isPressed) {
          Vector3f location = editObject.getLocalTranslation();
          Quaternion rot = editObject.getLocalRotation();
          octreeManager.addMeshAt(location, rot, new Sphere(30, 30, 2.0f));
        }
      }
    }, InputController.EDIT_ADD);
    
  }
  
  public void update(double tpf, Camera camera) {
    editPoint = calculateEditPoint(camera);
    if (editPoint != null && editPoint.distance(camera.getLocation()) < 10) {
      rootNode.attachChild(editObject);
      editObject.setLocalTranslation(editPoint);
    } else {
      rootNode.detachChild(editObject);
    }
  }
  
  private Vector3f calculateEditPoint(Camera camera) {
    List<Geometry> geometries = octreeManager.getClosestSpatials();
    Vector3f location = camera.getLocation();
    Ray ray = new Ray(location, camera.getDirection());
    Vector3f result = null;
    for (Geometry g : geometries) {
      CollisionResults collision = new CollisionResults();
      if (g.collideWith(ray, collision) != 0) {
        Vector3f closest = collision.getClosestCollision().getContactPoint();
        if (result == null || result.distance(location) > closest.distance(location)) {
          result = closest;
        }
      }
    }
    return result;
  }
  
}
