package top.frankyang.exp.anime;

import top.frankyang.exp.Property;

import java.util.Arrays;
import java.util.Objects;

import static top.frankyang.exp.anime.Transform.EMPTY;

public class AnimationFrame extends Property {
    public final double pace;

    public final Transform[] transforms;
    public final Transform transform;

    public AnimationFrame(double[] origin, double[] motion, int[] color, float alpha, float scale, Double pace, Transform[] transforms) {
        super(
                origin[0], origin[1], origin[2], motion[0], motion[1], motion[2], color[0], color[1], color[2], alpha, 0, scale
        );

        for (double i : motion) {
            if ((i < -10d || i > 10d) && !Double.isNaN(i)) {
                throw new IllegalArgumentException(String.format("动量的每个值仅能是[-10, 10]的浮点或Double.NaN，而非%s。", Arrays.toString(motion)));
            }
        }

        for (int i : color) {
            if ((i < 0 && i != -1) || i > 255) {
                throw new IllegalArgumentException(String.format("颜色的每个值仅能是[0, 255]的整数或-1，而非%s。", Arrays.toString(color)));
            }
        }

        if ((alpha < 0 && alpha != -1) || alpha > 1) {
            throw new IllegalArgumentException(String.format("透明度的值仅能是[0, 1]的数或-1，而非%s。", alpha));
        }

        if (scale < 0 && scale != -1) {
            throw new IllegalArgumentException(String.format("缩放的值仅能是[0, ∞]的数或-1，而非%s。", scale));
        }

        if (pace == null || pace < 0 || pace > 100) {
            throw new IllegalArgumentException(String.format("步进仅能是[0, 100]的浮点数，而非%s。", pace));
        }
        this.pace = pace;
        this.transforms = transforms;

        if (this.transforms.length > 1) {
            transform = Arrays.stream(this.transforms)
                    .reduce(EMPTY, Transform::compositeWith);
        } else if (this.transforms.length == 1) {
            transform = this.transforms[0];  // Only one, no composition
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
        double dx = compositeVal(this.dx, other.dx, pace);
        double dy = compositeVal(this.dy, other.dy, pace);
        double dz = compositeVal(this.dz, other.dz, pace);
        int r = compositeVal(this.r, other.r, pace);
        int g = compositeVal(this.g, other.g, pace);
        int b = compositeVal(this.b, other.b, pace);
        float a = compositeVal(this.a, other.a, pace);
        float s = compositeVal(this.s, other.s, pace);

        Transform[] t = new Transform[]{this.transform.compositeWith(other.transform, pace)};

        return new AnimationFrame(
                new double[]{x, y, z},
                new double[]{dx, dy, dz},
                new int[]{r, g, b},
                a, s, 0d, t
        );
    }
}
