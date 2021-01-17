package top.frankyang.exp.anime;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.util.math.Vector3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.Properties;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AnimationGroup extends ArrayList<AnimationFrame> {
    private static final Gson gson = new Gson();
    public final double time;
    AnimationGroup nextGroup;

    public AnimationGroup(List<AnimationFrame> frames, double time) {
        this.time = time;
        this.addAll(frames);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static AnimationGroup fromExpr(String expr, double time) {
        Type frameWrapperListType = new TypeToken<List<JsonFrameWrapper>>() {
        }.getType();
        List<JsonFrameWrapper> frameWrapperList = gson.fromJson(expr, frameWrapperListType);

        ArrayList<AnimationFrame> frames = new ArrayList<>();

        for (JsonFrameWrapper frameWrapper : frameWrapperList) {  // Cast
            frames.add(Objects.requireNonNull(frameWrapper.toFrame()));
        }

        if (frames.size() < 2) {
            throw new IllegalArgumentException("关键帧太少，不能推导动画：至少需要两个关键帧。");
        }

        return new AnimationGroup(frames, time);

    }

    public void apply(List<Particle> particles) {
        WorkerThread thread = this.new WorkerThread(particles, nextGroup);
        thread.setDaemon(true);
        thread.start();
    }

    public void setNextGroup(AnimationGroup nextGroup) {
        this.nextGroup = Objects.requireNonNull(nextGroup);
    }

    private final class WorkerThread extends Thread {
        private final List<Particle> particles;
        private final AnimationGroup nextGroup;

        public WorkerThread(List<Particle> particles, AnimationGroup nextGroup) {
            this.particles = particles;
            this.nextGroup = nextGroup;
        }

        @Override
        public void run() {
            long keyFrameCount = AnimationGroup.this.size();
            long realFrameCount = Math.round(
                    time / 1000 * Main.frameRate
            );
            ArrayList<Properties> realFrames = new ArrayList<>();

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
                positions.add(Main.getParticlePos(particle));
            }

            for (Properties realFrame : realFrames) {
                int bound = particles.size();
                for (int i = 0; i < bound; i++) {
                    Particle particle = particles.get(i);
                    Vector3d position = positions.get(i);

                    particle.setPos(
                            realFrame.x + position.x,
                            realFrame.y + position.y,
                            realFrame.z + position.z
                    );
                    Main.setParticleDelta(particle,
                            new Vector3d(
                                    realFrame.dx,
                                    realFrame.dy,
                                    realFrame.dz
                            )
                    );
                    particle.setColor(
                            realFrame.r / 255f,
                            realFrame.g / 255f,
                            realFrame.b / 255f
                    );
                    Main.setParticleAlpha(particle, realFrame.a);
                    Main.setParticleScale(particle, realFrame.s);
                }

                try {
                    Thread.sleep(Math.round(1000 / Main.frameRate));  // ~ 30 FPS
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (nextGroup != null) {
                nextGroup.apply(particles);
            }
        }
    }
}
