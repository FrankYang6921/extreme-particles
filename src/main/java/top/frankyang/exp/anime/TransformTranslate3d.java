package top.frankyang.exp.anime;

public class TransformTranslate3d extends Transform {
    public TransformTranslate3d(Double x, Double y, Double z) {
        super(1d, 0d, 0d, 0d, 0d, 1d, 0d, 0d, 0d, 0d, 1d, 0d, x, y, z, 1d);
    }

    public static final class TransformTranslateZ extends TransformTranslate3d {
        public TransformTranslateZ(Double i) {
            super(0d, 0d, i);
        }
    }

    public static final class TransformTranslateY extends TransformTranslate3d {
        public TransformTranslateY(Double i) {
            super(0d, i, 0d);
        }
    }

    public static final class TransformTranslateX extends TransformTranslate3d {
        public TransformTranslateX(Double i) {
            super(i, 0d, 0d);
        }
    }
}
