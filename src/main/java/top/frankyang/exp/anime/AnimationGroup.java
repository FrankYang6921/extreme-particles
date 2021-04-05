package top.frankyang.exp.anime;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.util.math.Vector3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.ParticleUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static top.frankyang.exp.Main.frameSignal;
import static top.frankyang.exp.ParticleUtils.*;

public class AnimationGroup extends ArrayList<AnimationFrame> {
    private static final Gson gson = new Gson();
    private final double time;
    private AnimationGroup nextGroup = null;

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

    public void apply(List<Particle> particles) {
        Main.pool.submit(() -> {
            long keyFrameCount = AnimationGroup.this.size();
            long realFrameCount = Math.round(
                    time / 1000 * Main.frameRate
            );
            ArrayList<AnimationFrame> realFrames = new ArrayList<>();

            for (int i = 0; i < keyFrameCount - 1; i++) {
                AnimationFrame thisKeyFrame = AnimationGroup.this.get(i);
                AnimationFrame nextKeyFrame = AnimationGroup.this.get(i + 1);

                double realForKey = realFrameCount * (nextKeyFrame.pace - thisKeyFrame.pace) / 100;
                for (int j = 0; j < realForKey; j++) {
                    realFrames.add(  // Make a frame of the animation
                            thisKeyFrame.compositeWith(nextKeyFrame, j / realForKey * 100)
                    );
                }
            }

            ArrayList<Vector3d> positions = new ArrayList<>();
            for (Particle particle : particles) {
                positions.add(ParticleUtils.getParticlePos(particle));
            }
            Vector3d[] rect3d = getRect3d(positions);
            double mx = rect3d[0].x;
            double my = rect3d[0].y;
            double mz = rect3d[0].z;

            for (AnimationFrame realFrame : realFrames) {
                int bound = particles.size();

                boolean canDoAlpha = realFrame.a >= 0;
                boolean canDoScale = realFrame.s >= 0;
                boolean canDoTransform = !realFrame.transform.equals(Transform.EMPTY);
                double ox = 0, oy = 0, oz = 0;

                if (canDoTransform) {
                    ox = !Double.isNaN(realFrame.x) ? realFrame.x : -((rect3d[1].x - mx) / 2);
                    oy = !Double.isNaN(realFrame.y) ? realFrame.y : -((rect3d[1].y - my) / 2);
                    oz = !Double.isNaN(realFrame.z) ? realFrame.z : -((rect3d[1].z - mz) / 2);
                }

                for (int i = 0; i < bound; i++) {
                    Particle particle = particles.get(i);
                    Vector3d position = positions.get(i);

                    if (!particle.isAlive()) {
                        continue;
                    }

                    if (canDoTransform) {
                        double rx = position.x - mx + ox;
                        double ry = position.y - my + oy;
                        double rz = position.z - mz + oz;

                        Vector3d then = realFrame.transform.evaluateOn(new Vector3d(rx, ry, rz));

                        particle.setPos(
                                then.x + mx - ox,
                                then.y + my - oy,
                                then.z + mz - oz
                        );
                    }

                    Vector3d d = getParticleDelta(particle);
                    ParticleUtils.setParticleDelta(particle,
                            new Vector3d(
                                    Double.isNaN(realFrame.dx) ? d.x : realFrame.dx,
                                    Double.isNaN(realFrame.dy) ? d.y : realFrame.dy,
                                    Double.isNaN(realFrame.dz) ? d.z : realFrame.dz
                            )
                    );

                    Vector3d c = getParticleColor(particle);
                    ParticleUtils.setParticleColor(particle,
                            new Vector3d(
                                    realFrame.r < 0 ? c.x : realFrame.r / 255f,
                                    realFrame.g < 0 ? c.y : realFrame.g / 255f,
                                    realFrame.b < 0 ? c.z : realFrame.b / 255f
                            )
                    );

                    if (canDoAlpha) {
                        ParticleUtils.setParticleAlpha(particle, realFrame.a);
                    }

                    if (canDoScale) {
                        ParticleUtils.setParticleScale(particle, realFrame.s);
                    }
                }

                try {
                    synchronized (frameSignal) {
                        frameSignal.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (nextGroup != null) nextGroup.apply(particles);
        });
    }

    public void setNextGroup(AnimationGroup nextGroup) {
        this.nextGroup = Objects.requireNonNull(nextGroup);
    }
}
