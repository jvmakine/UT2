package fi.haju.ut2.voxels.octree;

import org.junit.Test;

import fi.haju.ut2.voxels.octree.VoxelOctree;

import static fi.haju.ut2.geometry.Position.pos;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VoxelOctreeTest {

  @Test public void octree_division_greates_subtrees_of_half_side_length() {
    VoxelOctree tree = new VoxelOctree(pos(50,50,50), 100);
    tree.divide();
    for(int i = 0; i < 8; ++i) {
      double side = tree.children[i].corners[0].position.distance(tree.children[i].corners[1].position);  
      assertEquals(50, side, 0.01);
    }
  }
  
  @Test public void depth_difference_between_neighbours_can_only_be_one() {
    VoxelOctree tree = new VoxelOctree(pos(50,50,50), 100);
    tree.divide();
    tree.children[0].divide();
    tree.children[0].children[1].divide();
    assertNotNull(tree.children[1].children);
  }
    
}
