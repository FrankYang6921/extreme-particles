package top.frankyang.exp;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.util.math.Vector3d;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("unused")
public final class Util {
    public static Vector3d getParticleColor(Particle particle) {
        Class<?> clazz = particle.getClass();

        double x, y, z;
        try {
            Field field = clazz.getField("colorRed");
            field.setAccessible(true);
            x = field.getDouble(particle);
            field = clazz.getField("colorGreen");
            field.setAccessible(true);
            y = field.getDouble(particle);
            field = clazz.getField("colorBlue");
            field.setAccessible(true);
            z = field.getDouble(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return new Vector3d(x, y, z);
    }

    public static void setParticleColor(Particle particle, Vector3d color) {
        Class<?> clazz = particle.getClass();

        try {
            Field field = clazz.getField("colorRed");
            field.setAccessible(true);
            field.setDouble(particle, color.x);
            field = clazz.getField("colorGreen");
            field.setAccessible(true);
            field.setDouble(particle, color.y);
            field = clazz.getField("colorBlue");
            field.setAccessible(true);
            field.setDouble(particle, color.z);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static float getParticleAlpha(Particle particle) {
        Class<?> clazz = particle.getClass();
        try {
            Field field = clazz.getField("colorAlpha");
            field.setAccessible(true);
            return field.getFloat(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setParticleAlpha(Particle particle, float alpha) {
        Class<?> clazz = particle.getClass();
        try {
            Field field = clazz.getField("colorAlpha");
            field.setAccessible(true);
            field.set(particle, alpha);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getParticleLife(Particle particle) {
        Class<?> clazz = particle.getClass();
        try {
            Field field = clazz.getField("maxAge");
            field.setAccessible(true);
            return field.getInt(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setParticleLife(Particle particle, int life) {
        Class<?> clazz = particle.getClass();
        try {
            Field field = clazz.getField("maxAge");
            field.setAccessible(true);
            field.set(particle, life);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static float getParticleScale(Particle particle) {
        return Float.NaN;  // Not implemented
    }

    public static void setParticleScale(Particle particle, float scale) {
        particle.scale(scale);  // Not implemented
    }

    public static Vector3d getParticlePos(Particle particle) {
        Class<?> clazz = particle.getClass();

        double x, y, z;
        try {
            Field field = clazz.getField("x");
            field.setAccessible(true);
            x = field.getDouble(particle);
            field = clazz.getField("y");
            field.setAccessible(true);
            y = field.getDouble(particle);
            field = clazz.getField("z");
            field.setAccessible(true);
            z = field.getDouble(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return new Vector3d(x, y, z);
    }

    public static void setParticlePos(Particle particle, Vector3d pos) {
        Class<?> clazz = particle.getClass();

        try {
            Field field = clazz.getField("x");
            field.setAccessible(true);
            field.setDouble(particle, pos.x);
            field = clazz.getField("y");
            field.setAccessible(true);
            field.setDouble(particle, pos.y);
            field = clazz.getField("z");
            field.setAccessible(true);
            field.setDouble(particle, pos.z);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Vector3d getParticleDelta(Particle particle) {
        Class<?> clazz = particle.getClass();

        double x, y, z;
        try {
            Field field = clazz.getField("velocityX");
            field.setAccessible(true);
            x = field.getDouble(particle);
            field = clazz.getField("velocityY");
            field.setAccessible(true);
            y = field.getDouble(particle);
            field = clazz.getField("velocityZ");
            field.setAccessible(true);
            z = field.getDouble(particle);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return new Vector3d(x, y, z);
    }

    public static void setParticleDelta(Particle particle, Vector3d delta) {
        Class<?> clazz = particle.getClass();

        try {
            Field field = clazz.getField("velocityX");
            field.setAccessible(true);
            field.setDouble(particle, delta.x);
            field = clazz.getField("velocityY");
            field.setAccessible(true);
            field.setDouble(particle, delta.y);
            field = clazz.getField("velocityZ");
            field.setAccessible(true);
            field.setDouble(particle, delta.z);
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
