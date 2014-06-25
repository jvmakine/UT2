package fi.haju.ut2.ui;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

import fi.haju.ut2.ui.render.modules.SimpleLightingModule;
import fi.haju.ut2.ui.render.renderers.OctreeRenderer;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class Game extends SimpleApplication {

  private VoxelOctree octree;
  private SimpleLightingModule lightingModule;
  private OctreeRenderer octreeRenderer;
  
  @Inject public Game(AppSettings appSettings, VoxelOctree octree, SimpleLightingModule lightModule, OctreeRenderer octreeRenderer) {
    setShowSettings(false);
    setSettings(appSettings);
    this.octree = octree;
    this.lightingModule = lightModule;
    this.octreeRenderer = octreeRenderer;
  }
  
  @Override public void simpleInitApp() {
    lightingModule.setup(rootNode, assetManager, viewPort);
    octreeRenderer.setup(rootNode, assetManager);
    octreeRenderer.render(octree);
  }

}
