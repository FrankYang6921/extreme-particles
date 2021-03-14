package top.frankyang.exp;

public class Property {
    public final double x, y, z, dx, dy, dz;
    public final float a, s;
    public final int r, g, b, l;

    public Property(double x, double y, double z, double dx, double dy, double dz, int r, int g, int b, float a, int l, float s) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.s = s;
        this.l = l;
    }

    @Override
    public String toString() {
        return "Property{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", dx=" + dx +
                ", dy=" + dy +
                ", dz=" + dz +
                ", a=" + a +
                ", s=" + s +
                ", r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", l=" + l +
                '}';
    }
}
