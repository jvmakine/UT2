package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import static fi.haju.ut2.voxels.octree.VoxelNode.node;
import static fi.haju.ut2.geometry.Position.average;

public class VoxelEdge {
  
  private static final int INTERSECTION_ACCURACY = 4;
  
  public final VoxelNode minus;
  public final VoxelNode plus;
  public VoxelEdge parent = null;
  
  public VoxelEdge minusChild;
  public VoxelEdge plusChild;
  public VoxelNode dividor = null;
  
  private PositionWithNormal vertex;
  
  public VoxelEdge(VoxelNode from, VoxelNode to, Function3d function) {
    this.minus = from;
    this.plus = to;
    if(from.positive != to.positive) {
      Position pos = interpolateVertex(from, to, function); 
      vertex = new PositionWithNormal(pos, function.gradient(pos.x, pos.y, pos.z).inverse());
    }
  }

  private static Position interpolateVertex(VoxelNode from, VoxelNode to, Function3d function) {
    boolean frompos = from.positive;
    Position fromv = from.position;
    Position tov = to.position;
    Position vertex = Position.average(from.position, to.position);
    for(int i = 0; i < INTERSECTION_ACCURACY; ++i) {
      boolean pos = function.value(vertex.x, vertex.y, vertex.z) >= 0;
      if (pos != frompos) {
        tov = vertex;
      } else {
        fromv = vertex;
        frompos = pos;
      }
      vertex = Position.average(fromv, tov);
    }
    return vertex;
  }
  
  public PositionWithNormal vertex() {
    if(hasChild()) {
      PositionWithNormal mv = minusChild.vertex();
      PositionWithNormal pv = plusChild.vertex();
      return mv == null ? pv : mv;
    } else {
      return vertex;
    }
  }
  
  
  public static VoxelEdge edge(VoxelNode from, VoxelNode to, Function3d function) {
    return new VoxelEdge(from, to, function);
  }
  
  public final boolean hasChild() {
    return dividor != null;
  }
  
  public final void divide(Function3d function) {
    if(dividor != null) return;
    dividor = node(average(minus.position, plus.position), function);
    minusChild = edge(minus, dividor, function);
    plusChild = edge(dividor, plus, function);
    minusChild.parent = this;
    plusChild.parent = this;
  }
  
}
