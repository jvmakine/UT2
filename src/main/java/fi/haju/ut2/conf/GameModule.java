package fi.haju.ut2.conf;

import com.google.inject.AbstractModule;
import com.jme3.system.AppSettings;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.SphericalFunction;
import fi.haju.ut2.voxels.octree.VoxelOctree;

public class GameModule extends AbstractModule {

  @Override protected void configure() { 
    bind(AppSettings.class).toInstance(appSettings());
    bind(VoxelOctree.class).toInstance(octree());
  }
  
  private VoxelOctree octree() {
    VoxelOctree result = new VoxelOctree(new Position(-8, -8, -8), 16,
        new SphericalFunction(4, new Position(0, 0, 0))
    );
    result.divideAllToLevel(5);
    /*result.divide();
    result.children[4].divide();
    result.children[4].children[3].divide();//*/
    result.calculateComponents();
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
