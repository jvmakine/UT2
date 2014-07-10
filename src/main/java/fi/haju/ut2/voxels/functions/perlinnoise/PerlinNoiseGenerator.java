package fi.haju.ut2.voxels.functions.perlinnoise;

public final class PerlinNoiseGenerator {
  private static final double LEVEL_AMPLITUDE_MULTIPLIER = 5.0f;
  private final int numberOfLevels;
  private final int baseMapSizeLog2;
  private final int seed;
  private final NoiseLevel[] levels; 
  
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
  
}
