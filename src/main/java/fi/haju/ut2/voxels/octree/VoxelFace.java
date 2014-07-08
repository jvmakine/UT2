package fi.haju.ut2.voxels.octree;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import static fi.haju.ut2.voxels.octree.VoxelNode.node;
import static fi.haju.ut2.geometry.Position.average;
import static fi.haju.ut2.geometry.Position.add;
import static fi.haju.ut2.geometry.Position.substract;
import static fi.haju.ut2.voxels.octree.VoxelEdge.edge;

public class VoxelFace {
  
  public VoxelNode dividor = null;
  public VoxelFace[] children; // 0 = --, 1 = -+, 2 = +-, 3 = ++
  public VoxelFace parent = null;
  public VoxelEdge[] edges = new VoxelEdge[4];
  public VoxelOctree plus;
  public VoxelOctree minus;

  private Set<FaceSegment> faceSegmentCache = null;
  
  public VoxelFace() {}
  
  public VoxelFace(VoxelEdge e1, VoxelEdge e2, VoxelEdge e3, VoxelEdge e4) {
    edges[0] = e1;
    edges[1] = e2;
    edges[2] = e3;
    edges[3] = e4;
  }
  
  public static final VoxelFace face(VoxelEdge e1, VoxelEdge e2, VoxelEdge e3, VoxelEdge e4) {
    return new VoxelFace(e1, e2, e3, e4);
  }
  
  public void setSides(VoxelOctree minus, VoxelOctree plus) {
    this.plus = plus;
    this.minus = minus;
  }
  
  public final boolean hasChildren() {
    return dividor != null;
  }
  
  public VoxelEdge dividingEdge(int index) {
    switch (index) {
    case 0 : return children[0].edges[1];
    case 1 : return children[1].edges[2];
    case 2 : return children[2].edges[3];
    case 3 : return children[3].edges[0];
    }
    throw new IllegalArgumentException();
  }
  
  public final VoxelFace generateParent(int index, Function3d function) {
    VoxelEdge[] e = generateParentEdges(index, function);
    VoxelEdge[] divs = generateParentDividors(index, function, e);
    
    VoxelFace f = new VoxelFace(e[0], e[1], e[2], e[3]);
    f.children = new VoxelFace[4];
    f.children[index] = this;
    this.parent = f;
    
    if (index != 0) f.children[0] = face(e[0].minusChild, divs[0], divs[3], e[3].minusChild);
    if (index != 1) f.children[1] = face(e[0].plusChild, e[1].minusChild, divs[1], divs[0]);
    if (index != 2) f.children[2] = face(divs[1], e[1].plusChild, e[2].plusChild, divs[2]);
    if (index != 3) f.children[3] = face(divs[3], divs[2], e[2].minusChild, e[3].plusChild);
    
    if (index == 0) f.dividor = edges[2].plus;
    else if (index == 1) f.dividor = edges[2].minus;
    else if (index == 2) f.dividor = edges[0].minus;
    else if (index == 3) f.dividor = edges[0].plus;
    return f;
  }
  
  private final VoxelEdge[] generateParentDividors(int index, Function3d function, VoxelEdge[] e) {
    VoxelEdge[] div = new VoxelEdge[4];
    if (index == 0) {
      VoxelNode mid = edges[1].plus;
      div[0] = edges[1];
      div[1] = edge(mid, e[1].dividor, function);
      div[2] = edge(mid, e[2].dividor, function);
      div[3] = edges[2];
    } else if (index == 1) {
      VoxelNode mid = edges[3].plus;
      div[0] = edges[3];
      div[1] = edges[2];
      div[2] = edge(mid, e[2].dividor, function);
      div[3] = edge(e[3].dividor, mid, function);
    } else if (index == 2) {
      VoxelNode mid = edges[3].minus;
      div[0] = edge(e[0].dividor, mid, function);
      div[1] = edges[0];
      div[2] = edges[3];
      div[3] = edge(e[3].dividor, mid, function);
    } else if (index == 3) {
      VoxelNode mid = edges[1].minus;
      div[0] = edge(e[0].dividor, mid, function);
      div[1] = edge(mid, e[1].dividor, function);
      div[2] = edges[1];
      div[3] = edges[0];
    } else {
      throw new IllegalArgumentException("Wrong index : " + index);
    }
    return div;
  }
  
