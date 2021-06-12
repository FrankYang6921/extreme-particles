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

public interface Transform {
    Transform EMPTY = new Transform() {
        @Override
        public Transform compositeWith(Transform other, double progress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Transform compositeWith(Transform other) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Vector3d evaluateOn(Vector3d position) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Vector3d> evaluateOn(List<Vector3d> positions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEffectivelyEmpty() {
            return true;
        }
    };

    Transform compositeWith(Transform other, double progress);

    Transform compositeWith(Transform other);

    Vector3d evaluateOn(Vector3d position);

    List<Vector3d> evaluateOn(List<Vector3d> positions);

    boolean isEffectivelyEmpty();

    final class TransformFactory {
        private static final Map<String, Class<? extends Transform>> transformMap = new HashMap<>();

        static {
            transformMap.put("matrix3d", TransformMatrix3d.class);
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
