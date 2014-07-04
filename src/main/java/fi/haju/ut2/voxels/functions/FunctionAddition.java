package fi.haju.ut2.voxels.functions;

import fi.haju.ut2.geometry.Position;

public class FunctionAddition implements Function3d {

  private final Function3d from;
  private final Function3d to;
  
  public FunctionAddition(Function3d from, Function3d to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public double value(double x, double y, double z) {
    return from.value(x, y, z) + to.value(x, y, z);
  }

  @Override
  public Position gradient(double x, double y, double z) {
    return from.gradient(x, y, z).add(to.gradient(x, y, z));
  }

}
