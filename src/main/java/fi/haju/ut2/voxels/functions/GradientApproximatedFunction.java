package fi.haju.ut2.voxels.functions;

import fi.haju.ut2.geometry.Position;

public abstract class GradientApproximatedFunction implements Function3d {

  @Override public abstract double value(double x, double y, double z);

  @Override public Position gradient(double x, double y, double z) {
    double d = 0.001;
    double xm = value(x-d, y, z);
    double xp = value(x+d, y, z);
    double ym = value(x, y-d, z);
    double yp = value(x, y+d, z);
    double zm = value(x, y, z-d);
    double zp = value(x, y, z+d);
    return new Position((xp-xm)/d/2, (yp-ym)/d/2, (zp-zm)/d/2);
  }

}
