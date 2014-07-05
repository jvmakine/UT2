package fi.haju.ut2.voxels.functions.perlinnoise;

public class InterpolationUtil {

  private InterpolationUtil() {
  }

  public static double interpolateLinear(double t, double v1, double v2) {
    return v1 + (v2 - v1) * t;
  }

  public static double interpolateLinear3d(
      double xt, double yt, double zt,
      double n1, double n2, double n3, double n4, double n5, double n6, double n7, double n8) {
    double x1 = n1 + (n2 - n1) * xt;
    double x2 = n3 + (n4 - n3) * xt;
    double z1 = x1 + (x2 - x1) * yt;
    double xx1 = n5 + (n6 - n5) * xt;
    double xx2 = n7 + (n8 - n7) * xt;
    double z2 = xx1 + (xx2 - xx1) * yt;
    return z1 + (z2 - z1) * zt;
  }

  public static double interpolateLinear2d(double xt, double yt, double n1, double n2, double n3, double n4) {
    double xx1 = n1 + (n2 - n1) * xt;
    double xx2 = n3 + (n4 - n3) * xt;
    return xx1 + (xx2 - xx1) * yt;
  }


}
