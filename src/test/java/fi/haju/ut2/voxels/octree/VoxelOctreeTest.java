package fi.haju.ut2.voxels.octree;

import java.util.List;

import org.junit.Test;

import fi.haju.ut2.voxels.functions.GradientApproximatedFunction;
import fi.haju.ut2.voxels.functions.SinoidalFunction;
import fi.haju.ut2.voxels.octree.VoxelOctree;
import static fi.haju.ut2.geometry.Position.pos;
import static fi.haju.ut2.voxels.octree.VoxelFaceTest.assertFaceCorrectlyConnected;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;

public class VoxelOctreeTest {

  @Test public void correctly_detects_overlaps_with_sphere() {
    VoxelOctree tree = new VoxelOctree(pos(0,0,0), 50, new SinoidalFunction());
    assertThat(tree.overlapsSphere(pos(100,50,50), 40), is(false));    
    assertThat(tree.overlapsSphere(pos(100,50,50), 60), is(true));
    assertThat(tree.overlapsSphere(pos(10,10,10), 3), is(true));
  }
  
  @Test public void empty_octree_is_compressed_correctly() {
    VoxelOctree tree = new VoxelOctree(pos(0,0,0), 5, new GradientApproximatedFunction() {
      @Override public double value(double x, double y, double z) {
        return -1;
      }
    });
    tree.divideAllToLevel(3);
    tree.compress();
    assertThat(tree.children, is(nullValue()));
    for (int i = 0; i < 6; ++i) {
      assertThat(tree.faces[i].children, is(nullValue()));
    }
  }
  
  @Test public void returns_octrees_in_sphere() {
    VoxelOctree tree = new VoxelOctree(pos(0,0,0), 5, new SinoidalFunction());
    List<VoxelOctree> result = tree.treesInSphere(pos(0,0,0), 10.0, 2);
    assertThat(result.size(), greaterThan(0));
    for (VoxelOctree res : result) {
      assertThat(res.overlapsSphere(pos(0,0,0), 10.0), is(true));
    }
  }
  
  @Test public void divided_octree_has_correctly_connected_edges() {
    VoxelOctree tree = createOctree();
    tree.divide();
    assertCorrectlyConnected(tree);
    assertCorrectlyConnected(tree.children[0]);
    assertCorrectlyConnected(tree.children[1]);
    assertCorrectlyConnected(tree.children[2]);
    assertCorrectlyConnected(tree.children[3]);
    assertCorrectlyConnected(tree.children[4]);
    assertCorrectlyConnected(tree.children[5]);
    assertCorrectlyConnected(tree.children[6]);
    assertCorrectlyConnected(tree.children[7]);
  }
  
  @Test public void parent_octree_has_correctly_connected_edges() {
    for (int i = 0; i < 8; ++i) {
      VoxelOctree tree = createOctree().generateOctreeWithChild(i);
      assertCorrectlyConnected(tree);
      assertCorrectlyConnected(tree.children[0]);
      assertCorrectlyConnected(tree.children[1]);
      assertCorrectlyConnected(tree.children[2]);
      assertCorrectlyConnected(tree.children[3]);
      assertCorrectlyConnected(tree.children[4]);
      assertCorrectlyConnected(tree.children[5]);
      assertCorrectlyConnected(tree.children[6]);
      assertCorrectlyConnected(tree.children[7]);
      assertChildrenCorrectlyConnected(tree);
    }
  }
  
  @Test public void neighbours_connected_correctly_between_children() {
    VoxelOctree tree = createOctree();
    tree.divideAllToLevel(3);
    assertChildrenCorrectlyConnected(tree.children[0]);
  }
  
  @Test public void neighbour_loop_returns_to_itself() {
    VoxelOctree tree = createOctree();
    tree.divideAllToLevel(3);
    VoxelOctree cell = tree.children[0].children[0];
    assertThat(cell.down().east().up().west(), is(cell));
  }
  
  @Test public void children_returned_correctly_by_position() {
    VoxelOctree tree = new VoxelOctree(pos(-1,-1,-1), 2, new SinoidalFunction());
    tree.divideAllToLevel(2);
    assertThat(tree.getOctreeAtPosition(pos(-0.5, -0.5, -0.5), 1), is(tree.children[0]));
    assertThat(tree.getOctreeAtPosition(pos(0.5, -0.5, -0.5), 1), is(tree.children[1]));
    assertThat(tree.getOctreeAtPosition(pos(0.5, -0.5, 0.5), 1), is(tree.children[2]));
    assertThat(tree.getOctreeAtPosition(pos(-0.5, -0.5, 0.5), 1), is(tree.children[3]));
    
    assertThat(tree.getOctreeAtPosition(pos(-0.5, 0.5, -0.5), 1), is(tree.children[4]));
    assertThat(tree.getOctreeAtPosition(pos(0.5, 0.5, -0.5), 1), is(tree.children[5]));
    assertThat(tree.getOctreeAtPosition(pos(0.5, 0.5, 0.5), 1), is(tree.children[6]));
    assertThat(tree.getOctreeAtPosition(pos(-0.5, 0.5, 0.5), 1), is(tree.children[7]));
  }
  
