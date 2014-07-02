package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import fi.haju.ut2.voxels.octree.utils.OctreeConstructionUtils;
import static fi.haju.ut2.voxels.octree.VoxelFace.face;
import static fi.haju.ut2.voxels.octree.VoxelEdge.edge;

/**
 * Restricted octree, the depth of neighbors may differ only by one
 * The faces of octree and edges of faces are indexed as described in voxel_cube.png
 */
public final class VoxelOctree {

  public VoxelFace[] faces = new VoxelFace[6];
  public VoxelOctree[] children;
  public VoxelNode dividor;
  public VoxelOctree[] neighbours = new VoxelOctree[6];
  public VoxelOctree parent;
  public Position vertexPosition = null;
  public Function3d function;
  public int depth;
  
  public VoxelOctree() { }
  
  public VoxelOctree(Position upperLeftBackCorner, double side, Function3d function) {
    this.function = function;
    depth = 0;
    VoxelNode[] corners = new VoxelNode[8];
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
  }
  
  public Position center() {
    return Position.average(faces[0].edges[0].minus.position, faces[5].edges[2].plus.position);
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
    divideNeighboursWithLesserDepth();
    
    for(VoxelFace face : faces) face.divide(function);
    dividor = node(center());
    
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
    
    children = new VoxelOctree[8];
    
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
    
    for(int i = 0; i < 8; ++i) {
      children[i].depth = depth + 1;
      children[i].parent = this;
      children[i].function = function;
    }
    
    OctreeConstructionUtils.setupChildNeighbours(children, neighbours);
    
  }

  private void divideNeighboursWithLesserDepth() {
    for(VoxelOctree n : neighbours) {
      if (n != null) {
        if (n.depth < depth) {
          n.divide();
        }
      }
    }
  }
    
  private final VoxelNode node(Position pos) {
    return VoxelNode.node(pos, function);
  }
  
  public VoxelOctree up() { return neighbours[0]; }
  public VoxelOctree west() { return neighbours[1]; }
  public VoxelOctree north() { return neighbours[2]; }
  public VoxelOctree east() { return neighbours[3]; }
  public VoxelOctree south() { return neighbours[4]; }
  public VoxelOctree down() { return neighbours[5]; }
  
}
