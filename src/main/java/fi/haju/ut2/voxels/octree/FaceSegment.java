package fi.haju.ut2.voxels.octree;

public class FaceSegment {
  public final PositionWithNormal from;
  public final PositionWithNormal to;
  
  public FaceSegment(PositionWithNormal from, PositionWithNormal to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public String toString() {
    return "segment[" + from + " -> " + to + "]";
  }
  
}
