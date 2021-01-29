package top.frankyang.exp.anime;
import static java.lang.Math.*;

public class TransformRotate3d extends Transform {
    public TransformRotate3d(Double x, Double y, Double z, Double a) {
        super(
                1 + (1 - cos(a)) * (pow(x, 2) - 1), -z * sin(a) + x * y * (1 - cos(a)), y * sin(a) + x * z * (1 - cos(a)), 0d,
                z * sin(a) + x * y * (1 - cos(a)), 1 + (1 - cos(a)) * (pow(y, 2) - 1), -x * sin(a) + y * z * (1 - cos(a)), 0d,
                -y * sin(a) + x * z * (1 - cos(a)), x * sin(a) + y * z * (1 - cos(a)), 1 + (1 - cos(a)) * (pow(z, 2) - 1), 0d,
                0d, 0d, 0d, 1d
        );
    }

    public static class TransformRotateZ extends TransformRotate3d {
        public TransformRotateZ(Double a) {
            super(0d, 0d, 1d, a);
        }
    }

    public static class TransformRotateY extends TransformRotate3d {
        public TransformRotateY(Double a) {
            super(0d, 1d, 0d, a);
        }
    }

    public static class TransformRotateX extends TransformRotate3d {
        public TransformRotateX(Double a) {
            super(1d, 0d, 0d, a);
        }
    }
}
