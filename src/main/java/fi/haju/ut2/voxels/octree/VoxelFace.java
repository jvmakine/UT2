package fi.haju.ut2.voxels.octree;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import static fi.haju.ut2.voxels.octree.VoxelNode.node;
import static fi.haju.ut2.geometry.Position.average;
import static fi.haju.ut2.voxels.octree.VoxelEdge.edge;

public class VoxelFace {
  
  public VoxelNode dividor = null;
  public VoxelFace[] children; // 0 = --, 1 = -+, 2 = +-, 3 = ++
  public VoxelFace parent = null;
  public VoxelEdge[] edges = new VoxelEdge[4];
  public VoxelOctree plus;
  public VoxelOctree minus;
  
  public VoxelFace() {}
  
  public VoxelFace(VoxelEdge e1, VoxelEdge e2, VoxelEdge e3, VoxelEdge e4) {
    edges[0] = e1;
    edges[1] = e2;
    edges[2] = e3;
    edges[3] = e4;
    calculateFaceSegements();
  }
  
  public static final VoxelFace face(VoxelEdge e1, VoxelEdge e2, VoxelEdge e3, VoxelEdge e4) {
    return new VoxelFace(e1, e2, e3, e4);
  }
  
  public final boolean hasChildren() {
    return dividor != null;
  }
  
  public final VoxelOctree plus() {
    if (plus != null) return plus;
    if (parent != null) return parent.plus();
    return null;
  }
  
  public final VoxelOctree minus() {
    if (minus != null) return minus;
    if (parent != null) return parent.minus();
    return null;
  }
  
  public final void divide(Function3d function) {
    if(dividor != null) return;
    dividor = node(average(edges[0].minus.position, edges[1].plus.position), function);
    for(int i = 0; i < 4; ++i) {
      edges[i].divide(function);
    }
    children = new VoxelFace[4];
    children[0] = face(edges[0].minusChild, edge(edges[0].dividor, dividor, function), edge(edges[3].dividor, dividor, function), edges[3].minusChild);
    children[1] = face(edges[0].plusChild, edges[1].minusChild, edge(dividor, edges[1].dividor, function), children[0].edges[1]);
    children[2] = face(children[1].edges[2], edges[1].plusChild, edges[2].plusChild, edge(dividor, edges[2].dividor, function));
    children[3] = face(children[0].edges[2], children[2].edges[3], edges[2].minusChild, edges[3].plusChild);
    for(VoxelFace face : children) {
      face.parent = this;
    }
    // recalculate face segments as the vertices may have moved to more accurate positions
    recalculateFaceSegementsForParents();
  }
  
  public void recalculateFaceSegementsForParents() {
    calculateFaceSegements();
    if (parent != null) parent.calculateFaceSegements();  
  }
  
  public void calculateFaceSegements() {
    if (plus != null) plus.calculateComponents();
    if (minus != null) minus.calculateComponents();
  }
  
  private List<FaceSegment> getFaceSegements() {
    int index = calculateConnectionIndex();
    switch(index) {
    // No segments
    case 0b0000 : 
    case 0b1111 : return Lists.newArrayList();
    // One corner
    case 0b1000 :
    case 0b0111 : return segment(edges[3], edges[0]);
    case 0b0100 :
    case 0b1011 : return segment(edges[0], edges[1]);
    case 0b0010 :
    case 0b1101 : return segment(edges[1], edges[2]);
    case 0b0001 :
    case 0b1110 :  return segment(edges[2], edges[3]);
    // One dividor
    case 0b1100 :
    case 0b0011 : return segment(edges[3], edges[1]);
    case 0b0110 :
    case 0b1001 : return segment(edges[0], edges[2]);
    // Ambiguity cases
    case 0b1010 :
    case 0b0101 : return solveAmbiguitySegment();
    }
    throw new IllegalArgumentException("Unknown index " + index);
  }

  private List<FaceSegment> solveAmbiguitySegment() {
    List<FaceSegment> result = Lists.newArrayList();
    // if normals differ too much, they are supposed to reside on different surfaces
    if (Position.difference(edges[0].vertex().normal, edges[1].vertex().normal) > 2) {
      result.addAll(segment(edges[3], edges[0]));
      result.addAll(segment(edges[1], edges[2])); 
    } else {
      result.addAll(segment(edges[0], edges[1]));
      result.addAll(segment(edges[2], edges[3]));
    }
    return result;
  }

  private final List<FaceSegment> segment(VoxelEdge e1, VoxelEdge e2) {
    return Lists.newArrayList(new FaceSegment(e1.vertex(), e2.vertex()));
  }

  private final int calculateConnectionIndex() {
    int index = 0;
    index = (index << 1) + (corner30().positive ? 1 : 0); 
    index = (index << 1) + (corner01().positive ? 1 : 0);
    index = (index << 1) + (corner12().positive ? 1 : 0);
    index = (index << 1) + (corner23().positive ? 1 : 0);
    return index;
  }
  
  public VoxelNode corner30() {
    return edges[0].minus;
  }
  
  public VoxelNode corner01() {
    return edges[0].plus;
  }
  
  public VoxelNode corner12() {
    return edges[2].plus;
  }
  
  public VoxelNode corner23() {
    return edges[2].minus;
  }
  
  public VoxelEdge[] dividingEdges() {
    VoxelEdge result[] = new VoxelEdge[4];
    result[0] = children[0].edges[1];
    result[1] = children[1].edges[2];
    result[2] = children[2].edges[3];
    result[3] = children[3].edges[0];
    return result;
  }

  public Set<FaceSegment> getSegments() {
    Set<FaceSegment> result = Sets.newHashSet();
    if (!hasChildren()) {
      result.addAll(getFaceSegements());
    } else {
      for(int i = 0; i < 4; ++i) result.addAll(children[i].getSegments());
    }
    return result;
  }

}
