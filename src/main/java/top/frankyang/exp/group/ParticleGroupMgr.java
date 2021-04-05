package top.frankyang.exp.group;

import net.minecraft.client.particle.Particle;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class ParticleGroupMgr {
    private static final ParticleGroupMap groupMap = new ParticleGroupMap();

    private ParticleGroupMgr() {
    }

    public static void put(String groupName, Particle particle) {
        groupMap.computeIfAbsent(
                groupName, s -> new ParticleGroup()
        ).add(particle);
    }

    public static void put(String groupName, Collection<Particle> particle) {
        groupMap.computeIfAbsent(
                groupName, s -> new ParticleGroup()
        ).addAll(particle);
    }

    public static List<Particle> get(String groupName) {
        return Objects.requireNonNull(groupMap.get(groupName), "该粒子组未被定义。");
    }

    public static void deathHook(Object dead) {
        groupMap.deathHook(dead);
    }
}
