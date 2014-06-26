package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import static fi.haju.ut2.geometry.Position.average;

public final class VoxelOctree {

  public VoxelNode[] corners = new VoxelNode[8];
  public VoxelOctree[] children;
  public VoxelOctree[] neighbours = new VoxelOctree[6];
  public VoxelOctree parent;
  public Function3d function;
  public int depth;
  
  protected VoxelOctree() { }
  
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
  }
  
  public void divideAllToLevel(int level) {
    if(depth >= level) return;
    if (children == null) divide();
    for(int i = 0; i < 8; ++i) {
      children[i].divideAllToLevel(level);
    }
  }
  
  public void divide() {
    for(VoxelOctree n : neighbours) {
      if (n != null) {
        if (n.depth < depth) {
          n.divide();
        }
      }
    }
    
    children = new VoxelOctree[8];
    children[0] = new VoxelOctree();
    children[0].corners[0] = corners[0];
    for (int i = 1; i < 8; ++i) {
      children[0].corners[i] = node(average(corners[0].position, corners[i].position));
    }
    
    children[1] = new VoxelOctree();
    children[1].corners[0] = children[0].corners[1];
    children[1].corners[1] = corners[1];
    children[1].corners[2] = node(average(corners[1].position, corners[2].position));
    children[1].corners[3] = children[0].corners[2];
    children[1].corners[4] = children[0].corners[5];
    children[1].corners[5] = node(average(corners[1].position, corners[5].position));
    children[1].corners[6] = node(average(corners[1].position, corners[6].position));
    children[1].corners[7] = children[0].corners[6];
    
    children[2] = new VoxelOctree();
    children[2].corners[0] = children[1].corners[3];
    children[2].corners[1] = children[1].corners[2];
    children[2].corners[2] = corners[2];
    children[2].corners[3] = node(average(corners[2].position, corners[3].position));
    children[2].corners[4] = children[1].corners[7];
    children[2].corners[5] = children[1].corners[6];
    children[2].corners[6] = node(average(corners[2].position, corners[6].position));
    children[2].corners[7] = node(average(corners[2].position, corners[7].position));
    
    children[3] = new VoxelOctree();
    children[3].corners[0] = children[0].corners[3];
    children[3].corners[1] = children[0].corners[2];
    children[3].corners[2] = children[2].corners[3];
    children[3].corners[3] = corners[3];
    children[3].corners[4] = children[0].corners[7];
    children[3].corners[5] = children[0].corners[6];
    children[3].corners[6] = children[2].corners[7];
    children[3].corners[7] = node(average(corners[3].position, corners[7].position));
    
    children[4] = new VoxelOctree();
    children[4].corners[0] = children[0].corners[4];
    children[4].corners[1] = children[0].corners[5];
    children[4].corners[2] = children[0].corners[6];
    children[4].corners[3] = children[0].corners[7];
    children[4].corners[4] = corners[4];
    children[4].corners[5] = node(average(corners[4].position, corners[5].position));
    children[4].corners[6] = node(average(corners[4].position, corners[6].position));
    children[4].corners[7] = node(average(corners[4].position, corners[7].position));
    
    children[5] = new VoxelOctree();
    children[5].corners[0] = children[1].corners[4];
    children[5].corners[1] = children[1].corners[5];
    children[5].corners[2] = children[1].corners[6];
    children[5].corners[3] = children[1].corners[7];
    children[5].corners[4] = children[4].corners[5];
    children[5].corners[5] = corners[5];
    children[5].corners[6] = node(average(corners[5].position, corners[6].position));
    children[5].corners[7] = children[4].corners[6];
    
    children[6] = new VoxelOctree();
    children[6].corners[0] = children[2].corners[4];
    children[6].corners[1] = children[2].corners[5];
    children[6].corners[2] = children[2].corners[6];
    children[6].corners[3] = children[2].corners[7];
    children[6].corners[4] = children[5].corners[7];
    children[6].corners[5] = children[5].corners[6];
    children[6].corners[6] = corners[6];
    children[6].corners[7] = node(average(corners[6].position, corners[7].position));
    
    children[7] = new VoxelOctree();
    children[7].corners[0] = children[3].corners[4];
    children[7].corners[1] = children[3].corners[5];
    children[7].corners[2] = children[3].corners[6];
    children[7].corners[3] = children[3].corners[7];
    children[7].corners[4] = children[4].corners[7];
    children[7].corners[5] = children[4].corners[6];
    children[7].corners[6] = children[6].corners[7]; 
    children[7].corners[7] = corners[7];
    
    for(int i = 0; i < 8; ++i) {
      children[i].depth = depth + 1;
      children[i].parent = this;
      children[i].function = function;
    }
    
    neighbour(0, children[0], childNeighbour(neighbours[0], 4));
    neighbour(1, children[0], childNeighbour(neighbours[1], 1));
    neighbour(2, children[0], childNeighbour(neighbours[2], 3));
    neighbour(3, children[0], children[1]);
    neighbour(4, children[0], children[3]);
    neighbour(5, children[0], children[4]);
    
    neighbour(0, children[1], childNeighbour(neighbours[0], 5));
    neighbour(2, children[1], childNeighbour(neighbours[2], 2));
    neighbour(3, children[1], childNeighbour(neighbours[3], 0));
    neighbour(4, children[1], children[2]);
    neighbour(5, children[1], children[5]);
    
    neighbour(0, children[2], childNeighbour(neighbours[0], 6));
    neighbour(1, children[2], children[3]);
    neighbour(3, children[2], childNeighbour(neighbours[3], 3));
    neighbour(4, children[2], childNeighbour(neighbours[4], 1));
    neighbour(5, children[2], children[6]);
    
    neighbour(0, children[3], childNeighbour(neighbours[0], 7));
    neighbour(1, children[3], childNeighbour(neighbours[1], 2));
    neighbour(4, children[3], childNeighbour(neighbours[4], 0));
    neighbour(5, children[3], children[7]);
    
    neighbour(1, children[4], childNeighbour(neighbours[1], 5));
    neighbour(2, children[4], childNeighbour(neighbours[2], 7));
    neighbour(3, children[4], children[5]);
    neighbour(4, children[4], children[7]);
    neighbour(5, children[4], childNeighbour(neighbours[5], 0));
    
    neighbour(2, children[5], childNeighbour(neighbours[2], 6));
    neighbour(3, children[5], childNeighbour(neighbours[3], 4));
    neighbour(4, children[5], children[6]);
    neighbour(5, children[5], childNeighbour(neighbours[5], 1));
    
    neighbour(1, children[6], children[7]);
    neighbour(3, children[6], childNeighbour(neighbours[3], 7));
    neighbour(4, children[6], childNeighbour(neighbours[4], 5));
    neighbour(5, children[6], childNeighbour(neighbours[5], 2));
    
    neighbour(1, children[7], childNeighbour(neighbours[1], 6));
    neighbour(4, children[7], childNeighbour(neighbours[4], 4));
    neighbour(5, children[7], childNeighbour(neighbours[5], 3));
    
  }

  private final static void neighbour(int neigh, VoxelOctree from, VoxelOctree to) {
    if (to == null) return;
    from.neighbours[neigh] = to;
    to.neighbours[inverseNeigbourIndex(neigh)] = from;
  }
  
  private final static int inverseNeigbourIndex(int index) {
    switch(index) {
    case 0: return 5;
    case 1: return 3;
    case 2: return 4;
    case 3: return 1;
    case 4: return 2;
    case 5: return 0;
    }
    throw new IllegalArgumentException("Unknown neighbour index " + index);
  }
  
  private final VoxelOctree childNeighbour(VoxelOctree neighbour, int child) {
    if (neighbour == null) return null;
    if (neighbour.children == null) return neighbour;
    return neighbour.children[child];
  }
  
  private final VoxelNode node(Position pos) {
    return VoxelNode.node(pos, function);
  }
  
  
}
