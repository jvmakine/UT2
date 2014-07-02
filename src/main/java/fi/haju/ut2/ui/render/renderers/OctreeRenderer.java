package fi.haju.ut2.ui.render.renderers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.ui.MeshUtils;
import fi.haju.ut2.voxels.octree.VoxelEdge;
import fi.haju.ut2.voxels.octree.VoxelFace;
import fi.haju.ut2.voxels.octree.VoxelNode;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeRenderer {
  
  private Node rootNode;
  private AssetManager assetManager;
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
  
  private void sphere(Position pos, double size, boolean positive) {
    Geometry node = MeshUtils.makeSimpleMesh(
        new Sphere(6, 6, (float)size * 0.2f),
        positive ? new ColorRGBA(0.4f, 0.7f, 0.4f, 1.0f) : new ColorRGBA(0.1f, 0.17f, 0.1f, 1.0f),
        assetManager);
    node.setLocalTranslation((float)pos.x, (float)pos.y, (float)pos.z);
    rootNode.attachChild(node);
  }
  
  private void line(Position p1, Position p2) {
    Geometry line = MeshUtils.line(p1, p2, new ColorRGBA(0.4f, 0.7f, 0.3f, 1.0f), assetManager);
    rootNode.attachChild(line);
  }
  
  public void render(VoxelOctree root) {
    Set<VoxelEdge> edges = Sets.newHashSet();
    Map<VoxelNode, Integer> nodes = Maps.newHashMap();
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(root);
    while (!tbp.isEmpty()) {
      VoxelOctree octree = tbp.remove();
      for (VoxelFace face : octree.faces) {
        for (VoxelEdge edge : face.edges) {
          edges.add(edge);
          if (!nodes.containsKey(edge.minus)) {
            nodes.put(edge.minus, octree.depth);
          }
          if (!nodes.containsKey(edge.plus)) {
            nodes.put(edge.plus, octree.depth);
          }
        }
      }
      
      if (octree.children != null) {
        for(VoxelOctree child : octree.children) {
          if (child != null) {
            tbp.add(child);
          }
        }
      }
    }
    drawNodes(nodes);
    drawEdges(edges);
  }

  private void drawEdges(Set<VoxelEdge> edges) {
    for (VoxelEdge edge : edges) {
      line(edge.minus.position, edge.plus.position);
    }
  }

  private void drawNodes(Map<VoxelNode, Integer> nodes) {
    for (Entry<VoxelNode, Integer> entry : nodes.entrySet()) {
      sphere(entry.getKey().position, Math.pow(0.65, entry.getValue()), entry.getKey().positive);
    }
  }
  
}
