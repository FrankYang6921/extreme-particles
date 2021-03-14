package top.frankyang.exp;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.util.math.Vector3d;
import top.frankyang.exp.mixin.ReflectiveParticle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "JavaReflectionMemberAccess"})
public final class Util {
    private static final Map<Particle, Float> mapping = new HashMap<>();

    public static Vector3d getParticleColor(Particle particle) {
        ReflectiveParticle i = (ReflectiveParticle) particle;

        double x, y, z;
        x = i.getColorRed();
        y = i.getColorGreen();
        z = i.getColorBlue();

        return new Vector3d(x, y, z);
    }

    public static void setParticleColor(Particle particle, Vector3d color) {
        ReflectiveParticle i = (ReflectiveParticle) particle;

        i.setColorRed((float) color.x);
        i.setColorGreen((float) color.y);
        i.setColorBlue((float) color.z);
    }

    public static float getParticleAlpha(Particle particle) {
        return ((ReflectiveParticle) particle).getColorAlpha();
    }

    public static void setParticleAlpha(Particle particle, float alpha) {
        ((ReflectiveParticle) particle).setColorAlpha(alpha);
    }

    public static int getParticleLife(Particle particle) {
        return ((ReflectiveParticle) particle).getMaxAge();
    }

    public static void setParticleLife(Particle particle, int life) {
        ((ReflectiveParticle) particle).setMaxAge(life);
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
        ReflectiveParticle i = (ReflectiveParticle) particle;

        double x, y, z;
        x = i.getX();
        y = i.getY();
        z = i.getZ();

        return new Vector3d(x, y, z);
    }

    public static void setParticlePos(Particle particle, Vector3d pos) {
        ReflectiveParticle i = (ReflectiveParticle) particle;
        i.setX(pos.x);
        i.setY(pos.y);
        i.setZ(pos.z);
    }

    public static Vector3d getParticleDelta(Particle particle) {
        ReflectiveParticle i = (ReflectiveParticle) particle;

        double x, y, z;
        x = i.getVelocityX();
        y = i.getVelocityY();
        z = i.getVelocityZ();

        return new Vector3d(x, y, z);
    }

    public static void setParticleDelta(Particle particle, Vector3d delta) {
        ReflectiveParticle i = (ReflectiveParticle) particle;

        i.setVelocityX(delta.x);
        i.setVelocityY(delta.y);
        i.setVelocityZ(delta.z);
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
