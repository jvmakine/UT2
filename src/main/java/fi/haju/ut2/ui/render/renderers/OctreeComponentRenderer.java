package fi.haju.ut2.ui.render.renderers;

import java.util.Queue;
import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.ui.MeshUtils;
import fi.haju.ut2.voxels.octree.OctreeComponent;
import fi.haju.ut2.voxels.octree.PositionWithNormal;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeComponentRenderer {
  
  private Node rootNode;
  private AssetManager assetManager;
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
  
  private void line(Position p1, Position p2, ColorRGBA color) {
    Geometry line = MeshUtils.line(p1, p2, color, assetManager);
    rootNode.attachChild(line);
  }
  
  public void render(VoxelOctree root) {
    Set<OctreeComponent> components = Sets.newHashSet();
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(root);
    while (!tbp.isEmpty()) {
      VoxelOctree octree = tbp.remove();
      if(octree.children == null) {
        components.addAll(octree.components);
      } else { 
        for(VoxelOctree child : octree.children) {
          if (child != null) {
            tbp.add(child);
          }
        }
      }
    }
    drawComponents(components);
  }

  private void drawComponents(Set<OctreeComponent> components) {
    for (OctreeComponent component : components) {
      ColorRGBA color = new ColorRGBA((float)Math.random(), (float)Math.random(), (float)Math.random(), (float)1.0);
      PositionWithNormal last = component.vertices.peekLast();
      for (PositionWithNormal p : component.vertices) {
        line(last.position, p.position, color);
        last = p;
      }
    }
  }  
}
