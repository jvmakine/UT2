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
    List<Geometry> toBeDetached = null;
    if (geometryMap.containsKey(mesh.octree)) {
      toBeDetached = geometryMap.get(mesh.octree).geometries;
    }
    geometryMap.put(mesh.octree, mesh);
    for(Geometry geometry : mesh.geometries) {
      game.addGeometry(geometry);
    }
    if (toBeDetached != null) {
      for(Geometry geometry : toBeDetached) {
        game.removeGeometry(geometry);
      } 
    }
  }

  private List<Geometry> generate(VoxelOctree octree, int renderLevel) {
    octree.compress();
    octree.calculateComponents();
    List<Geometry> geometries = Lists.newArrayList(); 
    geometries.addAll(octreeSurfaceMeshGenerator.generate(renderLevel, octree, assetManager));
    return geometries;
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
          Position pos = focus;
          updateLod(4,pos);
          for (Position p : pos.centeredEmptyCube(d, 1)) { updateLod(4, p); }
          for (Position p : pos.centeredEmptyCube(d, 2)) { updateLod(3, p); }
          for (Position p : pos.centeredEmptyCube(d, 3)) { updateLod(3, p); }
          for (Position p : pos.centeredEmptyCube(d, 4)) { updateLod(2, p); }
          for (Position p : pos.centeredEmptyCube(d, 5)) { updateLod(2, p); }
          for (Position p : pos.centeredEmptyCube(d, 6)) { updateLod(2, p); }
        }
      }
    });
    updater.start();
  }
  
  private void updateLod(int level, Position p) {
    VoxelOctree tree = octree.getOctreeAtPosition(p, 0);
    OctreeMesh old = geometryMap.get(tree);
    if (old != null && old.renderLevel != level) {
      old = null;
    }
    if (old == null) {
      tree.divideAllToLevel(level);
      updateLowerLevelNeighbours(level, tree);
      OctreeMesh m = new OctreeMesh(tree, generate(tree, level), level);
      attach(m);
    }
  }

  private void updateLowerLevelNeighbours(int level, VoxelOctree tree) {
    for (VoxelOctree n : tree.neighbours()) {
      if (n == null) continue;
      OctreeMesh m = geometryMap.get(n);
      if (m != null && m.renderLevel < level) {
        OctreeMesh newMesh = new OctreeMesh(n, generate(n, m.renderLevel), m.renderLevel);
        attach(newMesh);
      }
    }
  }
  
  public void stop() {
    updaterRunning = false;
    updater = null;
  }
  
}
