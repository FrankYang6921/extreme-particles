package top.frankyang.exp.anime;

import net.minecraft.client.particle.Particle;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class AnimationMgr {
    public static final AnimationMgr INSTANCE = new AnimationMgr();
    private final HashMap<String, AnimationGroup> animations = new HashMap<>();

    private AnimationMgr() {
    }

    public AnimationGroup get(String key) {
        return animations.get(key);
    }

    public AnimationGroup put(String key, AnimationGroup value) {
        return animations.put(key, value);
    }

    public void register(String id, String expr, double time, String then) {
        AnimationGroup group = AnimationGroup.of(expr, time);
        if (then != null) {
            group.setNextGroup(then);  // Set the next group
        }
        animations.put(id, group);
    }

    public void withdraw(String id) {
        if (!animations.containsKey(id)) throw new RuntimeException("该标识符未被定义。");
        animations.remove(id);
    }

    public boolean isAbsent(String id) {
        return !animations.containsKey(id);
    }

    public void apply(String id, List<Particle> particles, boolean withdrawAfter, boolean killAllAfter) {
        Objects.requireNonNull(animations.get(id), "该标识符未被定义。").apply(particles, withdrawAfter, killAllAfter);
    }
}
