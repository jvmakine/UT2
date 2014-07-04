package fi.haju.ut2.voxels.functions;

import fi.haju.ut2.geometry.Position;

public interface Function3d {
  double value(double x, double y, double z);
  Position gradient(double x, double y, double z);
}
