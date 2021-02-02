package top.frankyang.exp.anime;

import javax.naming.NameNotFoundException;

import static java.lang.Float.NaN;

public final class FrameWrapper {
    public final double[] origin;
    public final double[] motion;
    public final int[] color;
    public final Float alpha;
    public final Float scale;
    public final Double pace;
    public final String[] transforms;

    public FrameWrapper(double[] origin, double[] motion, int[] color, Float alpha, Float scale, Double pace, String[] transforms) {
        this.origin = origin;
        this.motion = motion;
        this.color = color;
        this.alpha = alpha;
        this.scale = scale;
        this.pace = pace;
        this.transforms = transforms;
    }

    public AnimationFrame toFrame() {
        double[] origin;
        if (this.origin == null) {
            origin = new double[] {1, 0, 0};
        } else {
            origin = this.origin.clone();
        }

        double[] motion;
        if (this.motion == null) {
            motion = new double[] {
                    Double.NaN,
                    Double.NaN,
                    Double.NaN
            };
        } else {
            motion = this.motion.clone();
        }

        int[] color;
        if (this.color == null) {
            color = new int[] {
                    Integer.MIN_VALUE,
                    Integer.MIN_VALUE,
                    Integer.MIN_VALUE
            };
        } else {
            color = this.color.clone();
        }

        if (origin.length != 3) {
            throw new IllegalArgumentException("原点的值仅能是包含三个浮点值的数组。");
        }
        if (motion.length != 3) {
            throw new IllegalArgumentException("动量的值仅能是包含三个浮点值的数组。");
        }
        if (color.length != 3) {
            throw new IllegalArgumentException("颜色的值仅能是包含三个整数值的数组。");
        }

        Transform[] t;
        if (this.transforms == null) {
            t = new Transform[] {Transform.NOTHING};
        } else {
            t = new Transform[transforms.length];
            for (int i = 0, l = transforms.length; i < l; i++) {
                try {
                    t[i] = Transform.TransformFactory.parseTransform(transforms[i]);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    throw new IllegalArgumentException("不能以指定的变换参数来构造变换对象，" + throwable.getMessage());
                }
            }
        }

        float alpha;
        if (this.alpha != null) {
            alpha = this.alpha;
        } else {
            alpha = Float.NaN;
        }

        float scale;
        if (this.scale != null) {
            scale = this.scale;
        } else {
            scale = Float.NaN;
        }


        return new AnimationFrame(origin, motion, color, alpha, scale, pace, t);
    }
}
