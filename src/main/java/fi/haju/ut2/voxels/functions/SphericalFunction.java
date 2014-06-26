package fi.haju.ut2.voxels.functions;

public final class SphericalFunction implements Function3d {

  private final double radius;
  
  public SphericalFunction(double radius) {
    this.radius = radius;
  }
  
  @Override public double value(double x, double y, double z) {
    return radius - Math.sqrt(x*x + y*y + z*z);
  }

}
