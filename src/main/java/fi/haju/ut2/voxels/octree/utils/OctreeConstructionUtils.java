package fi.haju.ut2.voxels.octree.utils;

import static fi.haju.ut2.voxels.octree.VoxelEdge.edge;
import static fi.haju.ut2.voxels.octree.VoxelFace.face;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import fi.haju.ut2.voxels.octree.FaceSegment;
import fi.haju.ut2.voxels.octree.OctreeComponent;
import fi.haju.ut2.voxels.octree.PositionWithNormal;
import fi.haju.ut2.voxels.octree.VoxelEdge;
import fi.haju.ut2.voxels.octree.VoxelFace;
import fi.haju.ut2.voxels.octree.VoxelNode;
import fi.haju.ut2.voxels.octree.VoxelOctree;
import static fi.haju.ut2.voxels.octree.VoxelNode.node;

public final class OctreeConstructionUtils {
    
  public static int getParentsChildIndex(Position p, VoxelFace[] faces) {
    boolean above =  faces[0].edges[0].minus.position.y >= p.y;
    boolean west = faces[1].edges[0].minus.position.x >= p.x;
    boolean north = faces[2].edges[0].minus.position.z >= p.z;
    boolean east = faces[3].edges[0].minus.position.x <= p.x;
    boolean south = faces[4].edges[0].minus.position.z <= p.z;
    boolean below = faces[5].edges[0].minus.position.y <= p.y;
    
    if (above && west && north) return 6;
    if (above && west && south) return 5;
    if (above && east && north) return 7;
    if (above && east && south) return 4;
    
    if (below && west && north) return 2;
    if (below && west && south) return 1;
    if (below && east && north) return 3;
    if (below && east && south) return 0;
    
    if (above && north) return 6;
    if (above && south) return 5;
    if (below && north) return 2;
    if (below && south) return 0;
    
    if (above && west) return 5;
    if (above && east) return 7;
    if (below && west) return 2;
    if (below && east) return 3;
    
    if (west && north) return 6;
    if (west && south) return 5;
    if (east && north) return 7;
    if (east && south) return 4;
    
    if (above) return 6;
    if (below) return 2;
    if (south) return 5;
    if (north) return 6;
    if (west) return 6;
    if (east) return 7;
    
    throw new IllegalArgumentException("position p is inside child");
  }
  
  public static VoxelFace[] createInitialFaces(Position upperLeftBackCorner, double side, Function3d function) {
    VoxelFace[] faces = new VoxelFace[6];
    VoxelNode[] corners = new VoxelNode[8];
    // Upper side
    corners[0] = node(upperLeftBackCorner, function);
    corners[1] = node(corners[0].position.xplus(side), function);
    corners[2] = node(corners[1].position.zplus(side), function);
    corners[3] = node(corners[0].position.zplus(side), function);
    // Lower side
    corners[4] = node(corners[0].position.yplus(side), function);
    corners[5] = node(corners[1].position.yplus(side), function);
    corners[6] = node(corners[2].position.yplus(side), function);
    corners[7] = node(corners[3].position.yplus(side), function);
    
    VoxelEdge[] edges = new VoxelEdge[12];
    edges[0] = edge(corners[0], corners[1], function);
    edges[1] = edge(corners[1], corners[2], function);
    edges[2] = edge(corners[3], corners[2], function);
    edges[3] = edge(corners[0], corners[3], function);
    for(int i = 0; i < 4; ++i) edges[i + 4] = edge(corners[i], corners[i+4], function);
    edges[8] = edge(corners[4], corners[5], function);
    edges[9] = edge(corners[5], corners[6], function);
    edges[10] = edge(corners[7], corners[6], function);
    edges[11] = edge(corners[4], corners[7], function);
    
    faces[0] = face(edges[0], edges[1], edges[2], edges[3]);
    faces[1] = face(edges[3], edges[7], edges[11], edges[4]);
    faces[2] = face(edges[0], edges[5], edges[8], edges[4]);
    faces[3] = face(edges[1], edges[6], edges[9], edges[5]);
    faces[4] = face(edges[2], edges[6], edges[10], edges[7]);
    faces[5] = face(edges[8], edges[9], edges[10], edges[11]);
    return faces;
  }
  
