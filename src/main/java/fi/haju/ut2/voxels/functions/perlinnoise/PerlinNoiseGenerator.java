package fi.haju.ut2.voxels.functions.perlinnoise;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class PerlinNoiseGenerator {
  private static final double LEVEL_AMPLITUDE_MULTIPLIER = 5.0f;
  private final int numberOfLevels;
  private final int baseMapSizeLog2;
  private final int seed;
  private final NoiseLevel[] levels; 
  
  private final static class NoiseLevel {
    public final int sizeLog2;
    public final double amplitude;
    public final Map<Vector3i, DoubleArray3d> data = Maps.newHashMap();
    public final int seed;
    private final DataAccessor accessor = new DataAccessor();
    
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
          Vector3i pos = new Vector3i(gx, gy, gz);
          makeIfDoesNotExist(pos);
          array = data.get(pos); 
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
    
    private void makeIfDoesNotExist(Vector3i pos) {
      if(data.containsKey(pos)) return;
      int size = 1 << sizeLog2;
      data.put(pos, new DoubleArray3d(size, size, size, new Random(seed ^ pos.hashCode())));
    }
    
  }
  
  public PerlinNoiseGenerator(int levels, int baseMapSizeLog2, int seed) {
    this.numberOfLevels = levels;
    this.baseMapSizeLog2 = baseMapSizeLog2;
    this.seed = seed;
    this.levels = new NoiseLevel[levels];
  }
  
  /**
   * returns a value at the given coordinates
   */
  public final double getValueAt(double x, double y, double z) {
    double value = 0.0f;
    int size = 1 << baseMapSizeLog2;
    for(int level = 1; level <= numberOfLevels; ++level) {
      NoiseLevel noise = levels[level-1];
      if(noise == null) {
        noise = new NoiseLevel(4, level*LEVEL_AMPLITUDE_MULTIPLIER, seed ^ level);
        levels[level-1] = noise;
      }
      value += noise.getValueAt(x/size, y/size, z/size);
      size <<= 1;
    }
    return value;
  }
    
  /**
   * returns a value that is guaranteed to be above any value in the noise map  
   */
  public double getMaxValue() {
    return (double)Math.pow(LEVEL_AMPLITUDE_MULTIPLIER, numberOfLevels);
  }
  
  /**
   * returns a value that is guaranteed to be below any value in the noise map  
   */
  public double getMinValue() {
    return getMaxValue() * -1;
  }
  
  /**
   * returns a value that is guaranteed to be below any value within a cube with corner at given point and given edge length 
   */
  public double getMinValue(Vector3i corner, int edge) {
    List<Vector3i> corners = getCorners(corner, edge);
    double sum = 0;
    Vector3i localPos = null;
    int sizeLog2 = baseMapSizeLog2;
    for(int i = 1; i <= numberOfLevels; ++i) {
      double min = Float.MAX_VALUE;
      NoiseLevel level = levels[i-1];
      if(level == null) {
        level = new NoiseLevel(4, i*LEVEL_AMPLITUDE_MULTIPLIER, seed ^ i);
        levels[i-1] = level;
      }
      int size = 1 << sizeLog2;
      for(Vector3i v : corners) {
        int gx = v.x >= 0 ? (v.x >> sizeLog2) : ~(~v.x >> sizeLog2);
        int gy = v.y >= 0 ? (v.y >> sizeLog2) : ~(~v.y >> sizeLog2);
        int gz = v.z >= 0 ? (v.z >> sizeLog2) : ~(~v.z >> sizeLog2);
        Vector3i lp = new Vector3i(gx, gy, gz);
        if(localPos == null) {
          localPos = lp;
        }
        if(!lp.equals(localPos)) {
          min = -LEVEL_AMPLITUDE_MULTIPLIER*i;
          break;
        }
        min = Math.min(min, level.getValueAt(v.x/(double)(size), v.y/(double)(size), v.z/(double)(size)));
      }
      sum += min;
      sizeLog2 += 1;
    }
    return sum;
  }
  
  /**
   * returns a value that is guaranteed to be abow any value within a cube with corner at given point and given edge length 
   */
  public double getMaxValue(Vector3i corner, int edge) {
    List<Vector3i> corners = getCorners(corner, edge);
    double sum = 0;
    Vector3i localPos = null;
    int sizeLog2 = baseMapSizeLog2;
    for(int i = 1; i <= numberOfLevels; ++i) {
      double max = Float.MIN_VALUE;
      NoiseLevel level = levels[i-1];
      if(level == null) {
        level = new NoiseLevel(4, i*LEVEL_AMPLITUDE_MULTIPLIER, seed ^ i);
        levels[i-1] = level;
      }
      int size = 1 << sizeLog2;
      for(Vector3i v : corners) {
        int gx = v.x >= 0 ? (v.x >> sizeLog2) : ~(~v.x >> sizeLog2);
        int gy = v.y >= 0 ? (v.y >> sizeLog2) : ~(~v.y >> sizeLog2);
        int gz = v.z >= 0 ? (v.z >> sizeLog2) : ~(~v.z >> sizeLog2);
        Vector3i lp = new Vector3i(gx, gy, gz);
        if(localPos == null) {
          localPos = lp;
        }
        if(!lp.equals(localPos)) {
          max = LEVEL_AMPLITUDE_MULTIPLIER*i;
          break;
        }
        max = Math.max(max, level.getValueAt(v.x/(double)(size), v.y/(double)(size), v.z/(double)(size)));
      }
      sum += max;
      sizeLog2 += 1;
    }
    return sum;
  }

  private List<Vector3i> getCorners(Vector3i corner, int edge) {
    List<Vector3i> corners = Lists.newArrayList(
        corner, corner.add(0,edge,0), corner.add(edge,edge,0), corner.add(edge,0,0),
        corner.add(0,0,edge), corner.add(edge,0,edge), corner.add(0,edge,edge), corner.add(edge,edge,edge)
    );
    return corners;
  }
  
}
