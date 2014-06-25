package fi.haju.ut2.ui.render.renderers;

import java.util.Queue;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.ui.MeshUtils;
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
  
  private void sphere(Position pos) {
    Geometry node = MeshUtils.makeSimpleMesh(
        new Sphere(6, 6, 0.05f),
        new ColorRGBA(0.4f, 0.7f, 0.3f, 1.0f),
        assetManager);
    node.setLocalTranslation((float)pos.x, (float)pos.y, (float)pos.z);
    rootNode.attachChild(node);
  }
  
  private void line(Position p1, Position p2) {
    Geometry line = MeshUtils.line(p1, p2, new ColorRGBA(0.4f, 0.7f, 0.3f, 1.0f), assetManager);
    rootNode.attachChild(line);
  }
  
  public void render(VoxelOctree root) {
    Set<Pair<VoxelNode, VoxelNode>> connections = Sets.newHashSet();
    Set<VoxelNode> nodes = Sets.newHashSet();
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(root);
    while (!tbp.isEmpty()) {
      VoxelOctree octree = tbp.remove();
      for (VoxelNode node : octree.corners) {
        nodes.add(node);
      }
      connections.add(Pair.of(octree.corners[0], octree.corners[1]));
      connections.add(Pair.of(octree.corners[1], octree.corners[2]));
      connections.add(Pair.of(octree.corners[2], octree.corners[3]));
      connections.add(Pair.of(octree.corners[3], octree.corners[0]));
      
      connections.add(Pair.of(octree.corners[4], octree.corners[5]));
      connections.add(Pair.of(octree.corners[5], octree.corners[6]));
      connections.add(Pair.of(octree.corners[6], octree.corners[7]));
      connections.add(Pair.of(octree.corners[7], octree.corners[4]));
      
      connections.add(Pair.of(octree.corners[0], octree.corners[4]));
      connections.add(Pair.of(octree.corners[1], octree.corners[5]));
      connections.add(Pair.of(octree.corners[2], octree.corners[6]));
      connections.add(Pair.of(octree.corners[3], octree.corners[7]));
      
      if (octree.children != null) {
        for(VoxelOctree child : octree.children) {
          if (child != null) {
            tbp.add(child);
          }
        }
      }
    }
    drawNodes(nodes);
    drawEdges(connections);
  }

  private void drawEdges(Set<Pair<VoxelNode, VoxelNode>> connections) {
    for (Pair<VoxelNode, VoxelNode> edge : connections) {
      line(edge.getLeft().position, edge.getRight().position);
    }
  }

  private void drawNodes(Set<VoxelNode> nodes) {
    for (VoxelNode node : nodes) {
      sphere(node.position);
    }
  }
  
}