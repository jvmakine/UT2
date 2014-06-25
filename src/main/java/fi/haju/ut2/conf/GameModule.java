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
    VoxelOctree result = new VoxelOctree(new Position(-1, -1, -1), 2);
    result.divide();
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
