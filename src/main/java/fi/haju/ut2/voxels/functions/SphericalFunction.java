package fi.haju.ut2.voxels.functions;

import fi.haju.ut2.geometry.Position;

public final class SphericalFunction implements Function3d {

  private final double radius;
  
  public SphericalFunction(double radius) {
    this.radius = radius;
  }
  
  @Override public double value(double x, double y, double z) {
    return radius - Math.sqrt(x*x + y*y + z*z);
  }

  @Override
  public Position gradient(double x, double y, double z) {
    double dividor = Math.sqrt(x*x + y*y + z*z);
    return new Position(-x/dividor, -y/dividor, -z/dividor).normalize();
  }

}
