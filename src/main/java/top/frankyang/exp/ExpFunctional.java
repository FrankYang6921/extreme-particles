package top.frankyang.exp;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Objects;

public class ExpFunctional {
    private static final ScriptEngine host;

    static {
        ScriptEngineManager factory = new ScriptEngineManager();
        host = factory.getEngineByName("JavaScript");
        Objects.requireNonNull(host);
        try {  // Remove the latency at the first command call
            host.eval("null");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    static ParticleProperties getProperties(String expr, Vec3d origin, double thisTick, double finalTick) {
        double x, y, z;
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
            Object ra = host.get("a");
            a = (ra != null) ? (float) Double.parseDouble(ra.toString()) : -1;
            Object rl = host.get("l");
            l = (rl != null) ? (int) Double.parseDouble(rl.toString()) : -1;
            Object rs = host.get("s");
            s = (rs != null) ? (float) Double.parseDouble(rs.toString()) : -1;
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        return new ParticleProperties(x, y, z, r, g, b, a, l, s);
    }

    static String renderPattern(ParticleEffect effect, String expr, Vec3d origin, double time, int count) {
        if (ExpMain.disabled) {
            return null;
        }

        double frameTime = time / count;
        ArrayList<ParticleProperties> props = new ArrayList<>();

        for (float i = 0; i < time; i += frameTime) {
            ParticleProperties data;
            try {
                data = getProperties(
                        expr, origin, i / 1e3, time / 1e3
                );
            } catch (RuntimeException e) {
                return e.getMessage();
            }

            props.add(data);
        }

        AnimatorThread thread = new AnimatorThread(effect, props, origin, frameTime);
        thread.setDaemon(true);
        thread.start();

        return null;
    }

    private static class ParticleProperties {
        final double x, y, z;
        final float a, s;
        final int r, g, b, l;

        public ParticleProperties(double x, double y, double z, int r, int g, int b, float a, int l, float s) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.s = s;
            this.l = l;
        }
    }

    private static class AnimatorThread extends Thread {
        ParticleEffect effect;
        ArrayList<ParticleProperties> props;
        Vec3d origin;
        double frame;

        public AnimatorThread(ParticleEffect effect, ArrayList<ParticleProperties> props, Vec3d origin, double frame) {
            super();
            this.effect = effect;
            this.props = props;
            this.origin = origin;
            this.frame = frame;
        }

        @Override
        public void run() {
            for (ParticleProperties data : props) {
                long begin = System.nanoTime();  // Main logic begins

                Vec3d pos = new Vec3d(
                        data.x + origin.x,
                        data.y + origin.y,
                        data.z + origin.z
                );
                Vec3d color = new Vec3d(
                        data.r, data.g, data.b
                );
                ExpMain.constructParticle(effect, pos, Vec3d.ZERO, color, data.a, data.l, data.s);

                long end = System.nanoTime();  // Main logic ends

                double elapsed = (end - begin) / 1e3 / 1e3;
                long sleep = (long) (frame - elapsed);
                try {
                    Thread.sleep(Math.max(sleep, 0L));
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
