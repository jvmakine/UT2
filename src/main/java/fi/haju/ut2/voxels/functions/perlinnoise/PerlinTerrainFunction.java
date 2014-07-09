package fi.haju.ut2.voxels.functions.perlinnoise;

import java.util.Random;

import fi.haju.ut2.voxels.functions.GradientApproximatedFunction;

public class PerlinTerrainFunction extends GradientApproximatedFunction {

  private PerlinNoiseGenerator generator = new PerlinNoiseGenerator(6, 2, new Random().nextInt());
  
  @Override public double value(double x, double y, double z) {
    return generator.getValueAt(x, y, z) - 2*y;
  }

}
