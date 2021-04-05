package top.frankyang.exp.anime;

import net.minecraft.client.particle.Particle;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class AnimationMgr {
    private AnimationMgr() {

    }

    private static final HashMap<String, AnimationGroup> animations = new HashMap<>();

    public static void register(String id, String expr, double time, String then) {
        AnimationGroup group = AnimationGroup.of(expr, time);
        if (then != null) {
            AnimationGroup nextGroup = animations.get(then);
            if (nextGroup == null) {
                throw new RuntimeException("下一组动画的标识符未被定义。");
            }
            group.setNextGroup(nextGroup);  // Set the next group
        }
        animations.put(id, group);
    }

    public static void withdraw(String id) {
        if (!animations.containsKey(id)) {
            throw new RuntimeException("该标识符未被定义。");
        }
        animations.remove(id);
    }

    public static boolean isAbsent(String id) {
        return !animations.containsKey(id);
    }

    public static void apply(String id, List<Particle> particles) {
        Objects.requireNonNull(animations.get(id), "该标识符未被定义。").apply(particles);
    }
}
