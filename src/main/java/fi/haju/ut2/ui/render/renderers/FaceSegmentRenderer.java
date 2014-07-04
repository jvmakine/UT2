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
import fi.haju.ut2.voxels.octree.FaceSegment;
import fi.haju.ut2.voxels.octree.VoxelFace;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class FaceSegmentRenderer {
  
  private Node rootNode;
  private AssetManager assetManager;
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
  
  private void line(Position p1, Position p2) {
    Geometry line = MeshUtils.line(p1, p2, new ColorRGBA(1.0f, 0.7f, 0.7f, 1.0f), assetManager);
    rootNode.attachChild(line);
  }
  
  public void render(VoxelOctree root) {
    Set<FaceSegment> segments = Sets.newHashSet();
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(root);
    while (!tbp.isEmpty()) {
      VoxelOctree octree = tbp.remove();
      if (octree.children == null) {
        for (VoxelFace face : octree.faces) {
          segments.addAll(face.getSegments());
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
    drawSegments(segments);
  }

  private void drawSegments(Set<FaceSegment> segments) {
    for (FaceSegment edge : segments) {
      line(edge.from, edge.to);
    }
  }  
}
