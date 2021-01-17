package top.frankyang.exp.anime;

import net.minecraft.client.particle.Particle;
import top.frankyang.exp.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.toDegrees;

public final class Transform {
    public static final Map<String, Method> METHODS = new HashMap<String, Method>(){{
        this.put("matrix3d", Method.MATRIX3D);
        this.put("translate3d", Method.TRANSLATE3D);
        this.put("translateX", Method.TRANSLATE_X);
        this.put("translateY", Method.TRANSLATE_Y);
        this.put("translateZ", Method.TRANSLATE_Z);
        this.put("scale3d", Method.SCALE3D);
        this.put("scaleX", Method.SCALE_X);
        this.put("scaleY", Method.SCALE_Y);
        this.put("scaleZ", Method.SCALE_Z);
        this.put("rotate3d", Method.ROTATE3D);
        this.put("rotateX", Method.ROTATE_X);
        this.put("rotateY", Method.ROTATE_Y);
        this.put("rotateZ", Method.ROTATE_Z);
    }};

    private final String method;
    private final String params;
    private final double[] origin;
    private final transient List<ResolvedParam> resolvedParams = new ArrayList<>();

    public Transform(String method, String params, double[] origin) {
        if (!METHODS.containsKey(method)) {
            throw new IllegalArgumentException(String.format("不合法的变换类型：“%s”。", method));
        }
        this.method = method;
        this.params = params;
        this.origin = origin != null ? origin : new double[]{0, 0, 0};

        resolveParams();
    }

    private void resolveParams() {
        for (String param : this.params.split("\\s+")) {
            ResolvedParam parsed = ResolvedParam.tryToParse(param);
            if (parsed != null && parsed.isValidFor(METHODS.get(method))) {
                resolvedParams.add(parsed);
            } else {
                throw new IllegalArgumentException(String.format("不能解析变换参数：“%s”", param));
            }
        }
    }

    private void apply(List<Particle> particles) {
    }

    public Transform compositeWith(Properties other, double pace) {
        return null;
    }

    private enum Method {
        MATRIX3D, TRANSLATE3D, TRANSLATE_X, TRANSLATE_Y, TRANSLATE_Z, SCALE3D,
        SCALE_X, SCALE_Y, SCALE_Z, ROTATE3D, ROTATE_X, ROTATE_Y, ROTATE_Z
    }

    private abstract static class ResolvedParam {
        protected final double value;

        public ResolvedParam(double value) {
            this.value = value;
        }

        public abstract boolean isValidFor(Method method);

        public <T> T getValueFor(Class<T> clazz) {
            return clazz.cast(value);
        }

        public static ResolvedParam tryToParse(String value) {
            ResolvedParam ret;
            try {
                if ((ret = LengthParameter.parse(value)) != null) {
                    return ret;
                }
            } catch (NumberFormatException ignored) {
            }
            try {
                if ((ret = RatioParameter.parse(value)) != null) {
                    return ret;
                }
            } catch (NumberFormatException ignored) {
            }
            try {
                if ((ret = AngleParameter.parse(value)) != null) {
                    return ret;
                }
            } catch (NumberFormatException ignored) {
            }
            return null;
        }
    }

    private static class LengthParameter extends ResolvedParam {
        public LengthParameter(double value) {
            super(value);
        }

        public static ResolvedParam parse(String value) {
            if (value.endsWith("m")) {
                return new LengthParameter(Double.parseDouble(value.replaceAll("m", "")));
            } else if (value.endsWith("dm")) {
                return new LengthParameter(Double.parseDouble(value.replaceAll("dm", "")) / 1e1);
            } else if (value.endsWith("cm")) {
                return new LengthParameter(Double.parseDouble(value.replaceAll("cm", "")) / 1e2);
            }
            return null;
        }

        @Override
        public boolean isValidFor(Method method) {
            return method == Method.TRANSLATE3D || method == Method.TRANSLATE_X ||
                    method == Method.TRANSLATE_Y || method == Method.TRANSLATE_Z;
        }
    }

    private static class RatioParameter extends ResolvedParam {
        public RatioParameter(double value) {
            super(value);
        }

        public static ResolvedParam parse(String value) {
            if (value.endsWith("%")) {
                return new RatioParameter(Double.parseDouble(value.replaceAll("%", "")) / 1e2);
            }
            return null;
        }

        @Override
        public boolean isValidFor(Method method) {
            return method == Method.SCALE3D || method == Method.SCALE_X ||
                    method == Method.SCALE_Y || method == Method.SCALE_Z ||
                    method == Method.MATRIX3D;
        }
    }

    private static class AngleParameter extends ResolvedParam {
        public AngleParameter(double value) {
            super(value);
        }

        public static ResolvedParam parse(String value) {
            if (value.endsWith("deg")) {
                return new AngleParameter(Double.parseDouble(value.replaceAll("deg", "")));
            } else if (value.endsWith("turn")) {
                return new LengthParameter(Double.parseDouble(value.replaceAll("turn", "")) * 360);
            } else if (value.endsWith("rad")) {
                return new LengthParameter(toDegrees(Double.parseDouble(value.replaceAll("rad", ""))));
            }
            return null;
        }

        @Override
        public boolean isValidFor(Method method) {
            return method == Method.ROTATE3D || method == Method.ROTATE_X ||
                    method == Method.ROTATE_Y || method == Method.ROTATE_Z;
        }
    }
}
