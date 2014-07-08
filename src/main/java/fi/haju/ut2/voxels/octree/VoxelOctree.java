package fi.haju.ut2.voxels.octree;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import fi.haju.ut2.voxels.octree.utils.OctreeConstructionUtils;

import static fi.haju.ut2.voxels.octree.VoxelFace.face;
import static fi.haju.ut2.geometry.Position.add;
import static fi.haju.ut2.geometry.Position.substract;
import static fi.haju.ut2.voxels.octree.VoxelEdge.edge;

/**
 * Restricted octree, the depth of neighbors may differ only by one
 * The faces of octree and edges of faces are indexed as described in voxel_cube.png
 */
public final class VoxelOctree {

  public VoxelFace[] faces = new VoxelFace[6];
  public VoxelOctree[] children;
  public VoxelNode dividor;
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
    setupFaces();
    calculateComponents();
  }
  
  public Position center() {
    return Position.average(faces[0].edges[0].minus.position, faces[5].edges[2].plus.position);
  }
  
  public void divideAllToLevel(int level) {
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(this);
    while(!tbp.isEmpty()) {
      VoxelOctree tree = tbp.remove();
      if(tree.depth >= level) continue;
      if (tree.children == null) tree.divide();
      for(int i = 0; i < 8; ++i) {
        tbp.add(tree.children[i]);
      }
    }
  }

  public void setupFaces() {
    faces[0].plus = this;
    faces[1].plus = this;
    faces[2].plus = this;
    faces[3].minus = this;
    faces[4].minus = this;
    faces[5].minus = this;
  }
  
  public void divide() {
    if (children != null) return;
    for (VoxelFace face : faces) face.divide(function);
    dividor = node(center());
    children = OctreeConstructionUtils.constructChildren(dividor, faces, function);
    for (int i = 0; i < 8; ++i) {
      children[i].depth = depth + 1;
      children[i].parent = this;
      children[i].function = function;
      children[i].setupFaces();
    }
    calculateComponents();
    for(VoxelOctree n : neighbours()) {
      if(n != null) n.calculateComponents();
    }
  }
  
  public VoxelOctree getOctreeAtPosition(Position p, int depth) {
    if(isInside(p)) {
      if (depth <= this.depth) return this;
      if (children == null) {
        divide();
      }
      for(int i = 0; i < 8; ++i) {
        if (children[i].isInside(p)) {
          return children[i].getOctreeAtPosition(p, depth);
        }
      }
      throw new IllegalStateException();
    } else {
      if (parent != null) {
        return parent.getOctreeAtPosition(p, depth);
      } else {
        int index = OctreeConstructionUtils.getParentsChildIndex(p, faces);
        this.parent = generateOctreeWithChild(index);
        return parent.getOctreeAtPosition(p, depth);
      }
    }
  }

  public VoxelOctree generateOctreeWithChild(int index) {
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
    parent = new VoxelOctree();
    parent.faces = box;
    parent.dividor = mid;
    parent.depth = depth - 1;
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
      parent.children[i].depth = depth;
      parent.children[i].function = function;
    }
    parent.children[index] = this;
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
      
    for (int i = 0; i < 8; ++i) {
      if (i != index) {
        parent.children[i].calculateComponents();
      }
    }
      
    return parent;
  }

  private final boolean isInside(Position p) {
    return faces[0].edges[0].minus.position.y <= p.y 
        && faces[5].edges[0].minus.position.y >= p.y
        && faces[1].edges[0].minus.position.x <= p.x
        && faces[3].edges[0].minus.position.x >= p.x
        && faces[2].edges[0].minus.position.z <= p.z
        && faces[4].edges[0].minus.position.z >= p.z;
  }
  
  public void calculateComponents() {
    Set<FaceSegment> segments = Sets.newHashSet();
    for (VoxelFace face : faces) {
      segments.addAll(face.getMostDetailedSegments());
    }
    components = OctreeConstructionUtils.createComponentsFromSegments(segments);
    if (children != null) {
      for(int i = 0; i < 8; ++i) {
        children[i].calculateComponents();
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
  
  public List<VoxelOctree> neighbours() {
    return Lists.newArrayList(
        faces[0].minus(),
        faces[1].minus(),
        faces[2].minus(),
        faces[3].plus(),
        faces[4].plus(),
        faces[5].plus()
    );
  }
  
  public VoxelOctree up() { return faces[0].minus(); }
  public VoxelOctree west() { return faces[1].minus(); }
  public VoxelOctree north() { return faces[2].minus(); }
  public VoxelOctree east() { return faces[3].plus(); }
  public VoxelOctree south() { return faces[4].plus(); }
  public VoxelOctree down() { return faces[5].plus(); }

  public double edgeLength() {
    return faces[0].edges[0].edgeVector().length();
  }
  
}
