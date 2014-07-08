package fi.haju.ut2.voxels.octree;

import org.junit.Test;

import fi.haju.ut2.voxels.functions.SinoidalFunction;
import fi.haju.ut2.voxels.octree.VoxelOctree;
import static fi.haju.ut2.geometry.Position.pos;

import static fi.haju.ut2.voxels.octree.VoxelFaceTest.assertFaceCorrectlyConnected;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class VoxelOctreeTest {

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
    for (int i = 0; i < 7; ++i) {
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
  
  @Test public void neighbour_loop_returns_to_itself() {
    VoxelOctree tree = createOctree();
    tree.divideAllToLevel(3);
    VoxelOctree cell = tree.children[0].children[0];
    assertThat(cell.down().east().up().west(), is(cell));
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
