package top.frankyang.exp.anime;

import net.minecraft.client.util.math.Vector3d;
import top.frankyang.exp.anime.TransformRotate3d.TransformRotateX;
import top.frankyang.exp.anime.TransformRotate3d.TransformRotateY;
import top.frankyang.exp.anime.TransformRotate3d.TransformRotateZ;
import top.frankyang.exp.anime.TransformScale3d.TransformScaleX;
import top.frankyang.exp.anime.TransformScale3d.TransformScaleY;
import top.frankyang.exp.anime.TransformScale3d.TransformScaleZ;
import top.frankyang.exp.anime.TransformTranslate3d.TransformTranslateX;
import top.frankyang.exp.anime.TransformTranslate3d.TransformTranslateY;
import top.frankyang.exp.anime.TransformTranslate3d.TransformTranslateZ;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transform {
    public static final Transform EMPTY = new Transform();

    protected final Double[] arguments;

    public Transform(Double... arguments) {
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

    public Transform compositeWith(Transform other, double progress) {
        Double[] arguments = new Double[16];
        for (int i = 0; i < 16; i++) {
            arguments[i] = compositeVal(this.arguments[i], other.arguments[i], progress);
        }
        return new Transform(arguments);
    }

    public Transform compositeWith(Transform other) {
        // Algorithm from https://github.com/jlmakes/rematrix
        Double[] finalArgs = new Double[16];
        Double[] thisArgs = this.arguments;
        Double[] otherArgs = other.arguments;

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

        return new Transform(finalArgs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transform)) return false;
        Transform transform = (Transform) o;
        return Arrays.equals(arguments, transform.arguments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arguments);
    }

    public static final class TransformFactory {
        private static final Map<String, Class<? extends Transform>> transformMap = new HashMap<>();

        static {
            transformMap.put("matrix3d", Transform.class);
            transformMap.put("rotateX", TransformRotateX.class);
            transformMap.put("rotateY", TransformRotateY.class);
            transformMap.put("rotateZ", TransformRotateZ.class);
            transformMap.put("rotate3d", TransformRotate3d.class);
            transformMap.put("scaleX", TransformScaleX.class);
            transformMap.put("scaleY", TransformScaleY.class);
            transformMap.put("scaleZ", TransformScaleZ.class);
            transformMap.put("scale3d", TransformScale3d.class);
            transformMap.put("translateX", TransformTranslateX.class);
            transformMap.put("translateY", TransformTranslateY.class);
            transformMap.put("translateZ", TransformTranslateZ.class);
            transformMap.put("translate3d", TransformTranslate3d.class);
        }

        public static Transform getTransform(String name, Object... arguments) throws Throwable {
            Class<? extends Transform> clazz = Objects.requireNonNull(transformMap.get(name));

            Constructor<? extends Transform> c;

            Class<?>[] classes = new Class<?>[arguments.length];
            for (int i = 0, l = arguments.length; i < l; i++) {
                Object argument = arguments[i];
                classes[i] = argument.getClass();
            }

            try {
                c = clazz.getConstructor(classes);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("指定的参数不匹配指定变换类的任何构造方法签名。");
            }
            try {
                return c.newInstance(arguments);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("名称没有指向一个具有公共构造方法的可构造类。");
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("指定的参数不匹配指定变换类的公共构造方法签名。");
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        public static Transform parseTransform(String expr) throws Throwable {
            final String REGEX = "(\\([\\s\\S]*\\))";
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(expr);

            String argumentString;
            String[] argumentStrings;
            if (matcher.find()) {
                argumentString = Objects.requireNonNull(matcher.group(0))
                        .replaceAll("[()]+", "");
                argumentStrings = argumentString.split("[\\s,]+");
            } else {
                throw new IllegalArgumentException("在变换表达式中找不到变换实参数组。您是否错误地使用了全角括号？");
            }

            Double[] arguments = new Double[argumentStrings.length];
            try {
                for (int i = 0, l = argumentStrings.length; i < l; i++) {
                    String string = argumentStrings[i];
                    arguments[i] = Double.valueOf(string);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("变换实参数组“%s”不包含有效的双精度浮点值。", Arrays.toString(argumentStrings))
                );
            }

            String transformName = expr.split("\\s*\\(")[0];
            return getTransform(transformName, (Object[]) arguments);
        }
    }
}
