package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.voxels.functions.Function3d;
import static fi.haju.ut2.voxels.octree.VoxelNode.node;
import static fi.haju.ut2.geometry.Position.average;
import static fi.haju.ut2.voxels.octree.VoxelEdge.edge;

public class VoxelFace {
  
  public VoxelNode dividor = null;
  public VoxelFace[] children; // 0 == --, 1 = -+, 2 = +-, 3 = ++
  public VoxelEdge[] edges = new VoxelEdge[4];
  
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
  
  public final boolean hasChildren() {
    return dividor != null;
  }
  
  public final void divide(Function3d function) {
    if(dividor != null) return;
    dividor = node(average(edges[0].minus.position, edges[1].plus.position), function);
    for(int i = 0; i < 4; ++i) {
      edges[i].divide(function);
    }
    children = new VoxelFace[4];
    children[0] = face(edges[0].minusChild, edge(edges[0].dividor, dividor), edge(edges[3].dividor, dividor), edges[3].minusChild);
    children[1] = face(edges[0].plusChild, edges[1].minusChild, edge(dividor, edges[1].dividor), children[0].edges[1]);
    children[2] = face(children[1].edges[2], edges[1].plusChild, edges[2].plusChild, edge(dividor, edges[2].dividor));
    children[3] = face(children[0].edges[2], children[2].edges[3], edges[2].minusChild, edges[3].plusChild);
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

}
