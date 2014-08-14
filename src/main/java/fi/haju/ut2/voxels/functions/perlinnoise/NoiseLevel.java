package fi.haju.ut2.voxels.functions.perlinnoise;

import java.util.HashMap;
import java.util.Random;

final class NoiseLevel {
  public final int sizeLog2;
  public final double amplitude;
  public final int seed;
  private final DataAccessor accessor = new DataAccessor();
  private HashMap<Long, DoubleArray3d> data = new HashMap<>();
  
  public NoiseLevel(int sizeLog2, double amplitude, int seed) {
    this.sizeLog2 = sizeLog2;
    this.amplitude = amplitude;
    this.seed = seed;
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
        long key = ((long)gx) ^ (((long)gz) << 21) ^ (((long)gy) << 42);
        array = data.get(key);
        if (array == null) {
          array = new DoubleArray3d(sizeLog2, new Random(seed ^ key));
          data.put(key, array);
        }
        lastx = gx;
        lasty = gy;
        lastz = gz;
      }
      return array.get(x - (gx << sizeLog2), y - (gy << sizeLog2), z - (gz << sizeLog2));
    }
    
  }
  
  public double getValueAt(double x, double y, double z) {
    int xi = (int)Math.floor(x);
    int yi = (int)Math.floor(y);
    int zi = (int)Math.floor(z);
    return amplitude * InterpolationUtil.interpolateLinear3d(x - xi, y - yi, z - zi,
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