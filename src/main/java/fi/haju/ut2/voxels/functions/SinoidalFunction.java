package fi.haju.ut2.voxels.functions;

import static java.lang.Math.sin;

public final class SinoidalFunction implements Function3d {

  @Override public double value(double x, double y, double z) {
    return sin(x) + sin(y) + sin(z);
  }

}
