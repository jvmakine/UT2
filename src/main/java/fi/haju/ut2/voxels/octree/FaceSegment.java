package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.geometry.Position;

public class FaceSegment {
  public final Position from;
  public final Position to;
  
  public FaceSegment(Position from, Position to) {
    this.from = from;
    this.to = to;
  }
  
}
