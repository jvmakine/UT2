package fi.haju.ut2.ui;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

import fi.haju.ut2.ui.render.OctreeManager;
import fi.haju.ut2.ui.render.modules.SimpleLightingModule;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class Game extends SimpleApplication {

  @Inject private SimpleLightingModule lightingModule;
  @Inject private OctreeManager octreeRenderManager;
  @Inject private VoxelOctree octree;
  
  @Inject public Game(AppSettings appSettings) {
    setShowSettings(false);
    setSettings(appSettings);
  }
  
  @Override public void simpleInitApp() {
    lightingModule.setup(rootNode, assetManager, viewPort);
    octreeRenderManager.setup(rootNode, assetManager);
    octreeRenderManager.render(octree);
  }

}
