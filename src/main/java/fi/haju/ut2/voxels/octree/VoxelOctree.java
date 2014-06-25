package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.geometry.Position;

import static fi.haju.ut2.voxels.octree.VoxelNode.node;
import static fi.haju.ut2.geometry.Position.average;

public final class VoxelOctree {

  public VoxelNode[] corners = new VoxelNode[8];
  public VoxelOctree[] children;
  public VoxelOctree parent;
  public int depth;
  
  protected VoxelOctree() { }
  
  public VoxelOctree(Position upperLeftBackCorner, double side) {
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
  
  public void divide() {
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
    }
  }

  
  
}
