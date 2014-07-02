package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.voxels.functions.Function3d;

import static fi.haju.ut2.voxels.octree.VoxelNode.node;
import static fi.haju.ut2.geometry.Position.average;

public class VoxelEdge {
  
  public final VoxelNode minus;
  public final VoxelNode plus;
  
  public VoxelEdge minusChild;
  public VoxelEdge plusChild;
  public VoxelNode dividor = null;
  
  public VoxelEdge(VoxelNode from, VoxelNode to) {
    this.minus = from;
    this.plus = to;
  }
  
  public static VoxelEdge edge(VoxelNode from, VoxelNode to) {
    return new VoxelEdge(from, to);
  }
  
  public final boolean hasChild() {
    return dividor != null;
  }
  
  public final void divide(Function3d function) {
    if(dividor != null) return;
    dividor = node(average(minus.position, plus.position), function);
    minusChild = edge(minus, dividor);
    plusChild = edge(dividor, plus);
  }
  
}
