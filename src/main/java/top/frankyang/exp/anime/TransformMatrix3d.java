package top.frankyang.exp.anime;

import net.minecraft.client.util.math.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransformMatrix3d implements Transform {
    public static final TransformMatrix3d EMPTY = new TransformMatrix3d();

    protected final Double[] arguments;

    public TransformMatrix3d(Double... arguments) {
        if (arguments.length == 0) {
            this.arguments = new Double[]{
                    1d, 0d, 0d, 0d,
                    0d, 1d, 0d, 0d,
                    0d, 0d, 1d, 0d,
                    0d, 0d, 0d, 1d
            };
            /*  -           -
             * | 0  4  8  12 |
             * | 1  5  9  13 |
             * | 2  6  10 14 |
             * | 3  7  11 15 |
             *  -           -
             */
        } else if (arguments.length == 16) {
            this.arguments = arguments;
        } else {
            throw new IllegalArgumentException(String.format("三维矩阵变换仅允许没有或有16个参数，而非%d个。", arguments.length));
        }
    }

    protected static double compositeVal(double a, double b, double progress) {
        return a * (1 - progress / 100d) + b * progress / 100d;
    }

    public Vector3d evaluateOn(Vector3d position) {
        double x = position.x;
        double y = position.y;
        double z = position.z;
        double nx = arguments[0] * x + arguments[4] * y + arguments[8] * z + arguments[12];
        double ny = arguments[1] * x + arguments[5] * y + arguments[9] * z + arguments[13];
        double nz = arguments[2] * x + arguments[6] * y + arguments[10] * z + arguments[14];
        return new Vector3d(nx, ny, nz);
    }

    public List<Vector3d> evaluateOn(List<Vector3d> positions) {
        List<Vector3d> result = new ArrayList<>();
        for (Vector3d position : positions) {
            result.add(evaluateOn(position));
        }
        return result;
    }

    @Override
    public boolean isEffectivelyEmpty() {
        return equals(EMPTY);
    }

    public Transform compositeWith(Transform other, double progress) {
        TransformMatrix3d $other = (TransformMatrix3d) other;
        Double[] arguments = new Double[16];
        for (int i = 0; i < 16; i++) {
            arguments[i] = compositeVal(this.arguments[i], $other.arguments[i], progress);
        }
        return new TransformMatrix3d(arguments);
    }

    public Transform compositeWith(Transform other) {
        TransformMatrix3d $other = (TransformMatrix3d) other;
        // Algorithm from https://github.com/jlmakes/rematrix
        Double[] finalArgs = new Double[16];
        Double[] thisArgs = this.arguments;
        Double[] otherArgs = $other.arguments;

        for (int i = 0; i < 4; i++) {
            Double[] row = new Double[]{
                    thisArgs[i], thisArgs[i + 4], thisArgs[i + 8], thisArgs[1 + 12]
            };
            for (int j = 0; j < 4; j++) {
                int k = j * 4;
                Double[] col = new Double[]{
                        otherArgs[k], otherArgs[k + 1], otherArgs[k + 2], otherArgs[k + 3]
                };
                double res = row[0] * col[0] + row[1] * col[1] + row[2] * col[2] + row[3] * col[3];
                finalArgs[i + k] = res;
            }
        }

        return new TransformMatrix3d(finalArgs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransformMatrix3d)) return false;
        TransformMatrix3d transform = (TransformMatrix3d) o;
        return Arrays.equals(arguments, transform.arguments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arguments);
    }
}
