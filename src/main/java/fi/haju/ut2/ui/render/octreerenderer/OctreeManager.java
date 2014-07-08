package fi.haju.ut2.ui.render.octreerenderer;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.ui.Game;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeManager {
  
  @Inject private OctreeSurfaceGeometryGenerator octreeSurfaceMeshGenerator;
  @Inject private OctreeFaceSegmentGeometryGenerator octreeFaceSegmentGeometryGenerator;
  @Inject private VoxelOctree octree;
  @Inject private Game game;
  
  private AssetManager assetManager;
  private ConcurrentMap<VoxelOctree, OctreeMesh> geometryMap = Maps.newConcurrentMap();
  private Position focus = null;
  private Thread updater;
  private boolean updaterRunning = false;
  
  public void updateFocusPosition(double x, double y, double z) {
    focus = new Position(x, y, z); 
  }

  private void attach(OctreeMesh mesh) {
    geometryMap.put(mesh.octree, mesh);
    for(Geometry geometry : mesh.geometries) {
      game.addGeometry(geometry);
    }
  }

  private List<Geometry> generate(VoxelOctree octree) {
    List<Geometry> geometries = Lists.newArrayList(); 
    geometries.addAll(octreeSurfaceMeshGenerator.generate(octree, assetManager));
    geometries.addAll(octreeFaceSegmentGeometryGenerator.generate(octree, assetManager));
    return geometries;
  }

  private void detach(VoxelOctree octree) {
    OctreeMesh oldMesh = geometryMap.get(octree);
    geometryMap.remove(octree);
    if (oldMesh != null) {
      for (Geometry geometry : oldMesh.geometries) {
        game.removeGeometry(geometry);
      }
    }
  }
  
  public void setup(AssetManager assetManager) {
    this.assetManager = assetManager;
  }
  
  public void start() {
    updaterRunning = true;
    updater = new Thread(new Runnable() {
      @Override
      public void run() {
        double d = octree.edgeLength();
        while (updaterRunning) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          if (focus == null) continue;
          List<Position> positions = Lists.newArrayList();
          Position pos = focus;
          positions.add(pos);
          positions.addAll(pos.centeredEmptyCube(d));
          for (Position p : positions) {
            VoxelOctree tree = octree.getOctreeAtPosition(p, 0);
            OctreeMesh old = geometryMap.get(tree);
            if (old != null && old.renderLevel != 5) {
              detach(tree);
              old = null;
            }
            if (old == null) {
              tree.divideAllToLevel(5);
              OctreeMesh m = new OctreeMesh(tree, generate(tree), 5);
              attach(m);
            }
          }
        }
      }
    });
    updater.start();
  }
  
  public void stop() {
    updaterRunning = false;
    updater = null;
  }
  
}
