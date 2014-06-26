package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import fi.haju.ut2.voxels.octree.utils.OctreeConstructionUtils;

public final class VoxelOctree {

  public VoxelNode[] corners = new VoxelNode[8];
  public VoxelOctree[] children;
  public VoxelOctree[] neighbours = new VoxelOctree[6];
  public VoxelOctree parent;
  public Position vertexPosition = null;
  public Function3d function;
  public int depth;
  
  public VoxelOctree() { }
  
  public VoxelOctree(Position upperLeftBackCorner, double side, Function3d function) {
    this.function = function;
    depth = 0;
    // Upper side
    corners[0] = node(upperLeftBackCorner);
    corners[1] = node(corners[0].position.xplus(side));
    corners[2] = node(corners[1].position.zplus(side));
    corners[3] = node(corners[0].position.zplus(side));
    // Lower side
    corners[4] = node(corners[0].position.yplus(side));
    corners[5] = node(corners[1].position.yplus(side));
    corners[6] = node(corners[2].position.yplus(side));
    corners[7] = node(corners[3].position.yplus(side));
    
    updateVertex();
  }
  
  public Position center() {
    return Position.average(corners[0].position, corners[7].position);
  }
  
  public void divideAllToLevel(int level) {
    if(depth >= level) return;
    if (children == null) divide();
    for(int i = 0; i < 8; ++i) {
      children[i].divideAllToLevel(level);
    }
  }
  
  public void divide() {
    if(children != null) return;
    for(VoxelOctree n : neighbours) {
      if (n != null) {
        if (n.depth < depth) {
          n.divide();
        }
      }
    }
    
    children = OctreeConstructionUtils.createChildren(corners, function);
    OctreeConstructionUtils.setupChildNeighbours(children, neighbours);
    
    for(int i = 0; i < 8; ++i) {
      children[i].depth = depth + 1;
      children[i].parent = this;
      children[i].function = function;
      children[i].updateVertex();
    }
    
  }
  
  private void updateVertex() {
    boolean val = corners[0].positive;
    boolean hasVertex = false;
    for (int i = 1; i < 8; ++i) {
      if (corners[i].positive != val) {
        hasVertex = true;
        break;
      }
    }
    if (hasVertex) {
      vertexPosition = center(); 
    } else {
      vertexPosition = null;
    }
  }
  
  private final VoxelNode node(Position pos) {
    return VoxelNode.node(pos, function);
  }
  
  
}
