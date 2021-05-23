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

    public void put(String groupName, Particle particle) {
        groupMap.computeIfAbsent(
                groupName, s -> new ParticleGroup()
        ).add(particle);
    }

    public void put(String groupName, Collection<Particle> particle) {
        groupMap.computeIfAbsent(
                groupName, s -> new ParticleGroup()
        ).addAll(particle);
    }

    public List<Particle> get(String groupName) {
        List<Particle> groupList = groupMap.get(groupName);
        return groupList != null ? groupList : Collections.emptyList();  // If not found, returns an empty list instead.
    }
}
