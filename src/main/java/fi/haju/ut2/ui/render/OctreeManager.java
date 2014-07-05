package fi.haju.ut2.ui.render;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import fi.haju.ut2.ui.render.octreegenerator.OctreeFaceSegmentGeometryGenerator;
import fi.haju.ut2.ui.render.octreegenerator.OctreeSurfaceGeometryGenerator;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeManager {
  
  @Inject private OctreeSurfaceGeometryGenerator octreeSurfaceMeshGenerator;
  @Inject private OctreeFaceSegmentGeometryGenerator octreeFaceSegmentGeometryGenerator;
  
  private Node rootNode;
  private AssetManager assetManager;
  private Map<VoxelOctree, List<Geometry>> geometryMap = Maps.newHashMap();
  
  public void render(VoxelOctree octree) {
    detach(octree);
    attach(octree, generate(octree));
  }

  private void attach(VoxelOctree octree, List<Geometry> geometries) {
    geometryMap.put(octree, geometries);
    for(Geometry geometry : geometries) {
      rootNode.attachChild(geometry);
    }
  }

  private List<Geometry> generate(VoxelOctree octree) {
    List<Geometry> geometries = octreeSurfaceMeshGenerator.generate(octree, assetManager);
    geometries.addAll(octreeFaceSegmentGeometryGenerator.generate(octree, assetManager));
    return geometries;
  }

  private void detach(VoxelOctree octree) {
    List<Geometry> oldGeometries = geometryMap.get(octree);
    if (oldGeometries != null) {
      for (Geometry geometry : oldGeometries) {
        rootNode.detachChild(geometry);
      }
    }
  }
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
  
}
