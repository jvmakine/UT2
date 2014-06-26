package fi.haju.ut2.ui;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

import fi.haju.ut2.ui.render.modules.SimpleLightingModule;
import fi.haju.ut2.ui.render.renderers.OctreeRenderer;
import fi.haju.ut2.ui.render.renderers.OctreeVertexRenderer;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class Game extends SimpleApplication {

  @Inject private VoxelOctree octree;
  @Inject private SimpleLightingModule lightingModule;
  @Inject private OctreeRenderer octreeRenderer;
  @Inject private OctreeVertexRenderer octreeVertexRenderer;
  
  @Inject public Game(AppSettings appSettings) {
    setShowSettings(false);
    setSettings(appSettings);
  }
  
  @Override public void simpleInitApp() {
    lightingModule.setup(rootNode, assetManager, viewPort);
    octreeRenderer.setup(rootNode, assetManager);
    octreeVertexRenderer.setup(rootNode, assetManager);
    octreeVertexRenderer.render(octree);
    //octreeRenderer.render(octree);
  }

}
