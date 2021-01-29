package top.frankyang.exp.anime;

public final class FrameWrapper {
    public final double[] origin;
    public final double[] motion;
    public final int[] color;
    public final float alpha;
    public final float scale;
    public final String[] transforms;
    public final Double pace;

    public FrameWrapper(double[] origin, double[] motion, int[] color, float alpha, float scale, String[] transforms, Double pace) {
        this.origin = origin;
        this.motion = motion;
        this.color = color;
        this.alpha = alpha;
        this.scale = scale;
        this.transforms = transforms;
        this.pace = pace;
    }

    public AnimationFrame toFrame() {
        double[] origin;
        if (this.origin == null) {
            origin = new double[] {0, 0, 0};
        } else {
            origin = this.origin.clone();
        }

        double[] motion;
        if (this.motion == null) {
            motion = new double[] {0, 0, 0};
        } else {
            motion = this.motion.clone();
        }

        int[] color;
        if (this.color == null) {
            color = new int[] {0, 0, 0};
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

        return new AnimationFrame(origin, motion, color, alpha, scale, t, pace);
    }
}
