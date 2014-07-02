package fi.haju.ut2.voxels.octree.utils;

import static fi.haju.ut2.voxels.octree.VoxelEdge.edge;
import static fi.haju.ut2.voxels.octree.VoxelFace.face;
import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import fi.haju.ut2.voxels.octree.VoxelEdge;
import fi.haju.ut2.voxels.octree.VoxelFace;
import fi.haju.ut2.voxels.octree.VoxelNode;
import fi.haju.ut2.voxels.octree.VoxelOctree;

import static fi.haju.ut2.voxels.octree.VoxelNode.node;

public final class OctreeConstructionUtils {
    
  public static VoxelFace[] createInitialFaces(Position upperLeftBackCorner, double side, Function3d function) {
    VoxelFace[] faces = new VoxelFace[6];
    VoxelNode[] corners = new VoxelNode[8];
    // Upper side
    corners[0] = node(upperLeftBackCorner, function);
    corners[1] = node(corners[0].position.xplus(side), function);
    corners[2] = node(corners[1].position.zplus(side), function);
    corners[3] = node(corners[0].position.zplus(side), function);
    // Lower side
    corners[4] = node(corners[0].position.yplus(side), function);
    corners[5] = node(corners[1].position.yplus(side), function);
    corners[6] = node(corners[2].position.yplus(side), function);
    corners[7] = node(corners[3].position.yplus(side), function);
    
    VoxelEdge[] edges = new VoxelEdge[12];
    edges[0] = edge(corners[0], corners[1]);
    edges[1] = edge(corners[1], corners[2]);
    edges[2] = edge(corners[3], corners[2]);
    edges[3] = edge(corners[0], corners[3]);
    for(int i = 0; i < 4; ++i) edges[i + 4] = edge(corners[i], corners[i+4]);
    edges[8] = edge(corners[4], corners[5]);
    edges[9] = edge(corners[5], corners[6]);
    edges[10] = edge(corners[7], corners[6]);
    edges[11] = edge(corners[4], corners[7]);
    
    faces[0] = face(edges[0], edges[1], edges[2], edges[3]);
    faces[1] = face(edges[3], edges[7], edges[11], edges[4]);
    faces[2] = face(edges[0], edges[5], edges[8], edges[4]);
    faces[3] = face(edges[1], edges[6], edges[9], edges[5]);
    faces[4] = face(edges[2], edges[6], edges[10], edges[7]);
    faces[5] = face(edges[8], edges[9], edges[10], edges[11]);
    return faces;
  }
  
  public static VoxelOctree[] constructChildren(VoxelNode dividor, VoxelFace[] faces) {
    VoxelEdge nedge[] = new VoxelEdge[6];
    nedge[0] = edge(faces[0].dividor, dividor);
    nedge[1] = edge(faces[1].dividor, dividor);
    nedge[2] = edge(faces[2].dividor, dividor);
    nedge[3] = edge(dividor, faces[3].dividor);
    nedge[4] = edge(dividor, faces[4].dividor);
    nedge[5] = edge(dividor, faces[5].dividor);
    
    VoxelEdge up[] = faces[0].dividingEdges();
    VoxelEdge west[] = faces[1].dividingEdges();
    VoxelEdge north[] = faces[2].dividingEdges();
    VoxelEdge east[] = faces[3].dividingEdges();
    VoxelEdge south[] = faces[4].dividingEdges();
    VoxelEdge down[] = faces[5].dividingEdges();
    
    VoxelOctree[] children = new VoxelOctree[8];
    
    children[0] = new VoxelOctree();
    children[0].faces[0] = faces[0].children[0];
    children[0].faces[1] = faces[1].children[0];
    children[0].faces[2] = faces[2].children[0];
    children[0].faces[3] = face(up[0], nedge[0], nedge[2], north[0]);
    children[0].faces[4] = face(up[3], nedge[0], nedge[1], west[0]);
    children[0].faces[5] = face(north[3], nedge[2], nedge[1], west[3]);
    
    children[1] = new VoxelOctree();
    children[1].faces[0] = faces[0].children[1];
    children[1].faces[1] = children[0].faces[3];
    children[1].faces[2] = faces[2].children[1];
    children[1].faces[3] = faces[3].children[0];
    children[1].faces[4] = face(up[1], east[0], nedge[3], nedge[0]);
    children[1].faces[5] = face(north[1], east[3], nedge[3], nedge[2]);
    
    children[2] = new VoxelOctree();
    children[2].faces[0] = faces[0].children[2];
    children[2].faces[1] = face(up[2], south[0], nedge[4], nedge[0]);
    children[2].faces[2] = children[1].faces[4];
    children[2].faces[3] = faces[3].children[1];
    children[2].faces[4] = faces[4].children[1];
    children[2].faces[5] = face(nedge[3], east[1], south[1], nedge[4]);
    
    children[3] = new VoxelOctree();
    children[3].faces[0] = faces[0].children[3];
    children[3].faces[1] = faces[1].children[1];
    children[3].faces[2] = children[0].faces[4];
    children[3].faces[3] = children[2].faces[1];
    children[3].faces[4] = faces[4].children[0];
    children[3].faces[5] = face(nedge[1], nedge[4], south[3], west[1]);
    
    children[4] = new VoxelOctree();
    children[4].faces[0] = children[0].faces[5];
    children[4].faces[1] = faces[1].children[3];
    children[4].faces[2] = faces[2].children[3];
    children[4].faces[3] = face(nedge[2], nedge[5], down[0], north[2]);
    children[4].faces[4] = face(nedge[1], nedge[5], down[3], west[2]);
    children[4].faces[5] = faces[5].children[0];
    
    children[5] = new VoxelOctree();
    children[5].faces[0] = children[1].faces[5];
    children[5].faces[1] = children[4].faces[3];
    children[5].faces[2] = faces[2].children[2];
    children[5].faces[3] = faces[3].children[3];
    children[5].faces[4] = face(nedge[3], east[2], down[1], nedge[5]);
    children[5].faces[5] = faces[5].children[1];
    
    children[6] = new VoxelOctree();
    children[6].faces[0] = children[2].faces[5];
    children[6].faces[1] = face(nedge[4], south[2], down[2], nedge[5]);
    children[6].faces[2] = children[5].faces[4];
    children[6].faces[3] = faces[3].children[2];
    children[6].faces[4] = faces[4].children[2];
    children[6].faces[5] = faces[5].children[2];
    
    children[7] = new VoxelOctree();
    children[7].faces[0] = children[3].faces[5];
    children[7].faces[1] = faces[1].children[2];
    children[7].faces[2] = children[4].faces[4];
    children[7].faces[3] = children[6].faces[1];
    children[7].faces[4] = faces[4].children[3];
    children[7].faces[5] = faces[5].children[3];
    
    return children;
  }
  
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
