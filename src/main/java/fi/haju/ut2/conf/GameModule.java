package fi.haju.ut2.conf;

import com.google.inject.AbstractModule;
import com.jme3.system.AppSettings;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.perlinnoise.PerlinTerrainFunction;
import fi.haju.ut2.voxels.octree.VoxelOctree;

public class GameModule extends AbstractModule {

  @Override protected void configure() { 
    bind(AppSettings.class).toInstance(appSettings());
    bind(VoxelOctree.class).toInstance(octree());
  }
  
  private VoxelOctree octree() {
    VoxelOctree result = new VoxelOctree(new Position(-4, -4, -4), 8,
        new PerlinTerrainFunction()
    );
    //result.constructFromMeshToLevel(new Sphere(10, 10, 3), new Vector3f(0,0,0), new Quaternion(0, 0, 0, 0), 4, new Position(0,0,0), 3);
    return result;
  }

  private AppSettings appSettings() {
    AppSettings settings = new AppSettings(true);
    settings.setVSync(true);
    settings.setAudioRenderer(null);
    settings.setFullscreen(false);
    settings.setResolution(1800, 1000);
    return settings;
  }

}
