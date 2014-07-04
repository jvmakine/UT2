package fi.haju.ut2.voxels.octree;

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
  
  
  
}
