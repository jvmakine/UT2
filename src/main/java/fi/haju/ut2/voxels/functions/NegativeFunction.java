package fi.haju.ut2.voxels.functions;

import fi.haju.ut2.geometry.Position;

public class NegativeFunction implements Function3d {

  @Override public double value(double x, double y, double z) {
    return -1;
  }

  @Override public Position gradient(double x, double y, double z) {
    return new Position(0, 1, 0);
  }

}
