package top.frankyang.exp;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.util.math.Vector3d;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "JavaReflectionMemberAccess"})
public final class Util {
    private static final Class<Particle> clazz = Particle.class;
    private static final Field rField,
            gField,
            bField,
            aField,
            ageField,
            xField,
            yField,
            zField,
            dxField,
            dyField,
            dzField;
    private static final Map<Particle, Float> mapping = new HashMap<>();

    static {
        try {
            rField = clazz.getField("colorRed");
            gField = clazz.getField("colorGreen");
            bField = clazz.getField("colorBlue");
            aField = clazz.getField("colorAlpha");
            ageField = clazz.getField("maxAge");
            xField = clazz.getField("x");
            yField = clazz.getField("y");
            zField = clazz.getField("z");
            dxField = clazz.getField("velocityX");
            dyField = clazz.getField("velocityY");
            dzField = clazz.getField("velocityZ");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        rField.setAccessible(true);
        gField.setAccessible(true);
        bField.setAccessible(true);
        aField.setAccessible(true);
        ageField.setAccessible(true);
        xField.setAccessible(true);
        yField.setAccessible(true);
        zField.setAccessible(true);
        dxField.setAccessible(true);
        dyField.setAccessible(true);
        dzField.setAccessible(true);
    }

    public static Vector3d getParticleColor(Particle particle) {
        double x, y, z;
        try {
            x = rField.getDouble(particle);
            y = gField.getDouble(particle);
            z = bField.getDouble(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return new Vector3d(x, y, z);
    }

    public static void setParticleColor(Particle particle, Vector3d color) {
        try {
            rField.setDouble(particle, color.x);
            gField.setDouble(particle, color.y);
            bField.setDouble(particle, color.z);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static float getParticleAlpha(Particle particle) {
        try {
            return aField.getFloat(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setParticleAlpha(Particle particle, float alpha) {
        try {
            aField.set(particle, alpha);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getParticleLife(Particle particle) {
        try {
            return ageField.getInt(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setParticleLife(Particle particle, int life) {
        try {
            ageField.setInt(particle, life);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static float getParticleScale(Particle particle) {
        return mapping.getOrDefault(particle, 1f);
    }

    public static void setParticleScale(Particle particle, float scale) {
        float currentScale = getParticleScale(particle);
        float relativeScale = scale / currentScale;
        particle.scale(relativeScale);
        mapping.put(particle, scale);
    }

    public static void clearScaleCache() {
        mapping.entrySet().removeIf(e -> !e.getKey().isAlive());
    }

    public static Vector3d getParticlePos(Particle particle) {
        double x, y, z;
        try {
            x = xField.getDouble(particle);
            y = yField.getDouble(particle);
            z = zField.getDouble(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return new Vector3d(x, y, z);
    }

    public static void setParticlePos(Particle particle, Vector3d pos) {
        try {
            xField.setDouble(particle, pos.x);
            yField.setDouble(particle, pos.y);
            zField.setDouble(particle, pos.z);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Vector3d getParticleDelta(Particle particle) {
        double x, y, z;
        try {
            x = dxField.getDouble(particle);
            y = dyField.getDouble(particle);
            z = dzField.getDouble(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return new Vector3d(x, y, z);
    }

    public static void setParticleDelta(Particle particle, Vector3d delta) {
        try {
            dxField.setDouble(particle, delta.x);
            dyField.setDouble(particle, delta.y);
            dzField.setDouble(particle, delta.z);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Vector3d[] getRect3d(List<Vector3d> positions) {
        double minX = Double.MAX_VALUE,
                minY = Double.MAX_VALUE,
                minZ = Double.MAX_VALUE,
                maxX = Double.MIN_VALUE,
                maxY = Double.MIN_VALUE,
                maxZ = Double.MIN_VALUE;
        for (Vector3d position : positions) {
            minX = Math.min(position.x, minX);
            minY = Math.min(position.y, minY);
            minZ = Math.min(position.z, minZ);
            maxX = Math.min(position.x, maxX);
            maxY = Math.min(position.y, maxY);
            maxZ = Math.min(position.z, maxZ);
        }
        return new Vector3d[]{
                new Vector3d(minX, minY, minZ),
                new Vector3d(maxX, maxY, maxZ)
        };
    }
}
