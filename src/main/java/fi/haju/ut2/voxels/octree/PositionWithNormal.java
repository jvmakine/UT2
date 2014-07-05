package fi.haju.ut2.voxels.octree;

import java.util.List;

import com.google.common.collect.Lists;

import fi.haju.ut2.geometry.Position;

public final class PositionWithNormal {
  public final Position position;
  public final Position normal;

  public PositionWithNormal(Position position, Position normal) {
    this.position = position;
    this.normal = normal;
  }

  @Override public String toString() {
    return position.toString();
  }
  
  public static PositionWithNormal average(List<PositionWithNormal> points) {
    List<Position> positions = Lists.newArrayList();
    List<Position> normals = Lists.newArrayList();
    for(PositionWithNormal p : points) {
      positions.add(p.position);
      normals.add(p.normal);
    }
    return new PositionWithNormal(Position.average(positions), Position.average(normals).normalize());
  }
  
  
}
