package fi.haju.ut2.voxels.octree.utils;

import static fi.haju.ut2.geometry.Position.add;
import static fi.haju.ut2.geometry.Position.substract;
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
    
  public static VoxelOctree generateOctreeWithChild(int index, VoxelFace[] faces, Function3d function, VoxelOctree tree) {
    // dividing faces
    VoxelFace[][] div = new VoxelFace[3][];
    for(int i = 0; i < 3; ++i) div[i] = new VoxelFace[4];
    // parent faces
    VoxelFace[] box = new VoxelFace[6];
    // middle node
    VoxelNode mid = null;
    // dividing edges
    VoxelEdge[] dive = new VoxelEdge[6];
    
    if (index == 0) {
      // extend existing faces
      box[0] = faces[0].generateParent(0, function);
      box[1] = faces[1].generateParent(0, function);
      box[2] = faces[2].generateParent(0, function);
      // generate new corner
      Position d = box[2].edges[2].edgeVector();
      Position np = add(box[1].edges[2].plus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      mid = faces[3].edges[2].plus;
      // Make new outer edges
      VoxelEdge[] e = { 
        edge(box[2].edges[2].plus, nn, function),
        edge(box[1].edges[2].plus, nn, function),
        edge(box[0].edges[1].plus, nn, function) };
      // make new outer faces
      box[3] = face(box[0].edges[1], e[2], e[0], box[2].edges[1]);
      box[3].divide(function);
      box[4] = face(box[0].edges[2], e[2], e[1], box[1].edges[1]);
      box[4].divide(function);
      box[5] = face(box[2].edges[2], e[0], e[1], box[1].edges[2]);
      box[5].divide(function);
      // existing dividing edges
      dive[0] = faces[3].edges[1];
      dive[1] = faces[5].edges[2];
      dive[2] = faces[5].edges[1];
      // existing dividing faces
      div[0][0] = faces[5];
      div[1][0] = faces[3];
      div[2][0] = faces[4];
    } else if (index == 1) {
      // extend existing faces
      box[0] = faces[0].generateParent(1, function);
      box[2] = faces[2].generateParent(1, function);
      box[3] = faces[3].generateParent(0, function);
      // generate new corner
      Position d = box[2].edges[2].edgeVector();
      Position np = substract(box[3].edges[2].plus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      mid = faces[1].edges[2].plus;
      // Make new outer edges
      VoxelEdge[] e = { 
        edge(nn, box[3].edges[2].plus, function),
        edge(box[0].edges[2].minus, nn, function),
        edge(box[2].edges[2].minus, nn, function) };
      // make new outer faces
      box[1] = face(box[0].edges[3], e[1], e[2], box[2].edges[3]);
      box[1].divide(function);
      box[4] = face(box[0].edges[2], box[3].edges[1], e[0], e[1]);
      box[4].divide(function);
      box[5] = face(box[2].edges[2], box[3].edges[2], e[0], e[2]);
      box[5].divide(function);
      // existing dividing edges
      dive[0] = faces[1].edges[1];
      dive[2] = faces[1].edges[2];
      dive[3] = faces[4].edges[2];
      // existing dividing faces
      div[0][1] = faces[5];
      div[1][0] = faces[1];
      div[2][1] = faces[4];
    } else if (index == 2) {
      // extend existing faces
      box[0] = faces[0].generateParent(2, function);
      box[3] = faces[3].generateParent(1, function);
      box[4] = faces[4].generateParent(1, function);
      // generate new corner
      Position d = box[3].edges[2].edgeVector();
      Position np = substract(box[4].edges[2].minus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      mid = faces[2].edges[2].minus;
      // Make new outer edges
      VoxelEdge[] e = { 
        edge(box[0].edges[0].minus, nn, function),
        edge(nn, box[3].edges[2].minus, function),
        edge(nn, box[4].edges[2].minus, function) };
      // make new outer faces
      box[1] = face(box[0].edges[3], box[4].edges[3], e[2], e[0]);
      box[1].divide(function);
      box[2] = face(box[0].edges[0], box[3].edges[3], e[1], e[0]);
      box[2].divide(function);
      box[5] = face(box[2].edges[2], box[3].edges[2], box[4].edges[2], box[1].edges[2]);
      box[5].divide(function);
      // existing dividing edges
      dive[0] = faces[1].edges[3];
      dive[3] = faces[2].edges[2];
      dive[4] = faces[1].edges[2];
      // existing dividing faces
      div[0][2] = faces[5];
      div[1][1] = faces[1];
      div[2][1] = faces[2];
    } else if (index == 3) {
      // extend existing faces
      box[0] = faces[0].generateParent(3, function);
      box[1] = faces[1].generateParent(1, function);
      box[4] = faces[4].generateParent(0, function);
      // generate new corner
      Position d = box[1].edges[2].edgeVector();
      Position np = substract(box[4].edges[2].plus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      mid = faces[2].edges[2].plus;
      // Make new outer edges
      VoxelEdge[] e = { 
        edge(box[0].edges[0].plus, nn, function),
        edge(nn, box[4].edges[2].plus, function),
        edge(box[1].edges[2].minus, nn, function) };
      // make new outer faces
      box[2] = face(box[0].edges[0], e[0], e[2], box[1].edges[3]);
      box[2].divide(function);
      box[3] = face(box[0].edges[1], box[4].edges[1], e[1], e[0]);
      box[3].divide(function);
      box[5] = face(box[2].edges[2], box[3].edges[2], box[4].edges[2], box[1].edges[2]);
      box[5].divide(function);
      // existing dividing edges
      dive[0] = faces[2].edges[1];
      dive[1] = faces[5].edges[0];
      dive[4] = faces[5].edges[1];
      // existing dividing faces
      div[0][3] = faces[5];
      div[1][1] = faces[3];
      div[2][0] = faces[2];
    } else if (index == 4) {
      // extend existing faces
      box[5] = faces[5].generateParent(0, function);
      box[1] = faces[1].generateParent(3, function);
      box[2] = faces[2].generateParent(3, function);
      // generate new corner
      Position d = box[2].edges[2].edgeVector();
      Position np = add(box[1].edges[2].plus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      mid = faces[4].edges[0].plus;
      // Make new outer edges
      VoxelEdge[] e = { 
        edge(nn, box[5].edges[1].plus, function),
        edge(box[2].edges[0].plus, nn, function),
        edge(box[1].edges[0].plus, nn, function) };
      // make new outer faces
      box[0] = face(box[2].edges[0], e[1], e[2], box[1].edges[0]);
      box[0].divide(function);
      box[3] = face(e[1], e[0], box[5].edges[1], box[2].edges[1]);
      box[3].divide(function);
      box[4] = face(e[2], box[3].edges[1], box[5].edges[2], box[1].edges[1]);
      box[4].divide(function);
      // existing dividing edges
      dive[1] = faces[0].edges[2];
      dive[2] = faces[0].edges[1];
      dive[5] = faces[3].edges[1];
      // existing dividing faces
      div[0][0] = faces[0];
      div[1][3] = faces[3];
      div[2][3] = faces[4];
    } else if (index == 5) {
      // extend existing faces
      box[5] = faces[5].generateParent(1, function);
      box[2] = faces[2].generateParent(2, function);
      box[3] = faces[3].generateParent(3, function);
      // generate new corner
      Position d = box[2].edges[0].edgeVector();
      Position np = substract(box[3].edges[0].plus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      mid = faces[4].edges[0].minus;
      // Make new outer edges
      VoxelEdge[] e = { 
        edge(nn, box[5].edges[2].minus, function),
        edge(box[2].edges[0].minus, nn, function),
        edge(nn, box[3].edges[0].plus, function) };
      // make new outer faces
      box[0] = face(box[2].edges[0], box[3].edges[0], e[2], e[1]);
      box[0].divide(function);
      box[1] = face(box[0].edges[3], e[0], box[5].edges[3], box[2].edges[3]);
      box[1].divide(function);
      box[4] = face(box[0].edges[2], box[3].edges[1], box[5].edges[2], box[1].edges[1]);
      box[4].divide(function);
      // existing dividing edges
      dive[2] = faces[0].edges[3];
      dive[3] = faces[0].edges[2];
      dive[5] = faces[1].edges[1];
      // existing dividing faces
      div[0][1] = faces[0];
      div[1][3] = faces[1];
      div[2][2] = faces[4];
    } else if (index == 6) {
      // extend existing faces
      box[5] = faces[5].generateParent(2, function);
      box[3] = faces[3].generateParent(2, function);
      box[4] = faces[4].generateParent(2, function);
      // generate new corner
      Position d = box[3].edges[0].edgeVector();
      Position np = substract(box[4].edges[0].minus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      mid = faces[0].edges[0].minus;
      // Make new outer edges
      VoxelEdge[] e = { 
        edge(nn, box[5].edges[0].minus, function),
        edge(nn, box[4].edges[0].minus, function),
        edge(nn, box[3].edges[0].minus, function) };
      // make new outer faces
      box[0] = face(e[2], box[3].edges[0], box[4].edges[0], e[1]);
      box[0].divide(function);
      box[1] = face(box[0].edges[3], box[4].edges[3], box[5].edges[3], e[0]);
      box[1].divide(function);
      box[2] = face(box[0].edges[0], box[3].edges[3], box[5].edges[0], box[1].edges[3]);
      box[2].divide(function);
      // existing dividing edges
      dive[3] = faces[0].edges[0];
      dive[4] = faces[0].edges[3];
      dive[5] = faces[2].edges[3];
      // existing dividing faces
      div[0][2] = faces[0];
      div[1][2] = faces[1];
      div[2][2] = faces[2];
    } else if (index == 7) {
      // extend existing faces
      box[1] = faces[1].generateParent(2, function);
      box[5] = faces[5].generateParent(3, function);
      box[4] = faces[4].generateParent(3, function);
      // generate new corner
      Position d = box[1].edges[0].edgeVector();
      Position np = substract(box[4].edges[0].plus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      mid = faces[0].edges[0].plus;
      // Make new outer edges
      VoxelEdge[] e = { 
        edge(nn, box[5].edges[0].plus, function),
        edge(nn, box[4].edges[0].plus, function),
        edge(box[1].edges[0].minus, nn, function) };
      // make new outer faces
      box[0] = face(e[2], e[1], box[4].edges[0], box[1].edges[0]);
      box[0].divide(function);
      box[2] = face(box[0].edges[0], e[0], box[5].edges[0], box[1].edges[3]);
      box[2].divide(function);
      box[3] = face(box[0].edges[1], box[4].edges[1], box[5].edges[1], box[2].edges[1]);
      box[3].divide(function);
      // existing dividing edges
      dive[1] = faces[0].edges[0];
      dive[4] = faces[0].edges[1];
      dive[5] = faces[2].edges[1];
      // existing dividing faces
      div[0][3] = faces[0];
      div[1][2] = faces[3];
      div[2][3] = faces[2];
    }
    // make dividing edges
    if (dive[0] == null) dive[0] = edge(box[0].dividor, mid, function);
    if (dive[1] == null) dive[1] = edge(box[1].dividor, mid, function);
    if (dive[2] == null) dive[2] = edge(box[2].dividor, mid, function);
    
    if (dive[3] == null) dive[3] = edge(mid, box[3].dividor, function);
    if (dive[4] == null) dive[4] = edge(mid, box[4].dividor, function);
    if (dive[5] == null) dive[5] = edge(mid, box[5].dividor, function);
    // make dividing faces
    // xz
    if (div[0][0] == null) div[0][0] = face(box[2].dividingEdge(3), dive[2], dive[1], box[1].dividingEdge(3));
    if (div[0][1] == null) div[0][1] = face(box[2].dividingEdge(1), box[3].dividingEdge(3), dive[3], dive[2]);
    if (div[0][2] == null) div[0][2] = face(dive[3], box[3].dividingEdge(1), box[4].dividingEdge(1), dive[4]);
    if (div[0][3] == null) div[0][3] = face(dive[1], dive[4], box[4].dividingEdge(3), box[1].dividingEdge(1));
    // yz
    if (div[1][0] == null) div[1][0] = face(box[0].dividingEdge(0), dive[0], dive[2], box[2].dividingEdge(0));
    if (div[1][1] == null) div[1][1] = face(box[0].dividingEdge(2), box[4].dividingEdge(0), dive[4], dive[0]);
    if (div[1][2] == null) div[1][2] = face(dive[4], box[4].dividingEdge(2), box[5].dividingEdge(2), dive[5]);
    if (div[1][3] == null) div[1][3] = face(dive[2], dive[5], box[5].dividingEdge(0), box[2].dividingEdge(2));
    // xy
    if (div[2][0] == null) div[2][0] = face(box[0].dividingEdge(3), dive[0], dive[1], box[1].dividingEdge(0));
    if (div[2][1] == null) div[2][1] = face(box[0].dividingEdge(1), box[3].dividingEdge(0), dive[3], dive[0]);
    if (div[2][2] == null) div[2][2] = face(dive[3], box[3].dividingEdge(2), box[5].dividingEdge(1), dive[5]);
    if (div[2][3] == null) div[2][3] = face(dive[1], dive[5], box[5].dividingEdge(3), box[1].dividingEdge(2));
    
    // make parent
    VoxelOctree parent = new VoxelOctree();
    parent.faces = box;
    parent.dividor = mid;
    parent.depth = tree.depth - 1;
    parent.function = function;
    parent.children = new VoxelOctree[8];
    box[0].plus = parent;
    box[1].plus = parent;
    box[2].plus = parent;
    box[3].minus = parent;
    box[4].minus = parent;
    box[5].minus = parent;
    
    // make children
    for (int i = 0; i < 8; ++i) {
      parent.children[i] = new VoxelOctree();
      parent.children[i].depth = tree.depth;
      parent.children[i].function = function;
    }
    parent.children[index] = tree;
    if (index != 0) parent.children[0].faces = new VoxelFace[] { box[0].children[0], box[1].children[0], box[2].children[0], div[1][0], div[2][0], div[0][0] };
    if (index != 1) parent.children[1].faces = new VoxelFace[] { box[0].children[1], div[1][0], box[2].children[1], box[3].children[0], div[2][1], div[0][1] };
    if (index != 2) parent.children[2].faces = new VoxelFace[] { box[0].children[2], div[1][1], div[2][1],  box[3].children[1], box[4].children[1], div[0][2] };
    if (index != 3) parent.children[3].faces = new VoxelFace[] { box[0].children[3], box[1].children[1], div[2][0], div[1][1], box[4].children[0], div[0][3] };
      
    if (index != 4) parent.children[4].faces = new VoxelFace[] { div[0][0], box[1].children[3], box[2].children[3], div[1][3], div[2][3], box[5].children[0] };
    if (index != 5) parent.children[5].faces = new VoxelFace[] { div[0][1], div[1][3], box[2].children[2], box[3].children[3], div[2][2], box[5].children[1] };
    if (index != 6) parent.children[6].faces = new VoxelFace[] { div[0][2], div[1][2], div[2][2], box[3].children[2], box[4].children[2], box[5].children[2] };
    if (index != 7) parent.children[7].faces = new VoxelFace[] { div[0][3], box[1].children[2], div[2][3], div[1][2], box[4].children[3], box[5].children[3] };
    
      // setup child neighbours
    for(int i = 0; i < 4; ++i) { div[0][i].setSides(parent.children[i], parent.children[4 + i]); }
      
    div[1][0].setSides(parent.children[0], parent.children[1]);
    div[1][1].setSides(parent.children[3], parent.children[2]);
    div[1][2].setSides(parent.children[7], parent.children[6]);
    div[1][3].setSides(parent.children[4], parent.children[5]);
      
    div[2][0].setSides(parent.children[0], parent.children[3]);
    div[2][1].setSides(parent.children[1], parent.children[2]);
    div[2][2].setSides(parent.children[5], parent.children[6]);
    div[2][3].setSides(parent.children[4], parent.children[7]);
            
    return parent;
  }
  
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
