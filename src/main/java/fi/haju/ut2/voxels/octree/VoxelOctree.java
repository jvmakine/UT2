package fi.haju.ut2.voxels.octree;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
      children[i].calculateComponents();
    }
    
    OctreeConstructionUtils.setupChildNeighbours(children, neighbours);
  }
  
  private void calculateComponents() {
    Set<FaceSegment> segments = Sets.newHashSet();
    components = Lists.newArrayList();
    for (VoxelFace face : faces) {
      segments.addAll(face.faceSegments);
    }
    Map<Position, OctreeComponent> endPoints = Maps.newHashMap();
    for (FaceSegment segment : segments) {
      if (!endPoints.containsKey(segment.from) && !endPoints.containsKey(segment.to)) {
        // New component
        OctreeComponent component = new OctreeComponent();
        component.vertices.add(segment.from);
        component.vertices.add(segment.to);
        endPoints.put(segment.from, component);
        endPoints.put(segment.to, component);
      } else if (!endPoints.containsKey(segment.from) && endPoints.containsKey(segment.to)) {
        // Add to existing component
        OctreeComponent component = endPoints.get(segment.to);
        if (component.vertices.peekFirst() == segment.to) {
          component.vertices.addFirst(segment.from);
        } else {
          component.vertices.addLast(segment.from);
        }
        endPoints.remove(segment.to);
        endPoints.put(segment.from, component);
      } else if (endPoints.containsKey(segment.from) && !endPoints.containsKey(segment.to)) {
        // Add to existing component
        OctreeComponent component = endPoints.get(segment.from);
        if (component.vertices.peekFirst() == segment.from) {
          component.vertices.addFirst(segment.to);
        } else {
          component.vertices.addLast(segment.to);
        }
        endPoints.remove(segment.from);
        endPoints.put(segment.to, component);
      } else {
        // Both endpoints present
        OctreeComponent fromComponent = endPoints.get(segment.from);
        OctreeComponent toComponent = endPoints.get(segment.to);
        if (fromComponent == toComponent) {
          // Compoonent loop ready
          endPoints.remove(segment.from);
          endPoints.remove(segment.to);
          components.add(fromComponent);
        } else {
          // Component connection
          endPoints.remove(fromComponent.vertices.removeLast());
          endPoints.remove(toComponent.vertices.peekFirst());
          endPoints.remove(toComponent.vertices.peekLast());
          fromComponent.vertices.addAll(toComponent.vertices);
          endPoints.put(fromComponent.vertices.peekLast(), fromComponent);
        }
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