  @Test public void multiple_divisions_correctly_connected() {
    VoxelOctree tree = createOctree();
    tree.divideAllToLevel(3);
    VoxelOctree cell = tree.children[0].children[0].children[0];
    for (int i = 0; i < 7; ++i) cell = cell.east();
    for (int i = 0; i < 7; ++i) cell = cell.south();
    for (int i = 0; i < 7; ++i) cell = cell.down();
    for (int i = 0; i < 7; ++i) cell = cell.west();
    for (int i = 0; i < 7; ++i) cell = cell.up();
    for (int i = 0; i < 7; ++i) cell = cell.north();
    assertThat(cell, is(tree.children[0].children[0].children[0]));
  }
  
  @Test public void new_siblings_returned_correctly_by_position() {
    VoxelOctree tree = new VoxelOctree(pos(-1,-1,-1), 2, new SinoidalFunction());
    VoxelOctree n = tree.getOctreeAtPosition(pos(1.5, 0, 0), 0);
    assertThat(n.faces[1].minus, is(tree));
    assertThat(n, is(tree.faces[3].plus));
    n = tree.getOctreeAtPosition(pos(0, 0, 1.5), 0);
    assertThat(n.faces[2].minus, is(tree));
    assertThat(n, is(tree.faces[4].plus));
  }
  
  @Test public void neighbours_inherited_correctly_from_grandparent() {
    VoxelOctree tree = createOctree();
    VoxelOctree parent = tree.generateOctreeWithChild(0);
    assertChildrenCorrectlyConnected(parent);
    parent = parent.generateOctreeWithChild(1);
    assertChildrenCorrectlyConnected(parent);
    parent.children[0].divide();
    assertThat(parent.children[0].children[1].east().depth, is(tree.depth));
  }
  
  private void assertChildrenCorrectlyConnected(VoxelOctree tree) {
    VoxelOctree cell = tree.children[0];
    assertThat(cell.down(), is(tree.children[4]));
    assertThat(cell.east(), is(tree.children[1]));
    assertThat(cell.south(), is(tree.children[3]));
    cell = tree.children[6];
    assertThat(cell.up(), is(tree.children[2]));
    assertThat(cell.north(), is(tree.children[5]));
    assertThat(cell.west(), is(tree.children[7]));
  }
    
  private void assertCorrectlyConnected(VoxelOctree tree) {
    assertFaceCorrectlyConnected(tree.faces[0]);
    assertFaceCorrectlyConnected(tree.faces[1]);
    assertFaceCorrectlyConnected(tree.faces[2]);
    assertFaceCorrectlyConnected(tree.faces[3]);
    assertFaceCorrectlyConnected(tree.faces[4]);
    assertFaceCorrectlyConnected(tree.faces[5]);
    
    //Upper corners
    assertThat(tree.faces[0].corner30(), is(tree.faces[1].corner30()));
    assertThat(tree.faces[0].corner30(), is(tree.faces[2].corner30()));
    assertThat(tree.faces[0].corner01(), is(tree.faces[3].corner30()));
    assertThat(tree.faces[0].corner01(), is(tree.faces[2].corner01()));
    assertThat(tree.faces[0].corner12(), is(tree.faces[4].corner01()));
    assertThat(tree.faces[0].corner12(), is(tree.faces[3].corner01()));
    assertThat(tree.faces[0].corner23(), is(tree.faces[1].corner01()));
    assertThat(tree.faces[0].corner23(), is(tree.faces[4].corner30()));
    //Lower corners
    assertThat(tree.faces[5].corner30(), is(tree.faces[1].corner23()));
    assertThat(tree.faces[5].corner30(), is(tree.faces[2].corner23()));
    assertThat(tree.faces[5].corner01(), is(tree.faces[2].corner12()));
    assertThat(tree.faces[5].corner01(), is(tree.faces[3].corner23()));
    assertThat(tree.faces[5].corner12(), is(tree.faces[3].corner12()));
    assertThat(tree.faces[5].corner12(), is(tree.faces[4].corner12()));
    assertThat(tree.faces[5].corner23(), is(tree.faces[1].corner12()));
    assertThat(tree.faces[5].corner23(), is(tree.faces[4].corner23()));
  }

  private VoxelOctree createOctree() {
    return new VoxelOctree(pos(50,50,50), 100, new SinoidalFunction());
  }
    
}
