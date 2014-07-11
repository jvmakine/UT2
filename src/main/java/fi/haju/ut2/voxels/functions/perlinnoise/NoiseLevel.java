package fi.haju.ut2.voxels.functions.perlinnoise;

import java.util.Random;

final class NoiseLevel {
  public final int sizeLog2;
  public final double amplitude;
  public final int seed;
  private final DataAccessor accessor = new DataAccessor();
  private DoubleArray3d[][] data;
  
  public NoiseLevel(int sizeLog2, double amplitude, int seed) {
    this.sizeLog2 = sizeLog2;
    this.amplitude = amplitude;
    this.seed = seed;
    this.data = new DoubleArray3d[((1 << 4) << 4) << 4][];
  }
  
  /**
   * Speed optimizer, refetch array from map only when needed
   */
  private final class DataAccessor {
    private int lastx = Integer.MIN_VALUE;
    private int lasty = Integer.MIN_VALUE;
    private int lastz = Integer.MIN_VALUE;
    private DoubleArray3d array = null;
    
    public double getValueAt(int x, int y, int z) {
      // efficient integer division with rounding towards -inf
      int gx = x >= 0 ? (x >> sizeLog2) : ~(~x >> sizeLog2);
      int gy = y >= 0 ? (y >> sizeLog2) : ~(~y >> sizeLog2);
      int gz = z >= 0 ? (z >> sizeLog2) : ~(~z >> sizeLog2);
      if(gx != lastx || gy != lasty || gz != lastz) {
        // first index from least significant bits
        int i = (gx & 0b1111) | ((gy << 4) & 0b11110000) | ((gz << 8) & 0b111100000000);
        DoubleArray3d[] a = data[i];
        if (a == null) {
          a = data[i] = new DoubleArray3d[(1 << 8)];
        }
        int j = ((gx >> 4) ^ (gy >> 4) ^ (gz >> 4)) & 0b1111111;
        if (a[j] == null) {
          a[j] = new DoubleArray3d(sizeLog2, new Random(seed ^ i << 16 ^ j));
        }
        array = a[j]; 
        lastx = gx;
        lasty = gy;
        lastz = gz;
      }
      return amplitude * array.get(x - (gx << sizeLog2), y - (gy << sizeLog2), z - (gz << sizeLog2));
    }
    
  }
  
  public double getValueAt(double x, double y, double z) {
    int xi = (int)Math.floor(x);
    int yi = (int)Math.floor(y);
    int zi = (int)Math.floor(z);
    return InterpolationUtil.interpolateLinear3d(x - xi, y - yi, z - zi,
        accessor.getValueAt(xi, yi, zi),
        accessor.getValueAt(xi + 1, yi, zi),
        accessor.getValueAt(xi, yi + 1, zi),
        accessor.getValueAt(xi + 1, yi + 1, zi),
        accessor.getValueAt(xi, yi, zi + 1),
        accessor.getValueAt(xi + 1, yi, zi + 1),
        accessor.getValueAt(xi, yi + 1, zi + 1),
        accessor.getValueAt(xi + 1, yi + 1, zi + 1)
    );
  }
    
}