package top.frankyang.exp.anime;

public class FrameWrapper {
    public final double[] origin;
    public final double[] color;
    public final Float scale;
    public final String blendMode;
    public final Double progress;
    public final String[] transforms;

    private FrameWrapper(double[] origin, double[] color, Float scale, String blendMode, Double progress, String[] transforms) {
        this.origin = origin;
        this.color = color;
        this.scale = scale;
        this.blendMode = blendMode;
        this.progress = progress;
        this.transforms = transforms;
    }

    public AnimationFrame toFrame() {
        double[] origin = this.origin == null ? new double[]{
                0, 0, 0
        } : this.origin.clone();

        double[] color = this.color == null ? new double[]{
                -1, -1, -1, -1
        } : this.color.clone();

        if (origin.length != 3) {
            throw new IllegalArgumentException("原点的值仅能是包含三个浮点值的数组。");
        }
        if (color.length != 4) {
            throw new IllegalArgumentException("颜色的值仅能是包含四个浮点值的数组。");
        }

        Transform[] transforms;
        if (this.transforms == null) {
            transforms = new Transform[0];
        } else {
            transforms = new Transform[this.transforms.length];
            for (int i = 0; i < transforms.length; i++) {
                try {
                    transforms[i] = Transform
                            .TransformFactory
                            .parseTransform(this.transforms[i]);
                } catch (Throwable throwable) {
                    throw new IllegalArgumentException(
                            "不能以指定的变换参数来构造变换对象：" + throwable.getMessage()
                    );
                }
            }
        }

        float scale = this.scale != null ? this.scale : -1f;
        BlendMode blendMode = this.blendMode != null ? BlendMode.valueOf(this.blendMode) : BlendMode.NORMAL;

        return new AnimationFrame(origin, color, scale, blendMode, progress, transforms);
    }
}
