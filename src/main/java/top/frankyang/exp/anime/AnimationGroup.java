package top.frankyang.exp.anime;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.util.math.Vector3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.Particles;
import top.frankyang.exp.Property;
import top.frankyang.exp.ThreadMgr;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static top.frankyang.exp.Particles.getRect3d;

public class AnimationGroup extends ArrayList<AnimationFrame> {
    private static final Gson gson = new Gson();
    private final double time;
    private String nextGroupId = null;

    public AnimationGroup(List<AnimationFrame> frames, double time) {
        this.time = time;
        this.addAll(frames);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static AnimationGroup of(String expr, double time) {
        Type frameWrapperListType = new TypeToken<List<FrameWrapper>>() {
        }.getType();

        List<FrameWrapper> frameWrapperList;
        try {
            frameWrapperList = gson.fromJson(
                    Objects.requireNonNull(expr), frameWrapperListType
            );
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("给定的JSON字符串无效：" + e.getMessage());
        }


        ArrayList<AnimationFrame> frames = new ArrayList<>();

        for (FrameWrapper frameWrapper : frameWrapperList) {  // Perform cast
            frames.add(Objects.requireNonNull(frameWrapper.toFrame()));
        }

        if (frames.size() < 2) {
            throw new IllegalArgumentException("关键帧太少，不能推导动画。至少需要两个关键帧。");
        }

        return new AnimationGroup(frames, time);
    }

    private void apply(boolean isInPool, List<Particle> particles, boolean withdrawNow, boolean killAllAfter) {
        if (isInPool) {  // No extra thread creation when is already in a pool.
            apply0(particles, killAllAfter);
        } else {
            ThreadMgr.INSTANCE.getParallelPool().submit(() -> {
                List<Particle> particles0 = new ArrayList<>();
                particles.removeIf(p -> {
                    particles0.add(p);
                    return withdrawNow;
                });
                apply0(particles0, killAllAfter);
            });
        }
    }


    public void apply(List<Particle> particles, boolean withdrawNow, boolean killAllAfter) {
        apply(false, particles, withdrawNow, killAllAfter);
    }

    private void apply0(List<Particle> particles, boolean killAllAfter) {
        long keyFrameCount = AnimationGroup.this.size();
        long realFrameCount = Math.round(
                time / 1000 * Main.getGlobalAnimationFrameRate()
        );
        List<AnimationFrame> realFrames = new ArrayList<>();
        for (int i = 0; i < keyFrameCount - 1; i++) {
            AnimationFrame thisKeyFrame = AnimationGroup.this.get(i);
            AnimationFrame nextKeyFrame = AnimationGroup.this.get(i + 1);

            double realForKey = realFrameCount * (nextKeyFrame.progress - thisKeyFrame.progress) / 100;
            for (int j = 0; j < realForKey; j++) {
                realFrames.add(  // Make a frame of the animation
                        thisKeyFrame.compositeWith(nextKeyFrame, j / realForKey * 100)
                );
            }
        }

        List<Property> properties = new ArrayList<>();
        for (Particle particle : particles) {
            properties.add(Particles.getParticleProperty(particle));
        }
        Vector3d[] rect3d = getRect3d(
                properties.stream().map(p -> new Vector3d(p.x, p.y, p.z)).collect(Collectors.toList())
        );
        double mx = rect3d[0].x,
                my = rect3d[0].y,
                mz = rect3d[0].z;

        for (AnimationFrame realFrame : realFrames) {
            int bound = particles.size();

            boolean canDoAlpha = realFrame.a >= 0;
            boolean canDoScale = realFrame.s >= 0;
            boolean canDoTransform = !realFrame.transform.isEffectivelyEmpty();
            double ox = 0,
                    oy = 0,
                    oz = 0;

            if (canDoTransform) {  // Origin is required only for transforms.
                ox = !Double.isNaN(realFrame.x) ? realFrame.x : -((rect3d[1].x - mx) / 2);
                oy = !Double.isNaN(realFrame.y) ? realFrame.y : -((rect3d[1].y - my) / 2);
                oz = !Double.isNaN(realFrame.z) ? realFrame.z : -((rect3d[1].z - mz) / 2);
            }

            for (int i = 0; i < bound; i++) {
                Particle particle = particles.get(i);
                Property property = properties.get(i);
                apply0(particle,
                        property,
                        mx, my, mz,
                        ox, oy, oz,
                        realFrame,
                        canDoAlpha,
                        canDoScale,
                        canDoTransform);
            }

            ThreadMgr.INSTANCE.waitForFrame();
        }

        if (hasNextGroup())
            getNextGroup().apply(true, particles, false, killAllAfter);
        else if (killAllAfter)
            particles.forEach(Particle::markDead);
    }

    private void apply0(Particle particle, Property property, double mx, double my, double mz, double ox, double oy, double oz, AnimationFrame frame, boolean canDoAlpha, boolean canDoScale, boolean canDoTransform) {
        if (!particle.isAlive()) {
            return;
        }

        if (canDoTransform) {
            double rx = property.x - mx + ox;
            double ry = property.y - my + oy;
            double rz = property.z - mz + oz;

            Vector3d pos = frame.transform.evaluateOn(new Vector3d(rx, ry, rz));

            particle.setPos(
                    pos.x + mx - ox,
                    pos.y + my - oy,
                    pos.z + mz - oz
            );
        }

        switch (frame.blendMode) {
            case NORMAL:
                Particles.setParticleColor(particle,
                        new Vector3d(
                                frame.r < 0 ? property.r / 255f : frame.r / 255f,
                                frame.g < 0 ? property.g / 255f : frame.g / 255f,
                                frame.b < 0 ? property.b / 255f : frame.b / 255f
                        )
                );
                break;
            case MULTIPLY:
                Particles.setParticleColor(particle,
                        new Vector3d(
                                frame.r < 0 ? property.r / 255f : frame.r * property.r / 255f / 255f,
                                frame.g < 0 ? property.g / 255f : frame.g * property.g / 255f / 255f,
                                frame.b < 0 ? property.b / 255f : frame.b * property.b / 255f / 255f
                        )
                );
                break;
            case SCREEN:
                Particles.setParticleColor(particle,
                        new Vector3d(
                                frame.r < 0 ? property.r / 255f : 255 - (255 - frame.r) * (255 - property.r) / 255f / 255f,
                                frame.g < 0 ? property.g / 255f : 255 - (255 - frame.g) * (255 - property.g) / 255f / 255f,
                                frame.b < 0 ? property.b / 255f : 255 - (255 - frame.b) * (255 - property.b) / 255f / 255f
                        )
                );
                break;
            case AVERAGE:
                Particles.setParticleColor(particle,
                        new Vector3d(
                                frame.r < 0 ? property.r / 255f : (frame.r + property.r) / 2f / 255f,
                                frame.g < 0 ? property.g / 255f : (frame.g + property.g) / 2f / 255f,
                                frame.b < 0 ? property.b / 255f : (frame.b + property.b) / 2f / 255f
                        )
                );
                break;
            case LIGHTEN:
                Particles.setParticleColor(particle,
                        new Vector3d(
                                frame.r < 0 ? property.r / 255f : Math.max(property.r / 255f, frame.r / 255f),
                                frame.g < 0 ? property.g / 255f : Math.max(property.g / 255f, frame.g / 255f),
                                frame.b < 0 ? property.b / 255f : Math.max(property.b / 255f, frame.b / 255f)
                        )
                );
                break;
            case DARKEN:
                Particles.setParticleColor(particle,
                        new Vector3d(
                                frame.r < 0 ? property.r / 255f : Math.min(property.r / 255f, frame.r / 255f),
                                frame.g < 0 ? property.g / 255f : Math.min(property.g / 255f, frame.g / 255f),
                                frame.b < 0 ? property.b / 255f : Math.min(property.b / 255f, frame.b / 255f)
                        )
                );
                break;
        }

        if (canDoAlpha) {
            Particles.setParticleAlpha(particle, frame.a);
        }

        if (canDoScale) {
            Particles.setParticleScale(particle, frame.s);
        }
    }

    public boolean hasNextGroup() {
        return !AnimationMgr.INSTANCE.isAbsent(nextGroupId);
    }

    public AnimationGroup getNextGroup() {
        return AnimationMgr.INSTANCE.get(nextGroupId);
    }

    public void setNextGroup(String value) {
        nextGroupId = value;
    }
}
