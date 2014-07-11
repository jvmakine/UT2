package fi.haju.ut2.voxels.functions.perlinnoise;

import java.io.Serializable;
import java.util.Random;

public final class DoubleArray3d implements Serializable {
  private static final long serialVersionUID = 1L;

  private final double[] data;
  private final int sizeLog2;

  public DoubleArray3d(int sizeLog2, Random random) {
    this.sizeLog2 = sizeLog2;
    this.data = new double[((1 << sizeLog2) << sizeLog2) << sizeLog2];
    for (int x = 0; x < (1 << sizeLog2); ++x) {
      for (int y = 0; y < (1 << sizeLog2); ++y) {
        for (int z = 0; z < (1 << sizeLog2); ++z) {
          set(x, y, z, 2.0f * random.nextFloat() - 1.0f);
        }
      }
    }
  }

  public void set(int x, int y, int z, double value) {
    data[x + (y << sizeLog2) + ((z << sizeLog2) << sizeLog2)] = value;
  }

  public double get(int x, int y, int z) {
    return data[x + (y << sizeLog2) + ((z << sizeLog2) << sizeLog2)];
  }

}
