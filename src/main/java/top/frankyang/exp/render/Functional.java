package top.frankyang.exp.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.Properties;
import top.frankyang.exp.anime.AnimationMgr;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;

public final class Functional {
    private static final ScriptEngine host = Main.getScriptHost();

    private static Properties getProperties(String expr, Vec3d origin, double thisTick, double finalTick) {
        double x, y, z, dx, dy, dz;
        float a, s;
        int r, g, b, l;

        double percent = thisTick / finalTick;
        try {
            host.put("t", thisTick);
            host.put("f", finalTick);
            host.put("p", percent);
            host.put("x", null);
            host.put("y", null);
            host.put("z", null);
            host.put("r", null);
            host.put("g", null);
            host.put("b", null);
            host.put("dx", null);
            host.put("dy", null);
            host.put("dz", null);
            host.put("a", null);
            host.put("l", null);
            host.put("s", null);
            host.eval(expr);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }

        try {
            Object rx = host.get("x");
            x = (rx != null) ? Double.parseDouble(rx.toString()) : origin.x;
            Object ry = host.get("y");
            y = (ry != null) ? Double.parseDouble(ry.toString()) : origin.y;
            Object rz = host.get("z");
            z = (rz != null) ? Double.parseDouble(rz.toString()) : origin.z;
            Object rr = host.get("r");
            r = (rr != null) ? (int) Double.parseDouble(rr.toString()) : 255;
            Object rg = host.get("g");
            g = (rg != null) ? (int) Double.parseDouble(rg.toString()) : 255;
            Object rb = host.get("b");
            b = (rb != null) ? (int) Double.parseDouble(rb.toString()) : 255;
            Object rdx = host.get("dx");
            dx = (rdx != null) ? (int) Double.parseDouble(rdx.toString()) : 0;
            Object rdy = host.get("dy");
            dy = (rdy != null) ? (int) Double.parseDouble(rdy.toString()) : 0;
            Object rdz = host.get("dz");
            dz = (rdz != null) ? (int) Double.parseDouble(rdz.toString()) : 0;
            Object ra = host.get("a");
            a = (ra != null) ? (float) Double.parseDouble(ra.toString()) : -1;
            Object rl = host.get("l");
            l = (rl != null) ? (int) Double.parseDouble(rl.toString()) : -1;
            Object rs = host.get("s");
            s = (rs != null) ? (float) Double.parseDouble(rs.toString()) : -1;
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        return new Properties(x, y, z, dx, dy, dz, r, g, b, a, l, s);
    }

    public static String renderPattern(ParticleEffect effect,
                                       String data,
                                       Vec3d origin,
                                       double time,
                                       int count,
                                       String id) {
        if (Main.disabled) {
            return null;
        }

        double frameTime = time / count;

        ArrayList<Properties> props = new ArrayList<>();

        for (float i = 0; i < time; i += frameTime) {
            Properties prop;
            try {
                prop = getProperties(
                        data, origin, i / 1e3, time / 1e3
                );
            } catch (RuntimeException e) {
                return e.getMessage();
            }

            props.add(prop);
        }

        if (id != null && !AnimationMgr.exists(id)) {
            return "指定的标识符不是有效的动画。";
        }

        WorkerThread thread = new WorkerThread(
                effect, props, origin, frameTime, count, id
        );
        thread.setDaemon(true);
        thread.start();

        return null;
    }

    private final static class WorkerThread extends Thread {
        private final ParticleEffect effect;
        private final ArrayList<Properties> props;
        private final Vec3d origin;
        private final double frame;
        private final int count;
        private final String id;

        public WorkerThread(ParticleEffect effect, ArrayList<Properties> props, Vec3d origin, double frame, int count, String id) {
            super();
            this.effect = effect;
            this.props = props;
            this.origin = origin;
            this.frame = frame;
            this.count = count;
            this.id = id;
        }

        @Override
        public void run() {
            long frameCount = Math.round(
                    frame * count / Main.frameRate
            );
            long partLength = Math.round(
                    (double) count / frameCount
            );
            ArrayList<Particle> particles;
            particles = new ArrayList<>();

            long i = 0;
            for (Properties data : props) {
                Vec3d pos = new Vec3d(
                        data.x + origin.x,
                        data.y + origin.y,
                        data.z + origin.z
                );
                Vec3d delta = new Vec3d(
                        data.dx, data.dy, data.dz
                );
                Vec3d color = new Vec3d(
                        data.r, data.g, data.b
                );
                if (id != null) {
                    particles.add(Main.constructParticle(
                            effect, pos, delta, color, data.a, data.l, data.s
                    ));
                } else {
                    Main.constructParticle(
                            effect, pos, delta, color, data.a, data.l, data.s
                    );
                }

                if (++i % partLength != 0) {
                    continue;
                }

                try {
                    Thread.sleep(Math.round(1000 / Main.frameRate));  // ~ 30 FPS
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (id != null) {
                AnimationMgr.apply(id, particles);
            }
        }
    }
}
