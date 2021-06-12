package top.frankyang.exp.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.Property;
import top.frankyang.exp.ThreadMgr;
import top.frankyang.exp.internal.Renderer;
import top.frankyang.exp.internal.RendererContext;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public final class Functional implements Renderer {
    public static final Functional INSTANCE = new Functional();

    private static final ScriptEngine host = Main.getScriptHost();

    private Functional() {
    }

    private synchronized static Property getProperty(String expr, Vec3d origin, double thisTick, double finalTick) {
        double x, y, z, dx, dy, dz;
        float a, s;
        int r, g, b, l;

        double progress = thisTick / finalTick;
        try {
            host.put("t", thisTick);
            host.put("f", finalTick);
            host.put("p", progress);
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

        return new Property(x, y, z, dx, dy, dz, r, g, b, a, l, s);
    }

    public static void renderMain(ParticleEffect effect, String data, Vec3d origin, double time, int count) {
        if (Main.isParticleConstructionPaused()) {
            return;
        }

        double frameTime = time / count;

        List<Property> props = new ArrayList<>();

        for (float i = 0; i < time; i += frameTime) {
            props.add(getProperty(
                    data, origin, i / 1e3, time / 1e3
            ));
        }

        ThreadMgr.INSTANCE.getParallelPool().submit(() -> {
            long frameCount = Math.round(
                    frameTime * count / Main.getGlobalAnimationFrameRate()
            );
            long partLength = Math.round(
                    (double) count / frameCount
            );
            ArrayList<Particle> particles;

            long i = 0;
            for (Property prop : props) {
                Vec3d pos = new Vec3d(
                        prop.x + origin.x,
                        prop.y + origin.y,
                        prop.z + origin.z
                );
                Vec3d delta = new Vec3d(
                        prop.dx, prop.dy, prop.dz
                );
                Vec3d color = new Vec3d(
                        prop.r, prop.g, prop.b
                );
                Main.constructParticle(
                        effect, pos, delta, color, prop.a, prop.l, prop.s
                );


                if (++i % partLength != 0) {
                    continue;
                }

                ThreadMgr.INSTANCE.waitForFrame();
            }
        });
    }

    @Override
    public void renderContext(RendererContext rendererContext) {
        if (!(rendererContext instanceof FunctionalContext)) {
            throw new IllegalArgumentException("Invalid context type.");
        }
        FunctionalContext c = (FunctionalContext) rendererContext;
        c.catchFeedback(
                () -> renderMain(c.effect, c.data, c.origin, c.time, c.count)
        );
    }

    public static class FunctionalContext extends RendererContext {
        public final ParticleEffect effect;
        public final String data;
        public final Vec3d origin;
        public final double time;
        public final int count;

        public FunctionalContext(ParticleEffect effect, String data, Vec3d origin, double time, int count) {
            this.effect = effect;
            this.data = data;
            this.origin = origin;
            this.time = time;
            this.count = count;
        }

        @Override
        public String getSuccessfulFeedback() {
            return "通过函数批量构造了粒子。";
        }
    }
}
