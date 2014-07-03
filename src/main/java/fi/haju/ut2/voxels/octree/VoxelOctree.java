package fi.haju.ut2.voxels.octree;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import fi.haju.ut2.voxels.octree.utils.OctreeConstructionUtils;

/**
 * Restricted octree, the depth of neighbors may differ only by one
 * The faces of octree and edges of faces are indexed as described in voxel_cube.png
 */
public final class VoxelOctree {

  public VoxelFace[] faces = new VoxelFace[6];
  public VoxelOctree[] children;
  public VoxelNode dividor;
  public VoxelOctree[] neighbours = new VoxelOctree[6];
  public VoxelOctree parent;
  public Position vertexPosition = null;
  public Function3d function;
  public List<OctreeComponent> components;
  public int depth;
  
  public VoxelOctree() { }
  
  public VoxelOctree(Position upperLeftBackCorner, double side, Function3d function) {
    this.function = function;
    depth = 0;
    faces = OctreeConstructionUtils.createInitialFaces(upperLeftBackCorner, side, function);
    calculateComponents();
  }
  
  public Position center() {
    return Position.average(faces[0].edges[0].minus.position, faces[5].edges[2].plus.position);
  }
  
  public void divideAllToLevel(int level) {
    if(depth >= level) return;
    if (children == null) divide();
    for(int i = 0; i < 8; ++i) {
      children[i].divideAllToLevel(level);
    }
  }
  
  public void divide() {
    if (children != null) return;
    divideNeighboursWithLesserDepth();
    
    for (VoxelFace face : faces) face.divide(function);
    dividor = node(center());
    children = OctreeConstructionUtils.constructChildren(dividor, faces, function);
    for (int i = 0; i < 8; ++i) {
      children[i].depth = depth + 1;
      children[i].parent = this;
      children[i].function = function;
    }
    OctreeConstructionUtils.setupChildNeighbours(children, neighbours);
    for(int i = 0; i < 6; ++i) {
      if(neighbours[i] != null) neighbours[i].calculateComponents();
    }
    calculateComponents();
  }
  
  public void calculateComponents() {
    Set<FaceSegment> segments = Sets.newHashSet();
    for (VoxelFace face : faces) {
      segments.addAll(face.getSegments());
    }
    components = OctreeConstructionUtils.createComponentsFromSegments(segments);
    if (children != null) {
      for(int i = 0; i < 8; ++i) {
        children[i].calculateComponents();
      }
    }
      
  }

  private void divideNeighboursWithLesserDepth() {
    for (VoxelOctree n : neighbours) {
      if (n != null) {
        if (n.depth < depth) {
          n.divide();
        }
      }
    }
  }
    
  private final VoxelNode node(Position pos) {
    return VoxelNode.node(pos, function);
  }
  
  public Set<VoxelEdge> edges() {
    Set<VoxelEdge> result = Sets.newHashSet();
    for (VoxelFace face : faces) {
      for (int i = 0; i < 4; ++i) result.add(face.edges[i]);
    }
    if (children != null) {
      for(int i = 0; i < 8; ++i) {
        result.addAll(children[i].edges());
      }
    }
    return result;
  }
  
  public VoxelOctree up() { return neighbours[0]; }
  public VoxelOctree west() { return neighbours[1]; }
  public VoxelOctree north() { return neighbours[2]; }
  public VoxelOctree east() { return neighbours[3]; }
  public VoxelOctree south() { return neighbours[4]; }
  public VoxelOctree down() { return neighbours[5]; }
  
}
