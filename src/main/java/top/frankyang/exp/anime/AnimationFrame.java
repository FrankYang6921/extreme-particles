package top.frankyang.exp.anime;

import top.frankyang.exp.Properties;

import java.util.Arrays;
import java.util.Objects;

public final class AnimationFrame extends Properties {
    public final double pace;
    public final Transform[] trans;

    public AnimationFrame(double[] motion, int[] color, float alpha, float scale, Transform[] trans, Double pace) {  // TODO CSS style transform support
        super(
                0, 0, 0, motion[0], motion[1], motion[2], color[0], color[1], color[2], alpha, 0, scale
        );

        for (double i : motion) {
            if (i < -10d || i > 10d) {
                throw new IllegalArgumentException(String.format("动量（“motion”）的每个值仅能是[-10, 10]的浮点，而非%s。", Arrays.toString(motion)));
            }
        }

        for (int i : color) {
            if (i < 0 || i > 255) {
                throw new IllegalArgumentException(String.format("颜色（“color”）的每个值仅能是[0, 255]的整数，而非%s。", Arrays.toString(color)));
            }
        }

        if (pace == null || pace < 0 || pace > 100) {
            throw new IllegalArgumentException(String.format("步进（“pace”）仅能是[0, 100]的整数，而非%s。", pace));
        }
        this.pace = pace;
        this.trans = trans;
    }

    private static int compositeVal(int one, int another, double pace) {
        return (int) (one * (1 - pace / 100d) + another * pace / 100d);
    }

    private static float compositeVal(float one, float another, double pace) {
        return (float) (one * (1 - pace / 100d) + another * pace / 100d);
    }

    private static double compositeVal(double one, double another, double pace) {
        return one * (1 - pace / 100d) + another * pace / 100d;
    }

    public Properties compositeWith(Properties other, double pace) {
        Objects.requireNonNull(other);
        double x = compositeVal(this.x, other.x, pace);
        double y = compositeVal(this.y, other.y, pace);
        double z = compositeVal(this.z, other.z, pace);
        double dx = compositeVal(this.dx, other.dx, pace);
        double dy = compositeVal(this.dy, other.dy, pace);
        double dz = compositeVal(this.dz, other.dz, pace);
        int r = compositeVal(this.r, other.r, pace);
        int g = compositeVal(this.g, other.g, pace);
        int b = compositeVal(this.b, other.b, pace);
        float a = compositeVal(this.a, other.a, pace);
        float s = compositeVal(this.s, other.s, pace);
        int l = compositeVal(this.l, other.l, pace);

        return new Properties(x, y, z, dx, dy, dz, r, g, b, a, l, s);
    }
}
