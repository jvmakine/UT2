package fi.haju.ut2.ui.render.octree;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.ui.Game;
import fi.haju.ut2.voxels.octree.VoxelOctree;
import static fi.haju.ut2.geometry.Position.distance;

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
  private List<Geometry> closestSpatials = Lists.newArrayList();
  private List<OctreeEdit> incomingEdits = Lists.newArrayList();
  
  private Object editLock = new Object();
  
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
  
  public void detach(VoxelOctree octree) {
    if (geometryMap.containsKey(octree)) {
      for(Geometry geometry : geometryMap.get(octree).geometries) {
        game.removeGeometry(geometry);
      }
    }
    geometryMap.remove(octree);
  }

  public List<Geometry> generate(VoxelOctree octree, int renderLevel) {
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
        while (updaterRunning) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          if (focus == null) continue;
          Position pos = focus;
          updateGeometries(pos);
        }
      }
    });
    updater.start();
  }
  
  public void updateGeometries(Position pos) {
    Set<VoxelOctree> processed = Sets.newHashSet();
    closestSpatials = processUnprocessedTrees(processed, octree.treesInSphere(pos, 20.0, 0), 4, pos);
    processUnprocessedTrees(processed, octree.treesInSphere(pos, 40.0, 0), 3, pos);
    processUnprocessedTrees(processed, octree.treesInSphere(pos, 80.0, 0), 2, pos);
    processEdits();
    removeGeometriesNotInSet(processed);
  }
  
  private void processEdits() {
    OctreeEdit edit = null;
    synchronized(editLock) {
      if (!incomingEdits.isEmpty()) {
        edit = incomingEdits.remove(0);
      }
    }
    if (edit != null) {
      Vector3f l = edit.location;
      Geometry g = new Geometry("edit", edit.mesh);
      BoundingSphere sphere = new BoundingSphere(); 
      g.setModelBound(sphere);
      g.setLocalTranslation(edit.location);
      g.setLocalRotation(edit.rotation);
      g.updateModelBound();
      Vector3f c = sphere.getCenter();
      double radius = sphere.getRadius();
      Position p = new Position(c.x + l.x, c.y + l.y, c.z + l.z);
      VoxelOctree tree = octree.findSmallestTreeContainingSphere(p, radius);
      VoxelOctree editTree = tree.copyTopLevel();
      editTree.constructFromMeshToLevel(g, 4, p, radius);
      tree.mergeWith(editTree);
      List<VoxelOctree> updated = tree.getLevelAffected(0);
      for (VoxelOctree update : updated) {
        if (update.overlapsSphere(p, radius)) {
          detach(update);
          updateTreeMesh(4, update);
        }
      }
    }
  }

  private void removeGeometriesNotInSet(Set<VoxelOctree> processed) {
    for (VoxelOctree tree : geometryMap.keySet()) {
      if (!processed.contains(tree)) {
        for (Geometry g : geometryMap.get(tree).geometries) {
          game.removeGeometry(g);
        }
        geometryMap.remove(tree);
      }
    }
  }
  
  private List<Geometry> processUnprocessedTrees(Set<VoxelOctree> processed, List<VoxelOctree> trees, int level, final Position focus) {
    Collections.sort(trees, new Comparator<VoxelOctree>() {
      @Override public int compare(VoxelOctree o1, VoxelOctree o2) {
        return Double.compare(distance(focus, o1.center()), distance(focus, o2.center()));
      }
    });
    List<Geometry> result = Lists.newArrayList();
    for (VoxelOctree tree : trees) {
      if(!processed.contains(tree)) {
        result.addAll(updateTreeMesh(level, tree));
      }
    }
    processed.addAll(trees);
    return result;
  }
  
  private List<Geometry> updateTreeMesh(int level, VoxelOctree tree) {
    OctreeMesh old = geometryMap.get(tree);
    if (old != null && old.renderLevel != level) {
      old = null;
    }
    if (old == null) {
      tree.divideAllToLevel(level);
      updateLowerLevelNeighbours(level, tree);
      OctreeMesh m = new OctreeMesh(tree, generate(tree, level), level);
      attach(m);
      return m.geometries;
    } else {
      return old.geometries;
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
  
  public List<Geometry> getClosestSpatials() {
    return closestSpatials;
  }

  public void stop() {
    updaterRunning = false;
    updater = null;
  }

  public void addMeshAt(Vector3f location, Quaternion rot, Mesh mesh) {
    synchronized(editLock) {
      incomingEdits.add(new OctreeEdit(mesh, location, rot, false));
    }
  }
  
  public void deleteMeshAt(Vector3f location, Quaternion rot, Mesh mesh) {
    synchronized(editLock) {
      incomingEdits.add(new OctreeEdit(mesh, location, rot, true));
    }
  }
  
}
