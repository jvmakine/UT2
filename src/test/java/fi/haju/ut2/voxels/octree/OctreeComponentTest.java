package fi.haju.ut2.voxels.octree;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fi.haju.ut2.voxels.octree.utils.OctreeConstructionUtils;
import static fi.haju.ut2.geometry.Position.pos;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class OctreeComponentTest {

  @Test public void loop_is_constructed_as_single_component() {
    Set<FaceSegment> loop = createLoop(10, 0);
    List<OctreeComponent> components = OctreeConstructionUtils.createComponentsFromSegments(loop);
    assertThat(components.size(), is(1));
    assertThat(components.get(0).vertices.size(), is(10));
  }
  
  @Test public void two_loops_are_constructed_as_separate_components() {
    Set<FaceSegment> segments = createLoop(5, 0);
    segments.addAll(createLoop(5, 10));
    
    List<OctreeComponent> components = OctreeConstructionUtils.createComponentsFromSegments(segments);
    assertThat(components.size(), is(2));
    assertThat(components.get(0).vertices.size(), is(5));
    assertThat(components.get(1).vertices.size(), is(5));
  }
  
  @Test public void bug1_is_fixed() {
    for (int i = 0; i < 100; ++i) {
    List<PositionWithNormal> pos = Lists.newArrayList(
      tpos(0, 0, 0),
      tpos(1, 1, 1),
      tpos(2, 2, 2),
      tpos(3, 3, 3),
      tpos(4, 4, 4),
      tpos(5, 5, 5)
    );
    Set<FaceSegment> segments = Sets.newHashSet(
      segment(pos.get(0), pos.get(1)),
      segment(pos.get(2), pos.get(3)),
      segment(pos.get(2), pos.get(1)),
      segment(pos.get(3), pos.get(4)),
      segment(pos.get(5), pos.get(4)),
      segment(pos.get(0), pos.get(5))
    );
    
    List<OctreeComponent> components = OctreeConstructionUtils.createComponentsFromSegments(segments);
    assertThat(components.size(), is(1));
    assertThat(components.get(0).vertices.size(), is(pos.size()));
    }
  }
  
  private static FaceSegment segment(PositionWithNormal p1, PositionWithNormal p2) {
    return new FaceSegment(p1, p2);
  }
  
  private static PositionWithNormal tpos(double x, double y, double z) {
    return new PositionWithNormal(pos(x,y,z), pos(1,1,1));
  }

  private Set<FaceSegment> createLoop(int length, int start) {
    Set<FaceSegment> result = Sets.newHashSet();
    PositionWithNormal startpos = new PositionWithNormal(pos(start,start,start), pos(1,0,0));
    PositionWithNormal last = startpos;
    for(int i = 1; i <length; ++i) {
      PositionWithNormal p = new PositionWithNormal(pos(i + start,i + start,i + start), pos(1,0,0));
      result.add(new FaceSegment(last, p));
      last = p;
    }
    result.add(new FaceSegment(last, startpos));
    return result;
  }
  
}
