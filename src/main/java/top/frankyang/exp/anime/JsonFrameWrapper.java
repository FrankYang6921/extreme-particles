package top.frankyang.exp.anime;

public final class JsonFrameWrapper {
    public final double[] motion;
    public final int[] color;
    public final float alpha;
    public final float scale;
    public final Transform[] trans;
    public final Double pace;

    public JsonFrameWrapper(double[] motion, int[] color, float alpha, float scale, Transform[] trans, Double pace) {
        this.motion = motion;
        this.color = color;
        this.alpha = alpha;
        this.scale = scale;
        this.trans = trans;
        this.pace = pace;
    }

    public AnimationFrame toFrame() {
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

        if (motion.length != 3) {
            throw new IllegalArgumentException("动量（“motion”）的值能且仅能是包含三个浮点值的数组。");
        }
        if (color.length != 3) {
            throw new IllegalArgumentException("颜色（“color”）的值能且仅能是包含三个整数值的数组。");
        }
        return new AnimationFrame(motion, color, alpha, scale, trans, pace);
    }
}
