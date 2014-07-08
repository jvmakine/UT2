package fi.haju.ut2.ui.render.octreerenderer;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.ui.MeshUtils;
import fi.haju.ut2.voxels.octree.FaceSegment;
import fi.haju.ut2.voxels.octree.VoxelFace;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeFaceSegmentGeometryGenerator implements OctreeGeometryGenerator {
   
  public List<Geometry> generate(VoxelOctree root, AssetManager assetManager) {
    Set<FaceSegment> segments = Sets.newHashSet();
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(root);
    while (!tbp.isEmpty()) {
      VoxelOctree octree = tbp.remove();
      if (octree.children == null) {
        for (VoxelFace face : octree.faces) {
          segments.addAll(face.getMostDetailedSegments());
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
    return Lists.newArrayList(drawSegments(segments, assetManager));
  }

  private Geometry drawSegments(Set<FaceSegment> segments, AssetManager assetManager) {
    Set<Pair<Position, Position>> endpoints = Sets.newHashSet();
    for (FaceSegment edge : segments) {
      endpoints.add(Pair.of(edge.from.position, edge.to.position));
    }
    return MeshUtils.lines(endpoints, new ColorRGBA(1.0f, 0.7f, 0.7f, 1.0f), assetManager);
  }  
}
