package fi.haju.ut2.voxels.functions;

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import fi.haju.ut2.geometry.Position;

public final class SinoidalFunction implements Function3d {

  @Override public double value(double x, double y, double z) {
    return sin(x) + sin(y) + sin(z);
  }

  @Override
  public Position gradient(double x, double y, double z) {
    return new Position(cos(x), cos(y), cos(z)).normalize();
  }

}
