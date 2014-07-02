package fi.haju.ut2.voxels.octree.utils;

import fi.haju.ut2.voxels.octree.VoxelOctree;

public final class OctreeConstructionUtils {
    
  public static void setupChildNeighbours(VoxelOctree children[], VoxelOctree neighbours[]) {
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
    to.neighbours[OctreeConstructionUtils.inverseNeigbourIndex(neigh)] = from;
  }
  
  private static final VoxelOctree childNeighbour(VoxelOctree neighbour, int child) {
    if (neighbour == null) return null;
    if (neighbour.children == null) return neighbour;
    return neighbour.children[child];
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
  
  private OctreeConstructionUtils() { }
  
}
