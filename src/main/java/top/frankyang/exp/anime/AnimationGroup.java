package top.frankyang.exp.anime;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.util.math.Vector3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.Util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static top.frankyang.exp.Util.getRect3d;

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
            ArrayList<AnimationFrame> realFrames = new ArrayList<>();

            for (int i = 0; i < keyFrameCount - 1; i++) {
                AnimationFrame thisKeyFrame = AnimationGroup.this.get(i);
                AnimationFrame nextKeyFrame = AnimationGroup.this.get(i + 1);

                double realForKey = realFrameCount * (nextKeyFrame.percentage - thisKeyFrame.percentage) / 100;
                for (int j = 0; j < realForKey; j++) {
                    realFrames.add(  // Make a frame of the animation
                            thisKeyFrame.compositeWith(nextKeyFrame, j / realForKey * 100)
                    );
                }
            }

            ArrayList<Vector3d> positions = new ArrayList<>();
            for (Particle particle : particles) {
                positions.add(Util.getParticlePos(particle));
            }
            Vector3d[] rect3d = getRect3d(positions);
            double mx = rect3d[0].x;
            double my = rect3d[0].y;
            double mz = rect3d[0].z;

            for (AnimationFrame realFrame : realFrames) {
                int bound = particles.size();

                boolean canDoDelta = realFrame.dx != 0 || realFrame.dy != 0 || realFrame.dz != 0;
                boolean canDoColor = realFrame.r != 0 || realFrame.g != 0 || realFrame.b != 0;
                boolean canDoAlpha = realFrame.a != 0;
                boolean canDoScale = realFrame.s != 0;
                boolean canDoTransform = !realFrame.transform.equals(Transform.NOTHING);
                double ox = 0, oy = 0, oz = 0;

                if (canDoTransform) {
                    ox = realFrame.x;
                    oy = realFrame.y;
                    oz = realFrame.z;
                }

                for (int i = 0; i < bound; i++) {
                    Particle particle = particles.get(i);
                    Vector3d position = positions.get(i);

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
                    if (canDoDelta) {
                        Util.setParticleDelta(particle,
                                new Vector3d(
                                        realFrame.dx,
                                        realFrame.dy,
                                        realFrame.dz
                                )
                        );
                    }
                    if (canDoColor) {
                        particle.setColor(
                                realFrame.r / 255f,
                                realFrame.g / 255f,
                                realFrame.b / 255f
                        );
                    }
                    if (canDoAlpha) {
                        Util.setParticleAlpha(particle, realFrame.a);
                    }
                    if (canDoScale) {
                        Util.setParticleScale(particle, realFrame.s);
                    }
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
