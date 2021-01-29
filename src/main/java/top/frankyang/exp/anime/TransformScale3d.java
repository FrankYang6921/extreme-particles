package top.frankyang.exp.anime;

public class TransformScale3d extends Transform {
    public TransformScale3d(Double x, Double y, Double z) {
        super(x, 0d, 0d, 0d, 0d, y, 0d, 0d, 0d, 0d, z, 0d, 0d, 0d, 0d, 1d);
    }

    public static class TransformScaleZ extends TransformScale3d {
        public TransformScaleZ(Double i) {
            super(1d, 1d, i);
        }
    }

    public static class TransformScaleY extends TransformScale3d {
        public TransformScaleY(Double i) {
            super(1d, i, 1d);
        }
    }

    public static class TransformScaleX extends TransformScale3d {
        public TransformScaleX(Double i) {
            super(i, 1d, 1d);
        }
    }
}
