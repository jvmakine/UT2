package fi.haju.ut2.voxels.functions;

import fi.haju.ut2.geometry.Position;

public final class SphericalFunction implements Function3d {

  private final double radius;
  private final Position origin;
  
  public SphericalFunction(double radius, Position origin) {
    this.radius = radius;
    this.origin = origin;
  }
  
  @Override public double value(double x, double y, double z) {
    return radius - Math.sqrt((x - origin.x)*(x - origin.x) + (y - origin.y)*(y - origin.y) + (z - origin.z)*(z - origin.z));
  }

  @Override public Position gradient(double x, double y, double z) {
    double dividor = Math.sqrt((x - origin.x)*(x - origin.x) + (y - origin.y)*(y - origin.y) + (z - origin.z)*(z - origin.z));
    return new Position(-(x-origin.x)/dividor, -(y-origin.y)/dividor, -(z-origin.z)/dividor).normalize();
  }

}
