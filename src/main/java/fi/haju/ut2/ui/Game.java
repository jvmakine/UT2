package fi.haju.ut2.ui;


import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;

import fi.haju.ut2.ui.input.InputController;
import fi.haju.ut2.ui.render.modules.EditModule;
import fi.haju.ut2.ui.render.modules.SimpleLightingModule;
import fi.haju.ut2.ui.render.octree.OctreeManager;

@Singleton
public class Game extends SimpleApplication {

  @Inject private SimpleLightingModule lightingModule;
  @Inject private OctreeManager octreeRenderManager;
  @Inject private InputController inputController;
  @Inject private EditModule editModule;
  
  private Object geometryLock = new Object();
  private List<Geometry> toBeAdded = Lists.newArrayList();
  private List<Geometry> toBeDeleted = Lists.newArrayList();
  
  @Inject public Game(AppSettings appSettings) {
    setShowSettings(false);
    setSettings(appSettings);
  }
  
  @Override public void simpleInitApp() {
    lightingModule.setup(rootNode, assetManager, viewPort);
    octreeRenderManager.setup(assetManager);
    octreeRenderManager.start();
    getFlyByCamera().setMoveSpeed(5);
    inputController.setup(inputManager);
    editModule.setup(rootNode, assetManager, inputManager);
  }
  
  @Override public void simpleUpdate(float tpf) {
    Vector3f loc = getCamera().getLocation();
    octreeRenderManager.updateFocusPosition(loc.x, loc.y, loc.z);
    updateGeometries();
    editModule.update(tpf, getCamera());
  }

  private void updateGeometries() {
    synchronized(geometryLock) {
      for (Geometry g : toBeAdded) {
        rootNode.attachChild(g);
      }
      for (Geometry g : toBeDeleted) {
        rootNode.detachChild(g);
      }
      toBeAdded.clear();
      toBeDeleted.clear();
    }
  }
  
  public void addGeometry(Geometry g) {
    synchronized(geometryLock) {
      toBeAdded.add(g);
    }
  }
  
  public void removeGeometry(Geometry g) {
    synchronized(geometryLock) {
      toBeDeleted.add(g);
    }
  }
  
  @Override public void destroy() {
    super.destroy();
    octreeRenderManager.stop();
  }

}
