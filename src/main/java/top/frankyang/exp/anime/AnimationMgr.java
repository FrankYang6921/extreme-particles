package top.frankyang.exp.anime;

import net.minecraft.client.particle.Particle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AnimationMgr {
    private static final HashMap<String, AnimationGroup> animations = new HashMap<>();

    public static String register(String id, String expr, double time, String then) {
        try {
            AnimationGroup group = AnimationGroup.fromExpr(expr, time);
            if (then != null) {
                AnimationGroup nextGroup = animations.get(then);
                if (nextGroup == null) {
                    return String.format("找不到下一组动画：“%s”", then);
                }
                group.setNextGroup(nextGroup);  // Set the next group
            }
            animations.put(id, group);
        } catch (IllegalArgumentException e) {  // Any thrown error
            return e.getMessage();
        }

        return null;
    }

    public static String withdraw(String id) {
        if (animations.containsKey(id)) {
            return "该标识符未被定义。";
        }

        animations.remove(id);
        return null;
    }

    public static boolean isAbsent(String id) {
        return !animations.containsKey(id);
    }

    public static void apply(String id, ArrayList<Particle> particles) {
        Objects.requireNonNull(animations.get(id)).apply(particles);
    }
}
