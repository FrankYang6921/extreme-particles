package top.frankyang.exp.anime;

import java.util.Arrays;
import java.util.Objects;

public class AnimationFrame {
    public final double x, y, z, progress;
    public final int r, g, b;
    public final float a, s;
    public final BlendMode blendMode;
    public final Transform transform;

    public AnimationFrame(double[] origin, double[] color, float scale, BlendMode blendMode, Double progress, Transform[] transforms) {
        x = origin[0];
        y = origin[1];
        z = origin[2];
        r = (int) color[0];
        g = (int) color[1];
        b = (int) color[2];
        a = (float) color[3];
        s = scale;
        this.blendMode = blendMode;

        for (double i : color) {
            if ((i < 0 && i != -1) || i > 255) {
                throw new IllegalArgumentException(String.format("颜色的每个值仅能是[0, 255]的整数或-1，而非%s。", Arrays.toString(color)));
            }
        }

        if (scale < 0 && scale != -1) {
            throw new IllegalArgumentException(String.format("缩放的值仅能是[0, ∞]的浮点数或-1，而非%s。", scale));
        }

        if (progress == null || progress < 0 || progress > 100) {
            throw new IllegalArgumentException(String.format("步进的值仅能是[0, 100]的浮点数，而非%s。", progress));
        }
        this.progress = progress;

        if (transforms.length > 1) {
            transform = Arrays.stream(transforms)
                    .reduce(Transform.EMPTY, Transform::compositeWith);
        } else if (transforms.length == 1) {
            transform = transforms[0];  // Only one, no composition
        } else {
            transform = Transform.EMPTY;  // Nothing here, by default
        }
    }

    private static int compositeVal(int a, int b, double pace) {
        if (a == Integer.MIN_VALUE && b == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (a == Integer.MIN_VALUE) {
            return b;
        } else if (b == Integer.MIN_VALUE) {
            return a;
        }
        return (int) (a * (1 - pace / 100d) + b * pace / 100d);
    }

    private static float compositeVal(float a, float b, double pace) {
        if (Float.isNaN(a) && Float.isNaN(b)) {
            return Float.NaN;
        } else if (Float.isNaN(a)) {
            return b;
        } else if (Float.isNaN(b)) {
            return a;
        }
        return (float) (a * (1 - pace / 100d) + b * pace / 100d);
    }

    private static double compositeVal(double a, double b, double pace) {
        if (Double.isNaN(a) && Double.isNaN(b)) {
            return Double.NaN;
        } else if (Double.isNaN(a)) {
            return b;
        } else if (Double.isNaN(b)) {
            return a;
        }
        return a * (1 - pace / 100d) + b * pace / 100d;
    }

    public AnimationFrame compositeWith(AnimationFrame other, double pace) {
        Objects.requireNonNull(other);
        double x = compositeVal(this.x, other.x, pace);
        double y = compositeVal(this.y, other.y, pace);
        double z = compositeVal(this.z, other.z, pace);
        int r = compositeVal(this.r, other.r, pace);
        int g = compositeVal(this.g, other.g, pace);
        int b = compositeVal(this.b, other.b, pace);
        float a = compositeVal(this.a, other.a, pace);
        float s = compositeVal(this.s, other.s, pace);
        double p = compositeVal(this.progress, other.progress, pace);

        Transform[] t = new Transform[]{this.transform.compositeWith(other.transform, pace)};

        return new AnimationFrame(
                new double[]{x, y, z},
                new double[]{r, g, b, a},
                s, pace < 50 ? this.blendMode : other.blendMode, p, t
        );
    }
}