  private final VoxelEdge[] generateParentEdges(int index, Function3d function) {
    VoxelEdge[] e = new VoxelEdge[4];
    if (index == 0) {
      e[0] = edges[0].generateParentWithThisAsMinus(function);
      e[3] = edges[3].generateParentWithThisAsMinus(function);
      Position d = e[3].edgeVector();
      Position p = add(e[0].plus.position, d);
      VoxelNode nn = new VoxelNode(p, function);
      e[1] = new VoxelEdge(e[0].plus, nn, function);
      e[1].divide(function);
      e[2] = new VoxelEdge(e[3].plus, nn, function);
      e[2].divide(function);
    } else if (index == 1) {
      e[0] = edges[0].generateParentWithThisAsPlus(function);
      e[1] = edges[1].generateParentWithThisAsMinus(function);
      Position d = e[1].edgeVector();
      Position p = add(e[0].minus.position, d);
      VoxelNode nn = new VoxelNode(p, function);
      e[2] = new VoxelEdge(nn, e[1].plus, function);
      e[2].divide(function);
      e[3] = new VoxelEdge(e[0].minus, nn, function);
      e[3].divide(function);
    } else if (index == 2) {
      e[1] = edges[1].generateParentWithThisAsPlus(function);
      e[2] = edges[2].generateParentWithThisAsPlus(function);
      Position d = e[1].edgeVector();
      Position p = substract(e[2].minus.position, d);
      VoxelNode nn = new VoxelNode(p, function);
      e[0] = new VoxelEdge(nn, e[1].minus, function);
      e[0].divide(function);
      e[3] = new VoxelEdge(nn, e[2].minus, function);
      e[3].divide(function);
    } else if (index == 3) {
      e[2] = edges[2].generateParentWithThisAsMinus(function);
      e[3] = edges[3].generateParentWithThisAsPlus(function);
      Position d = e[2].edgeVector();
      Position p = add(e[3].minus.position, d);
      VoxelNode nn = new VoxelNode(p, function);
      e[0] = new VoxelEdge(e[3].minus, nn, function);
      e[0].divide(function);
      e[1] = new VoxelEdge(nn, e[2].plus, function);
      e[1].divide(function);
    } else {
      throw new IllegalArgumentException("Wrong index : " + index);
    }
    return e;
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
    clearCaches();
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
  }
  
  private void clearCaches() {
    faceSegmentCache = null;
    if(parent != null) parent.clearCaches();
  }
 
  private final List<FaceSegment> getFaceSegements() {
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
    int index = (edges[0].minus.positive ? 1 : 0); 
    index = (index << 1) | (edges[0].plus.positive ? 1 : 0);
    index = (index << 1) | (edges[2].plus.positive ? 1 : 0);
    return (index << 1) | (edges[2].minus.positive ? 1 : 0);
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

  public Set<FaceSegment> getMostDetailedSegments() {
    if (faceSegmentCache != null) return faceSegmentCache;
    Set<FaceSegment> result = Sets.newHashSet();
    Queue<VoxelFace> tbp = Queues.newArrayDeque();
    tbp.add(this);
    while(!tbp.isEmpty()) {
      VoxelFace face = tbp.remove();
      if (face.dividor == null) {
        result.addAll(face.getFaceSegements());
      } else {
        for(int i = 0; i < 4; ++i) tbp.add(face.children[i]);
      }
    }
    faceSegmentCache = result;
    return result;
  }

}
