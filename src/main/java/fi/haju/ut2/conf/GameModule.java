package fi.haju.ut2.conf;

import com.google.inject.AbstractModule;
import com.jme3.system.AppSettings;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.octree.VoxelOctree;

public class GameModule extends AbstractModule {

  @Override protected void configure() { 
    bind(AppSettings.class).toInstance(appSettings());
    bind(VoxelOctree.class).toInstance(octree());
  }
  
  private VoxelOctree octree() {
    VoxelOctree result = new VoxelOctree(new Position(-2, -2, -2), 4);
    result.divide();
    result.children[0].divide();
    return result;
  }

  private AppSettings appSettings() {
    AppSettings settings = new AppSettings(true);
    settings.setVSync(true);
    settings.setAudioRenderer(null);
    settings.setFullscreen(false);
    settings.setResolution(800, 600);
    return settings;
  }

}
