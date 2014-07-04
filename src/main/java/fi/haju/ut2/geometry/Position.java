package fi.haju.ut2.geometry;

import java.util.List;

public final class Position {
  public double x;
  public double y;
  public double z;
  
  public Position(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  public final Position xplus(double amount) {
    return pos(x+amount, y, z);
  }
  
  public final Position yplus(double amount) {
    return pos(x, y + amount, z);
  }
  
  public final Position zplus(double amount) {
    return pos(x, y, z + amount);
  }
  
  public final Position add(Position pos) {
    return pos(x + pos.x, y + pos.y, z + pos.z);
  }
  
  public final Position div(double dividor) {
    return pos(x / dividor, y / dividor, z / dividor);
  }
  
  public static final Position pos(double x, double y, double z) {
    return new Position(x, y, z);
  }
  
  public static final Position average(Position first, Position... positions) {
    Position sum = pos(first.x, first.y, first.z);
    for (Position p : positions) {
      sum = sum.add(p);
    } 
    return sum.div(positions.length + 1);
  }
  
  public static Position average(List<Position> cornPos) {
    Position first = cornPos.get(0);
    Position[] rest = new Position[cornPos.size() - 1];
    int i = 0;
    for (Position p : cornPos) {
      if (i > 0) {
        rest[i-1] = p;
      }
      i++;
    }
    return average(first, rest);
  }

  public double distance(Position p) {
    double dx = p.x - x;
    double dy = p.y - y;
    double dz = p.z - z;
    return Math.sqrt(dx*dx + dy*dy + dz*dz);
  }

  @Override
  public String toString() {
    return "pos[" + x + ", " + y + ", " + z + "]";
  }

  public Position normalize() {
    double l = x*x + y*y + z*z;
    return pos(x/l, y/l, z/l);
  }

  public Position inverse() {
    return pos(-x, -y, -z);
  }

}
