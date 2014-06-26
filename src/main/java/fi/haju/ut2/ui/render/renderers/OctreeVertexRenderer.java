package fi.haju.ut2.ui.render.renderers;

import java.util.Queue;

import javax.inject.Singleton;


import com.google.common.collect.Queues;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.ui.MeshUtils;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeVertexRenderer {
  
  private Node rootNode;
  private AssetManager assetManager;
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
  
  private void sphere(Position pos, double size) {
    Geometry node = MeshUtils.makeSimpleMesh(
        new Sphere(6, 6, (float)size * 0.2f),
        new ColorRGBA(1.0f, 0.1f, 0.1f, 1.0f),
        assetManager);
    node.setLocalTranslation((float)pos.x, (float)pos.y, (float)pos.z);
    rootNode.attachChild(node);
  }
  
  public void render(VoxelOctree root) {
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(root);
    while (!tbp.isEmpty()) {
      VoxelOctree octree = tbp.remove();
      if (octree.children != null) {
        for(VoxelOctree child : octree.children) {
          if (child != null) {
            tbp.add(child);
          }
        }
      } else if (octree.vertexPosition != null) {
        sphere(octree.vertexPosition, 0.2f);
      }
    }
  }
  
}
