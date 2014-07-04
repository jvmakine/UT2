package fi.haju.ut2.voxels.octree;

import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class OctreeComponent {
  public LinkedList<PositionWithNormal> vertices = Lists.newLinkedList();

  @Override
  public String toString() {
    return "component [" + StringUtils.join(vertices, ",") + "]";
  }
  
  
  
}
