package fi.haju.ut2.voxels.functions.perlinnoise;

import java.io.Serializable;
import java.util.Random;

public final class DoubleArray3d implements Serializable {
  private static final long serialVersionUID = 1L;

  private final double[] data;
  private final int width;
  private final int height;
  private final int depth;

  public static interface Initializer {
    double getValue(int x, int y, int z);
  }

  public DoubleArray3d(int width, int height, int depth) {
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.data = new double[width * height * depth];
  }

  public DoubleArray3d(int width, int height, int depth, Initializer initializer) {
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.data = new double[width * height * depth];
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        for (int z = 0; z < depth; ++z) {
          set(x, y, z, initializer.getValue(x, y, z));
        }
      }
    }
  }
  
  public DoubleArray3d(int width, int height, int depth, Random random) {
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.data = new double[width * height * depth];
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        for (int z = 0; z < depth; ++z) {
          set(x, y, z, 2.0f * random.nextFloat() - 1.0f);
        }
      }
    }
  }

  public void set(int x, int y, int z, double value) {
    data[getIndex(x, y, z)] = value;
  }

  public void add(int x, int y, int z, double value) {
    data[getIndex(x, y, z)] += value;
  }

  private int getIndex(int x, int y, int z) {
    return x + y * width + z * width * height;
  }

  public boolean isInside(int x, int y, int z) {
    return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
  }

  public double get(int x, int y, int z) {
    return data[getIndex(x, y, z)];
  }

  public double getInterpolated(double tx, double ty, double tz) {
    int tw = width;
    int td = depth;

    int x = (int) tx;
    int y = (int) ty;
    int z = (int) tz;

    double xt = tx - x;
    double yt = ty - y;
    double zt = tz - z;

    return InterpolationUtil.interpolateLinear3d(
        xt, yt, zt,
        data[x + y * tw + z * tw * td],
        data[x + 1 + y * tw + z * tw * td],
        data[x + y * tw + tw + z * tw * td],
        data[x + 1 + y * tw + tw + z * tw * td],
        data[x + y * tw + (z + 1) * tw * td],
        data[x + 1 + y * tw + (z + 1) * tw * td],
        data[x + y * tw + tw + (z + 1) * tw * td],
        data[x + 1 + y * tw + tw + (z + 1) * tw * td]);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getDepth() {
    return depth;
  }

}
