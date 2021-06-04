package top.frankyang.exp.group;

import net.minecraft.client.particle.Particle;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ParticleGroupMgr {
    public static final ParticleGroupMgr INSTANCE = new ParticleGroupMgr();

    private final ParticleGroupMap groupMap = new ParticleGroupMap();

    private ParticleGroupMgr() {
    }

    public void putParticles(String groupName, Collection<Particle> particles) {
        List<Particle> group = groupMap.computeIfAbsent(
                groupName, s -> new ParticleGroup()
        );
        group.addAll(particles);
    }

    public List<Particle> getOrEmpty(String groupName) {
        if (!groupMap.containsKey(groupName))
            return Collections.emptyList();
        return groupMap.get(groupName);
    }

    public List<Particle> getOrThrow(String groupName) {
        if (!groupMap.containsKey(groupName))
            throw new IllegalArgumentException();
        return groupMap.get(groupName);
    }

    public List<Particle> getOrCreate(String groupName) {
        groupMap.computeIfAbsent(groupName, s -> new ParticleGroup());
        return groupMap.get(groupName);
    }
}
