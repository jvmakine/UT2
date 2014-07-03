package fi.haju.ut2.voxels.octree;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.octree.utils.OctreeConstructionUtils;

import static fi.haju.ut2.geometry.Position.pos;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class OctreeComponentTest {

  @Test public void loop_is_constructed_as_single_component() {
    Set<FaceSegment> loop = createLoop(50);
    List<OctreeComponent> components = OctreeConstructionUtils.createComponentsFromSegments(loop);
    assertThat(components.size(), is(1));
    assertThat(components.get(0).vertices.size(), is(50));
  }

  private Set<FaceSegment> createLoop(int length) {
    Set<FaceSegment> result = Sets.newHashSet();
    Position start = pos(0,0,0);
    Position last = start;
    for(int i = 1; i <length; ++i) {
      Position p = pos(i,i,i);
      result.add(new FaceSegment(last, p));
      last = p;
    }
    result.add(new FaceSegment(last, start));
    return result;
  }
  
}
