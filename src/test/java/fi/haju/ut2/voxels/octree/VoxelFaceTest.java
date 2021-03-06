package fi.haju.ut2.voxels.octree;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import fi.haju.ut2.voxels.functions.Function3d;
import fi.haju.ut2.voxels.functions.SinoidalFunction;
import static fi.haju.ut2.voxels.octree.VoxelFace.face;
import static fi.haju.ut2.voxels.octree.VoxelNode.node;
import static fi.haju.ut2.voxels.octree.VoxelEdge.edge;
import static fi.haju.ut2.geometry.Position.pos;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.number.IsCloseTo.closeTo;

public class VoxelFaceTest {
  
  @Test public void face_division_creates_4_faces_with_valid_edges() {
    Function3d function = new SinoidalFunction();
    VoxelFace face = createTestFace(function);
    face.divide(function);
    
    assertThat(face.hasChildren(), is(true));
    assertThat(face.children.length, is(4));
    
    Set<VoxelEdge> edges = Sets.newHashSet();
    edges.addAll(Sets.newHashSet(face.edges));
    for(VoxelFace child : face.children) edges.addAll(Sets.newHashSet(child.edges));
    assertThat(edges.size(), is(16));
    for (VoxelEdge edge : edges) assertEdgeValid(edge);
  }

  @Test public void face_division_divides_edges_of_the_face() {
    Function3d function = new SinoidalFunction();
    VoxelFace face = createTestFace(function);
    face.divide(function);
    for (VoxelEdge edge : face.edges) assertThat(edge.hasChild(), is(true));
  }
  
  @Test public void divided_faces_are_correctly_connected() {
    Function3d function = new SinoidalFunction();
    VoxelFace face = createTestFace(function);
    face.divide(function);
    for (int i = 0; i < 4; ++i) assertFaceCorrectlyConnected(face.children[i]);
  }
  
  @Test public void face_parent_creation_creates_valid_parent_and_siblings() {
    Function3d function = new SinoidalFunction();
    for (int index = 0; index < 4; ++index) {
      VoxelFace face = createTestFace(function);
      VoxelFace parent = face.generateParent(index, function);
      assertFaceCorrectlyConnected(parent);
      assertFaceCorrectlyConnected(parent.children[0]);
      assertFaceCorrectlyConnected(parent.children[1]);
      assertFaceCorrectlyConnected(parent.children[2]);
      assertFaceCorrectlyConnected(parent.children[3]);
    }
  }
  
  @Test public void face_parent_edges_are_twice_as_long_as_child_edges() {
    Function3d function = new SinoidalFunction();
    for (int index = 0; index < 4; ++index) {
      VoxelFace face = createTestFace(function);
      double cl = face.edges[0].edgeVector().length();
      VoxelFace parent = face.generateParent(index, function);
      double pl = parent.edges[0].edgeVector().length();
      assertThat(pl, closeTo(2*cl, 0.001));
    }
  }
  
  public static void assertFaceCorrectlyConnected(VoxelFace face) {
    assertThat(face.edges[0].plus, is(face.edges[1].minus));
    assertThat(face.edges[1].plus, is(face.edges[2].plus));
    assertThat(face.edges[2].minus, is(face.edges[3].plus));
    assertThat(face.edges[3].minus, is(face.edges[0].minus));
  }
  
  private void assertEdgeValid(VoxelEdge edge) {
    assertThat(edge.minus.position.x, lessThanOrEqualTo(edge.plus.position.x));
    assertThat(edge.minus.position.y, lessThanOrEqualTo(edge.plus.position.y));
    assertThat(edge.minus.position.z, lessThanOrEqualTo(edge.plus.position.z));
  }

  private VoxelFace createTestFace(Function3d function) {
    VoxelNode[] nodes = new VoxelNode[4];
    VoxelEdge[] edges = new VoxelEdge[4];
    nodes[0] = node(pos(0,0,0), function);
    nodes[1] = node(pos(1,0,0), function);
    nodes[2] = node(pos(1,1,0), function);
    nodes[3] = node(pos(0,1,0), function);
    
    edges[0] = edge(nodes[0], nodes[1], function);
    edges[1] = edge(nodes[1], nodes[2], function);
    edges[2] = edge(nodes[3], nodes[2], function);
    edges[3] = edge(nodes[0], nodes[3], function);
    
    return face(edges[0], edges[1], edges[2], edges[3]);
  }
  
}