  public static List<OctreeComponent> createComponentsFromSegments(Set<FaceSegment> segments) {
    Map<PositionWithNormal, OctreeComponent> endPoints = Maps.newHashMap();
    List<OctreeComponent> components = Lists.newArrayList();
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
          // Component loop ready
          endPoints.remove(fromComponent.vertices.peekFirst());
          endPoints.remove(fromComponent.vertices.peekLast());
          components.add(fromComponent);
        } else {
          // Component connection
          PositionWithNormal f1 = fromComponent.vertices.peekFirst();
          PositionWithNormal f2 = fromComponent.vertices.peekLast();
          PositionWithNormal t1 = toComponent.vertices.peekFirst();
          PositionWithNormal t2 = toComponent.vertices.peekLast();
          endPoints.remove(f1);
          endPoints.remove(f2);
          endPoints.remove(t1);
          endPoints.remove(t2);
          if (f2 == segment.from && t1 == segment.to) {
            fromComponent.vertices.addAll(toComponent.vertices);
            endPoints.put(fromComponent.vertices.peekFirst(), fromComponent);
            endPoints.put(fromComponent.vertices.peekLast(), fromComponent);
          } else if (t1 == segment.from && f2 == segment.to) {
            toComponent.vertices.addAll(fromComponent.vertices);
            endPoints.put(toComponent.vertices.peekFirst(), toComponent);
            endPoints.put(toComponent.vertices.peekLast(), toComponent);
          } else if (t2 == segment.from && f1 == segment.to) {
            toComponent.vertices.addAll(fromComponent.vertices);
            endPoints.put(toComponent.vertices.peekFirst(), toComponent);
            endPoints.put(toComponent.vertices.peekLast(), toComponent);
          } else if (f1 == segment.from && t2 == segment.to) {
            toComponent.vertices.addAll(fromComponent.vertices);
            endPoints.put(toComponent.vertices.peekFirst(), toComponent);
            endPoints.put(toComponent.vertices.peekLast(), toComponent);
          } else if ((t1 == segment.from && f1 == segment.to) || (f1 == segment.from && t1 == segment.to)) {
            toComponent.vertices = Lists.newLinkedList(Lists.reverse(toComponent.vertices));
            toComponent.vertices.addAll(fromComponent.vertices);
            endPoints.put(toComponent.vertices.peekFirst(), toComponent);
            endPoints.put(toComponent.vertices.peekLast(), toComponent);
          } else if ((f2 == segment.from && t2 == segment.to) || (t2 == segment.from && f2 == segment.to)) {
            fromComponent.vertices.addAll(Lists.reverse(toComponent.vertices));
            endPoints.put(fromComponent.vertices.peekFirst(), fromComponent);
            endPoints.put(fromComponent.vertices.peekLast(), fromComponent);
          } else {
            throw new IllegalStateException();
          }
        }
      }
    }
    for(OctreeComponent component : components) {
      component.centralPoint = PositionWithNormal.average(component.vertices);
     //TODO: Reverse components if they are facing the wrong way, cull back faces
    }
    if (endPoints.size() > 0) {
      //log.warning("no component for " + endPoints.size() + " endpoints, generated " + components.size() + " components");
    }
    return components;
  }
  
  public static VoxelOctree[] constructChildren(VoxelNode dividor, VoxelFace[] faces, Function3d function) {
    VoxelEdge nedge[] = new VoxelEdge[6];
    nedge[0] = edge(faces[0].dividor, dividor, function);
    nedge[1] = edge(faces[1].dividor, dividor, function);
    nedge[2] = edge(faces[2].dividor, dividor, function);
    nedge[3] = edge(dividor, faces[3].dividor, function);
    nedge[4] = edge(dividor, faces[4].dividor, function);
    nedge[5] = edge(dividor, faces[5].dividor, function);
    
    VoxelEdge up[] = faces[0].dividingEdges();
    VoxelEdge west[] = faces[1].dividingEdges();
    VoxelEdge north[] = faces[2].dividingEdges();
    VoxelEdge east[] = faces[3].dividingEdges();
    VoxelEdge south[] = faces[4].dividingEdges();
    VoxelEdge down[] = faces[5].dividingEdges();
    
    VoxelOctree[] children = new VoxelOctree[8];
    
    children[0] = new VoxelOctree();
    children[0].faces[0] = faces[0].children[0];
    children[0].faces[1] = faces[1].children[0];
    children[0].faces[2] = faces[2].children[0];
    children[0].faces[3] = face(up[0], nedge[0], nedge[2], north[0]);
    children[0].faces[4] = face(up[3], nedge[0], nedge[1], west[0]);
    children[0].faces[5] = face(north[3], nedge[2], nedge[1], west[3]);
    
    children[1] = new VoxelOctree();
    children[1].faces[0] = faces[0].children[1];
    children[1].faces[1] = children[0].faces[3];
    children[1].faces[2] = faces[2].children[1];
    children[1].faces[3] = faces[3].children[0];
    children[1].faces[4] = face(up[1], east[0], nedge[3], nedge[0]);
    children[1].faces[5] = face(north[1], east[3], nedge[3], nedge[2]);
    
    children[2] = new VoxelOctree();
    children[2].faces[0] = faces[0].children[2];
    children[2].faces[1] = face(up[2], south[0], nedge[4], nedge[0]);
    children[2].faces[2] = children[1].faces[4];
    children[2].faces[3] = faces[3].children[1];
    children[2].faces[4] = faces[4].children[1];
    children[2].faces[5] = face(nedge[3], east[1], south[1], nedge[4]);
    
    children[3] = new VoxelOctree();
    children[3].faces[0] = faces[0].children[3];
    children[3].faces[1] = faces[1].children[1];
    children[3].faces[2] = children[0].faces[4];
    children[3].faces[3] = children[2].faces[1];
    children[3].faces[4] = faces[4].children[0];
    children[3].faces[5] = face(nedge[1], nedge[4], south[3], west[1]);
    
    children[4] = new VoxelOctree();
    children[4].faces[0] = children[0].faces[5];
    children[4].faces[1] = faces[1].children[3];
    children[4].faces[2] = faces[2].children[3];
    children[4].faces[3] = face(nedge[2], nedge[5], down[0], north[2]);
    children[4].faces[4] = face(nedge[1], nedge[5], down[3], west[2]);
    children[4].faces[5] = faces[5].children[0];
    
    children[5] = new VoxelOctree();
    children[5].faces[0] = children[1].faces[5];
    children[5].faces[1] = children[4].faces[3];
    children[5].faces[2] = faces[2].children[2];
    children[5].faces[3] = faces[3].children[3];
    children[5].faces[4] = face(nedge[3], east[2], down[1], nedge[5]);
    children[5].faces[5] = faces[5].children[1];
    
    children[6] = new VoxelOctree();
    children[6].faces[0] = children[2].faces[5];
    children[6].faces[1] = face(nedge[4], south[2], down[2], nedge[5]);
    children[6].faces[2] = children[5].faces[4];
    children[6].faces[3] = faces[3].children[2];
    children[6].faces[4] = faces[4].children[2];
    children[6].faces[5] = faces[5].children[2];
    
    children[7] = new VoxelOctree();
    children[7].faces[0] = children[3].faces[5];
    children[7].faces[1] = faces[1].children[2];
    children[7].faces[2] = children[4].faces[4];
    children[7].faces[3] = children[6].faces[1];
    children[7].faces[4] = faces[4].children[3];
    children[7].faces[5] = faces[5].children[3];
    
    return children;
  }
  
  private OctreeConstructionUtils() { }
  
}
