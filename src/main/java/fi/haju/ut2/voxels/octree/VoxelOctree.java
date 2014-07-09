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
  }
  
  public Position center() {
    return Position.average(faces[0].edges[0].minus.position, faces[5].edges[2].plus.position);
  }
  
  public void compress() {
   if (children == null) return;
    if (!hasInternalFeatures()) {
      children = null;
      dividor = null;
      for(int i = 0; i < 6; ++i) {
        VoxelOctree n = faces[i].other(this);
        if (n != null && n.children == null && !faces[i].hasFeature()) {
          faces[i].children = null;
          faces[i].dividor = null;
        }
      }
    } else {
      for (int i = 0; i < 8; ++i) {
        children[i].compress();
      }
    }
  }

  public boolean hasEdgeFeatures() {
    for (int i = 0; i < 6; ++i) {
      if (faces[i].hasFeature()) return true;
    }
    return false;
  }
  
  private boolean hasInternalFeatures() {
    if(children[0].faces[5].edges[1].vertex() != null) return true;
    if(children[0].faces[5].edges[2].vertex() != null) return true;
    if(children[1].faces[5].edges[2].vertex() != null) return true;
    if(children[3].faces[5].edges[1].vertex() != null) return true;
    if(children[0].faces[3].edges[1].vertex() != null) return true;
    if(children[4].faces[3].edges[1].vertex() != null) return true;
    return false;
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
    return OctreeConstructionUtils.generateOctreeWithChild(index, faces, function, this);
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
