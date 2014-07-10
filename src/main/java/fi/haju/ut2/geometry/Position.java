package fi.haju.ut2.geometry;

import java.util.List;

import com.google.common.collect.Lists;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

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
  
  public static double dot(Position p1, Position p2) {
    return p1.x * p2.x + p1.y*p2.y + p1.z*p2.z;
  }
  
  public static Position cross(Position p1, Position p2) {
    return pos(
      p1.y*p2.z - p1.z*p2.y,
      p1.z*p2.x - p1.x*p2.z,
      p1.x*p2.y - p1.y*p2.x
    );
  }

  public static Position substract(Position p1, Position p2) {
    return pos(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
  }
  
  public static Position add(Position p1, Position p2) {
    return pos(p1.x + p2.x, p1.y + p2.y, p1.z + p2.z);
  }
  
  public static double difference(Position p1, Position p2) {
    return abs(p1.x - p2.x) + abs(p1.y - p2.y) + abs(p1.z - p2.z); 
  }
  
  public static double distance(Position p1, Position p2) {
    double dx = p1.x - p2.x;
    double dy = p1.y - p2.y;
    double dz = p1.z - p2.z;
    return sqrt(dx*dx + dy*dy + dz*dz); 
  }

  public double length() {
    return sqrt(x*x + y*y + z*z);
  }

  public List<Position> centeredEmptyCube(double d, int s) {
    //TODO: More efficient implementation
    List<Position> result = Lists.newArrayList();
    Position corner = add(pos(-d*s,-d*s,-d*s));
    for (int x = 0; x < 2*s + 1; ++x) {
      for (int y = 0; y < 2*s + 1; ++y) {
        for (int z = 0; z < 2*s + 1; ++z) {
          if ((x != 0 && x != 2*s) && (y != 0 && y != 2*s) && (z != 0 && z != 2*s)) continue; // do not include inside points
          result.add(corner.add(pos(x*d, y*d, z*d)));
        }
      }
    }
    return result;
  }

}
