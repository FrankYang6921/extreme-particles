package top.frankyang.exp;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.util.math.Vector3d;
import top.frankyang.exp.mixin.ParticleReflector;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings({"unused"})
public final class ParticleUtils {
    private static final Map<Particle, Float> scaleCache = new WeakHashMap<>();

    private ParticleUtils() {
    }

    public static Vector3d getParticleColor(Particle particle) {
        ParticleReflector i = (ParticleReflector) particle;

        double x, y, z;
        x = i.getColorRed();
        y = i.getColorGreen();
        z = i.getColorBlue();

        return new Vector3d(x, y, z);
    }

    public static void setParticleColor(Particle particle, Vector3d color) {
        ParticleReflector i = (ParticleReflector) particle;

        i.setColorRed((float) color.x);
        i.setColorGreen((float) color.y);
        i.setColorBlue((float) color.z);
    }

    public static float getParticleAlpha(Particle particle) {
        return ((ParticleReflector) particle).getColorAlpha();
    }

    public static void setParticleAlpha(Particle particle, float alpha) {
        ((ParticleReflector) particle).setColorAlpha(alpha);
    }

    public static int getParticleLife(Particle particle) {
        return ((ParticleReflector) particle).getMaxAge();
    }

    public static void setParticleLife(Particle particle, int life) {
        ((ParticleReflector) particle).setMaxAge(life);
    }

    public static float getParticleScale(Particle particle) {
        return scaleCache.getOrDefault(particle, 1f);
    }

    public static void setParticleScale(Particle particle, float scale) {
        float relativeScale = scale / getParticleScale(particle);
        particle.scale(relativeScale);
        scaleCache.put(particle, scale);
    }

    public static Vector3d getParticlePos(Particle particle) {
        ParticleReflector i = (ParticleReflector) particle;

        double x, y, z;
        x = i.getX();
        y = i.getY();
        z = i.getZ();

        return new Vector3d(x, y, z);
    }

    public static void setParticlePos(Particle particle, Vector3d pos) {
        ParticleReflector i = (ParticleReflector) particle;
        i.setX(pos.x);
        i.setY(pos.y);
        i.setZ(pos.z);
    }

    public static Vector3d getParticleDelta(Particle particle) {
        ParticleReflector i = (ParticleReflector) particle;

        double x, y, z;
        x = i.getVelocityX();
        y = i.getVelocityY();
        z = i.getVelocityZ();

        return new Vector3d(x, y, z);
    }

    public static void setParticleDelta(Particle particle, Vector3d delta) {
        ParticleReflector i = (ParticleReflector) particle;

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
            maxX = Math.max(position.x, maxX);
            maxY = Math.max(position.y, maxY);
            maxZ = Math.max(position.z, maxZ);
        }
        return new Vector3d[]{
                new Vector3d(minX, minY, minZ),
                new Vector3d(maxX, maxY, maxZ)
        };
    }

    public static Property getParticleProperty(Particle particle) {
        Vector3d pos = getParticlePos(particle);
        Vector3d delta = getParticleDelta(particle);
        Vector3d color = getParticleColor(particle);
        return new Property(pos.x,
                pos.y,
                pos.z,
                delta.x,
                delta.y,
                delta.z,
                (int) color.x * 255,
                (int) color.y * 255,
                (int) color.z * 255,
                getParticleAlpha(particle),
                getParticleLife(particle),
                getParticleScale(particle)
        );
    }
}
