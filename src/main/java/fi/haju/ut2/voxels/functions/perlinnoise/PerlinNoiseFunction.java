package fi.haju.ut2.voxels.functions.perlinnoise;

import fi.haju.ut2.voxels.functions.GradientApproximatedFunction;

public class PerlinNoiseFunction extends GradientApproximatedFunction {

  private PerlinNoiseGenerator generator = new PerlinNoiseGenerator(4, 1, 12345);
  
  @Override public double value(double x, double y, double z) {
    return generator.getValueAt(x, y, z);
  }

}
