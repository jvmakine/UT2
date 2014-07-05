package fi.haju.ut2.ui;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

import fi.haju.ut2.ui.render.OctreeManager;
import fi.haju.ut2.ui.render.modules.SimpleLightingModule;

@Singleton
public class Game extends SimpleApplication {

  @Inject private SimpleLightingModule lightingModule;
  @Inject private OctreeManager octreeRenderManager;
  
  @Inject public Game(AppSettings appSettings) {
    setShowSettings(false);
    setSettings(appSettings);
  }
  
  @Override public void simpleInitApp() {
    lightingModule.setup(rootNode, assetManager, viewPort);
    octreeRenderManager.setup(rootNode, assetManager);
  }

  @Override public void simpleUpdate(float tpf) {
    Vector3f loc = cam.getLocation();
    octreeRenderManager.updateFocusPosition(loc.x, loc.y, loc.z);
  }
  
  

}
