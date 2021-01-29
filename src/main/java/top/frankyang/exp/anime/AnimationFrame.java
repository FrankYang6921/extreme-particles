package top.frankyang.exp.anime;

import top.frankyang.exp.Wrapper;

import java.util.Arrays;
import java.util.Objects;

import static top.frankyang.exp.anime.Transform.NOTHING;

public final class AnimationFrame extends Wrapper {
    public final double percentage;
    public final Transform[] transforms;
    public final Transform transform;

    public AnimationFrame(double[] origin, double[] motion, int[] color, float alpha, float scale, Transform[] transforms, Double percentage) {  // TODO CSS style transform support
        super(
                origin[0], origin[1], origin[2], motion[0], motion[1], motion[2], color[0], color[1], color[2], alpha, 0, scale
        );

        for (double i : motion) {
            if (i < -10d || i > 10d) {
                throw new IllegalArgumentException(String.format("动量的每个值仅能是[-10, 10]的浮点，而非%s。", Arrays.toString(motion)));
            }
        }

        for (int i : color) {
            if (i < 0 || i > 255) {
                throw new IllegalArgumentException(String.format("颜色的每个值仅能是[0, 255]的整数，而非%s。", Arrays.toString(color)));
            }
        }

        if (percentage == null || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(String.format("步进仅能是[0, 100]的整数，而非%s。", percentage));
        }
        this.percentage = percentage;
        this.transforms = transforms;

        if (this.transforms.length > 1) {
            transform = Arrays.stream(this.transforms)
                    .reduce(NOTHING, Transform::compositeWith);
        } else if (this.transforms.length == 1) {
            transform = this.transforms[0];  // Only one, no composition
        } else {
            transform = Transform.NOTHING;  // Nothing here, by default
        }
    }

    private static int compositeVal(int a, int b, double pace) {
        return (int) (a * (1 - pace / 100d) + b * pace / 100d);
    }

    private static float compositeVal(float a, float b, double pace) {
        return (float) (a * (1 - pace / 100d) + b * pace / 100d);
    }

    private static double compositeVal(double a, double b, double pace) {
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
                a, s, t, 0d
        );
    }
}
