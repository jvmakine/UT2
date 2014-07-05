package fi.haju.ut2.ui.render;

import java.util.List;

import javax.inject.Inject;

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
  
  public void render(VoxelOctree octree) {
    List<Geometry> geometries = octreeSurfaceMeshGenerator.generate(octree, assetManager);
    geometries.addAll(octreeFaceSegmentGeometryGenerator.generate(octree, assetManager));
    for(Geometry geometry : geometries) {
      rootNode.attachChild(geometry);
    }
  }
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
  
}
