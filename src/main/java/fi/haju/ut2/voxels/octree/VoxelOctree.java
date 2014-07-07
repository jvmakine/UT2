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
        if (children[i].isInside(p)) return children[i].getOctreeAtPosition(p, depth);
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
    if (index == 0) {
      // extend existing faces
      VoxelFace[] box = new VoxelFace[6];
      box[0] = faces[0].generateParent(0, function);
      box[1] = faces[1].generateParent(0, function);
      box[2] = faces[2].generateParent(0, function);
      // generate new corner
      Position d = box[2].edges[2].edgeVector();
      Position np = add(box[1].edges[2].plus.position, d);
      VoxelNode nn = new VoxelNode(np, function);
      VoxelNode mid = faces[3].edges[2].plus;
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
      // make dividing edges
      VoxelEdge[] dive = new VoxelEdge[6];
      dive[0] = faces[3].edges[1];
      dive[1] = faces[5].edges[2];
      dive[2] = faces[5].edges[1];
      dive[3] = edge(mid, box[3].dividor, function);
      dive[4] = edge(mid, box[4].dividor, function);
      dive[5] = edge(mid, box[5].dividor, function);
      // make dividing faces
      VoxelFace[][] div = new VoxelFace[3][];
      for(int i = 0; i < 3; ++i) div[i] = new VoxelFace[4];
      // xz
      div[0][0] = faces[5];
      div[0][1] = face(box[2].dividingEdge(1), box[3].dividingEdge(3), dive[3], dive[2]);
      div[0][2] = face(dive[3], box[3].dividingEdge(1), box[4].dividingEdge(1), dive[4]);
      div[0][3] = face(dive[1], dive[4], box[4].dividingEdge(3), box[1].dividingEdge(1));
      // yz
      div[1][0] = faces[3];
      div[1][1] = face(box[0].dividingEdge(2), box[4].dividingEdge(0), dive[4], dive[0]);
      div[1][2] = face(dive[4], box[4].dividingEdge(2), box[5].dividingEdge(2), dive[5]);
      div[1][3] = face(dive[2], dive[5], box[5].dividingEdge(0), box[2].dividingEdge(2));
      // xy
      div[2][0] = faces[4];
      div[2][1] = face(box[0].dividingEdge(1), box[3].dividingEdge(0), dive[3], dive[0]);
      div[2][2] = face(dive[3], box[3].dividingEdge(2), box[5].dividingEdge(1), dive[5]);
      div[2][3] = face(dive[1], dive[5], box[5].dividingEdge(3), box[1].dividingEdge(2));
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
      parent.children[0] = this;
      parent.children[1].faces = new VoxelFace[] { box[0].children[1], div[1][0], box[2].children[1], box[3].children[0], div[2][1], div[0][1] };
      parent.children[2].faces = new VoxelFace[] { box[0].children[2], div[1][1], div[2][1],  box[3].children[1], box[4].children[1], div[0][2] };
      parent.children[3].faces = new VoxelFace[] { box[0].children[3], box[1].children[1], div[2][0], div[1][1], box[4].children[0], div[0][3] };
      
      parent.children[4].faces = new VoxelFace[] { div[0][0], box[1].children[3], box[2].children[3], div[1][3], div[2][3], box[5].children[0] };
      parent.children[5].faces = new VoxelFace[] { div[0][1], div[1][3], box[2].children[2], box[3].children[3], div[2][2], box[5].children[1] };
      parent.children[6].faces = new VoxelFace[] { div[0][2], div[1][2], div[2][2], box[3].children[2], box[4].children[2], box[5].children[2] };
      parent.children[7].faces = new VoxelFace[] { div[0][3], box[1].children[2], div[2][3], div[1][2], box[4].children[3], box[5].children[3] };
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
        if (i != 0) {
          parent.children[i].calculateComponents();
        }
      }
      
      return parent;
    }
    // TODO Auto-generated method stub
    return null;
  }

  private final boolean isInside(Position p) {
    return faces[0].edges[0].minus.position.y >= p.y 
        && faces[5].edges[0].minus.position.y <= p.y
        && faces[1].edges[0].minus.position.x >= p.x
        && faces[3].edges[0].minus.position.x <= p.x
        && faces[2].edges[0].minus.position.z >= p.z
        && faces[4].edges[0].minus.position.z <= p.z;
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
  
}
