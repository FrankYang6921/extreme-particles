package top.frankyang.exp;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import top.frankyang.exp.anime.AnimationMgr;
import top.frankyang.exp.group.ParticleGroupMgr;
import top.frankyang.exp.internal.RendererManager;
import top.frankyang.exp.render.Functional;
import top.frankyang.exp.render.RenderImg;
import top.frankyang.exp.render.RenderSvg;
import top.frankyang.exp.render.RenderTxt;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.ParticleArgumentType.getParticle;
import static net.minecraft.command.argument.ParticleArgumentType.particle;
import static net.minecraft.command.argument.Vec2ArgumentType.getVec2;
import static net.minecraft.command.argument.Vec3ArgumentType.getVec3;
import static net.minecraft.command.argument.Vec3ArgumentType.vec3;


public final class Main implements ClientModInitializer {
    public static final ExecutorService pool = Executors.newCachedThreadPool();
    public static final ParticleDaemon particleDaemon = new ParticleDaemon();
    public static final Object frameSignal = new Object();
    private static final int MAJOR_VERSION = 0;
    private static final int MINOR_VERSION = 5;
    private static final int REVISION = 2;
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static boolean disabled = false;
    public static boolean isAsync = false;
    public static boolean fixLife = true;
    public static boolean isUnsafe = true;
    public static double frameRate = 30.3d;

    private static ScriptEngine host;

    static {
        pool.submit(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (frameSignal) {
                    frameSignal.notifyAll();
                }

                try {
                    //noinspection BusyWait
                    Thread.sleep(Math.round(1000 / Main.frameRate));  // ~ 30 FPS
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
            }
        });
    }

    private static void catchFeedback(Runnable r, String successful, CommandContext<ServerCommandSource> context) {
        try {
            r.run();
            context.getSource().sendFeedback(Text.of(successful), true);
        } catch (Exception e) {
            context.getSource().sendError(Text.of(e.getMessage()));
        }
    }

    private synchronized static Particle addParticle(
            ParticleEffect effect,
            double x,
            double y,
            double z,
            double dx,
            double dy,
            double dz) {
        return Objects.requireNonNull(client.particleManager.addParticle(effect, x, y, z, dx, dy, dz));
    }

    public static Particle constructParticle(ParticleEffect effect, Vec3d position, Vec3d delta, Vec3d color, float alpha, int life, float scale) {
        if (disabled) {
            return null;
        }

        double x = position.x;
        double y = position.y;
        double z = position.z;
        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;

        Particle particle = addParticle(effect, x, y, z, dx, dy, dz);


        if (color != null) {
            particle.setColor(
                    (float) color.x / 255.0f,
                    (float) color.y / 255.0f,
                    (float) color.z / 255.0f
            );
        }
        if (alpha >= 0) {
            ParticleUtils.setParticleAlpha(particle, alpha);
        }
        if (scale >= 0) {
            ParticleUtils.setParticleScale(particle, scale);
        }
        if (life >= 0) {
            ParticleUtils.setParticleLife(particle, life);
            if (fixLife) {
                particleDaemon.offer(particle);
            }
        }

        return particle;
    }

    @SuppressWarnings("unchecked")
    private static Map<ParticleTextureSheet, Queue<Particle>> getParticles() {
        ParticleManager mgr = client.particleManager;

        Class<?> clazz = mgr.getClass();
        Object wrapper;

        try {
            Field field = clazz.getDeclaredField("particles");
            field.setAccessible(true);
            wrapper = field.get(mgr);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return (Map<ParticleTextureSheet, Queue<Particle>>) wrapper;
    }

    private static synchronized void showAll() {
        Map<ParticleTextureSheet, Queue<Particle>> particles = getParticles();

        // For every kind of particle
        particles.forEach((particleTextureSheet, queue) -> {
            // For every single particle
            for (Particle particle : queue) {
                ParticleUtils.setParticleAlpha(particle, 1);
            }
        });
    }

    private static synchronized void hideAll() {
        Map<ParticleTextureSheet, Queue<Particle>> particles = getParticles();

        // For every kind of particle
        particles.forEach((particleTextureSheet, queue) -> {
            // For every single particle
            for (Particle particle : queue) {
                ParticleUtils.setParticleAlpha(particle, 0);
            }
        });
    }

    private static synchronized void killAll() {
        Map<ParticleTextureSheet, Queue<Particle>> particles = getParticles();

        // For every kind of particle
        particles.forEach((particleTextureSheet, queue) -> {
            // For every single particle
            for (Particle particle : queue) {
                // Kill that particle :(
                particle.markDead();
            }
        });
    }

    public static ScriptEngine getScriptHost() {
        if (host != null) {
            return host;
        }

        ScriptEngineManager factory = new ScriptEngineManager();
        host = factory.getEngineByName("JavaScript");
        if (host == null) {
            throw new AssertionError(
                    "Extreme Particles：您使用了需要用到Nashorn Javascript引擎的功能，但您的Java版本（≥15）并不具备该引擎。"
            );
        }

        try {  // Remove the latency at the first command call
            host.eval("0");
        } catch (ScriptException e) {
            throw new AssertionError(
                    "Extreme Particles：您使用了需要用到Nashorn Javascript引擎的功能，但您的Java版本（≥15）并不具备该引擎。"
            );
        }

        return host;
    }

    private static int about(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText(
                String.format(
                        "§e§lExtreme Particles§r v%d.%d.%d 是§9§nkworker§r制作的的自由软件。遵循GPLv3协议。", MAJOR_VERSION, MINOR_VERSION, REVISION
                )
        ), false);
        return 1;
    }

    @Override
    public void onInitializeClient() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("exp")
                    .then(CommandManager.literal("about").executes(Main::about))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("primitive")
                    .then(CommandManager.argument("particle", particle()).executes(context -> {
                        constructParticle(getParticle(context, "particle"), context.getSource().getPosition(), Vec3d.ZERO, null, -1, -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
                        return 1;
                    }).then(CommandManager.argument("position", vec3()).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), Vec3d.ZERO, null, -1, -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
                        return 1;
                    }).then(CommandManager.argument("delta", vec3(false)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), null, -1, -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
                        return 1;
                    }).then(CommandManager.argument("color", vec3(false)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), getVec3(context, "color"), -1, -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
                        return 1;
                    }).then(CommandManager.argument("alpha", floatArg(0)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), getVec3(context, "color"), getFloat(context, "alpha"), -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
                        return 1;
                    }).then(CommandManager.argument("life", integer(0)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), getVec3(context, "color"), getFloat(context, "alpha"), getInteger(context, "life"), -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
                        return 1;
                    }).then(CommandManager.argument("scale", floatArg(0)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), getVec3(context, "color"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"));
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
                        return 1;
                    })))))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("functional").then(CommandManager.argument("particle", particle())
                    .then(CommandManager.argument("func", string()).executes(context -> {
                        RendererManager.INSTANCE.call(Functional.INSTANCE, new Functional.FunctionalContext(getParticle(context, "particle"), getString(context, "func"), context.getSource().getPosition(), 1e3, 100), context);
                        return 1;
                    }).then(CommandManager.argument("origin", vec3()).executes(context -> {
                        RendererManager.INSTANCE.call(Functional.INSTANCE, new Functional.FunctionalContext(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), 1e3, 100), context);
                        return 1;
                    }).then(CommandManager.argument("time", doubleArg(0)).executes(context -> {
                        RendererManager.INSTANCE.call(Functional.INSTANCE, new Functional.FunctionalContext(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), getDouble(context, "time"), 100), context);
                        return 1;
                    }).then(CommandManager.argument("count", integer(1)).executes(context -> {
                        RendererManager.INSTANCE.call(Functional.INSTANCE, new Functional.FunctionalContext(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), getDouble(context, "time"), getInteger(context, "count")), context);
                        return 1;
                    })))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("renderImg").then(CommandManager.argument("particle", particle())
                    .then(CommandManager.argument("path", string()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), context.getSource().getPosition(), Vec3d.ZERO, null, false, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("origin", vec3()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), Vec3d.ZERO, null, false, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("delta", vec3(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), null, false, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("color", vec3(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), false, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("mono", bool()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("size", new Vec2ArgumentType(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), 1, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("type", integer(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("alpha", floatArg(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("life", integer(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("scale", floatArg(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), null), context);
                        return 1;
                    }).then(CommandManager.argument("group", string()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderImg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), getString(context, "group")), context);
                        return 1;
                    }))))))))))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("renderSvg").then(CommandManager.argument("particle", particle())
                    .then(CommandManager.argument("path", string()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), context.getSource().getPosition(), Vec3d.ZERO, null, false, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("origin", vec3()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), Vec3d.ZERO, null, false, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("delta", vec3(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), null, false, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("color", vec3(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), false, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("mono", bool()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("size", new Vec2ArgumentType(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), 1, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("type", integer(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("alpha", floatArg(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("life", integer(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("scale", floatArg(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), null), context);
                        return 1;
                    }).then(CommandManager.argument("group", string()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderSvg.INSTANCE, new RenderImg.ImgRenderContext(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), getString(context, "group")), context);
                        return 1;
                    }))))))))))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("renderTxt").then(CommandManager.argument("particle", particle())
                    .then(CommandManager.argument("text", string()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), context.getSource().getPosition(), Vec3d.ZERO, new Vec3d(255, 255, 255), null, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("origin", vec3()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), Vec3d.ZERO, new Vec3d(255, 255, 255), null, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("delta", vec3(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), new Vec3d(255, 255, 255), null, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("color", vec3(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), null, Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("font", string()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), Vec2f.ZERO, RenderImg.XY, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("size", new Vec2ArgumentType(false)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), 1, 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("type", integer(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), 1, -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("alpha", floatArg(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), -1, 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("life", integer(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), 1, null), context);
                        return 1;
                    }).then(CommandManager.argument("scale", floatArg(0)).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), null), context);
                        return 1;
                    }).then(CommandManager.argument("group", string()).executes(context -> {
                        RendererManager.INSTANCE.call(RenderTxt.INSTANCE, new RenderTxt.TxtRenderContext(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), getString(context, "group")), context);
                        return 1;
                    }))))))))))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("animation").then(CommandManager.literal("withdraw")
                    .then(CommandManager.argument("id", string()).executes(context -> {
                        try {
                            AnimationMgr.INSTANCE.withdraw(getString(context, "id"));
                        } catch (Exception e) {
                            context.getSource().sendError(Text.of(e.getMessage()));
                            return 1;
                        }
                        context.getSource().sendFeedback(Text.of("注销了一组动画。"), true);
                        return 1;
                    }))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("animation").then(CommandManager.literal("register")
                    .then(CommandManager.argument("id", string())
                            .then(CommandManager.argument("func", string()).executes(context -> {
                                catchFeedback(() -> AnimationMgr.INSTANCE.register(getString(context, "id"),
                                        getString(context, "func"),
                                        1000,
                                        null), "注册了一组动画。", context);

                                return 1;
                            }).then(CommandManager.argument("time", doubleArg(0)).executes(context -> {
                                catchFeedback(() -> AnimationMgr.INSTANCE.register(getString(context, "id"),
                                        getString(context, "func"),
                                        getDouble(context, "time"),
                                        null), "注册了一组动画。", context);
                                return 1;
                            }).then(CommandManager.argument("then", string()).executes(context -> {
                                catchFeedback(() -> AnimationMgr.INSTANCE.register(getString(context, "id"),
                                        getString(context, "func"),
                                        getDouble(context, "time"),
                                        getString(context, "then")), "注册了一组动画。", context);
                                return 1;
                            })))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("animation").then(CommandManager.literal("apply")
                    .then(CommandManager.argument("id", string())
                            .then(CommandManager.argument("group", string()).executes(context -> {
                                catchFeedback(() ->
                                        AnimationMgr.INSTANCE.apply(
                                                getString(context, "id"),
                                                ParticleGroupMgr.INSTANCE.get(
                                                        getString(context, "group")
                                                ),
                                                false, false
                                        ), "执行了一组动画。", context);
                                return 1;
                            }).then(CommandManager.argument("withdrawAfter", bool()).executes(context -> {
                                catchFeedback(() ->
                                        AnimationMgr.INSTANCE.apply(
                                                getString(context, "id"),
                                                ParticleGroupMgr.INSTANCE.get(
                                                        getString(context, "group")
                                                ),
                                                getBool(context, "withdrawAfter"), false
                                        ), "执行了一组动画。", context);
                                return 1;
                            }).then(CommandManager.argument("killAllAfter", bool()).executes(context -> {
                                catchFeedback(() ->
                                        AnimationMgr.INSTANCE.apply(
                                                getString(context, "id"),
                                                ParticleGroupMgr.INSTANCE.get(
                                                        getString(context, "group")
                                                ),
                                                getBool(context, "withdrawAfter"),
                                                getBool(context, "withdrawAfter")
                                        ), "执行了一组动画。", context);
                                return 1;
                            })))))))
            );


            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("configure").then(CommandManager.literal("pauseParticleConstruction").then(CommandManager.argument("value", bool()).executes(context -> {
                disabled = getBool(context, "value");
                context.getSource().sendFeedback(new LiteralText("粒子启用状态已更新。"), false);
                return 1;
            })))));
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("configure").then(CommandManager.literal("asyncParticleConstruction").then(CommandManager.argument("value", bool()).executes(context -> {
                isAsync = getBool(context, "value");
                context.getSource().sendFeedback(new LiteralText("粒子异步状态已更新。"), false);
                return 1;
            })))));
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("configure").then(CommandManager.literal("particleLifeAnimationFix").then(CommandManager.argument("value", bool()).executes(context -> {
                fixLife = getBool(context, "value");
                context.getSource().sendFeedback(new LiteralText("寿命修复状态已更新。"), false);
                return 1;
            })))));
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("configure").then(CommandManager.literal("globalAnimationFrameRate").then(CommandManager.argument("value", doubleArg(1)).executes(context -> {
                frameRate = getDouble(context, "value");
                context.getSource().sendFeedback(new LiteralText("目标帧率已更新。"), false);
                return 1;
            })))));
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("utilities").then(CommandManager.literal("showAll").executes(context -> {
                showAll();
                context.getSource().sendFeedback(new LiteralText("显示了所有粒子。"), false);
                return 1;
            }))));
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("utilities").then(CommandManager.literal("hideAll").executes(context -> {
                hideAll();
                context.getSource().sendFeedback(new LiteralText("隐藏了所有粒子。"), false);
                return 1;
            }))));
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("utilities").then(CommandManager.literal("killAll").executes(context -> {
                killAll();
                context.getSource().sendFeedback(new LiteralText("杀死了所有粒子。"), false);
                return 1;
            }))));
        });
    }

    private static class ParticleDaemon {
        private final List<ParticleContext> contexts = new ArrayList<>();

        public ParticleDaemon() {
            Thread thread = new Thread(() -> {
                while (true) {
                    tick();

                    try {
                        synchronized (frameSignal) {
                            frameSignal.wait();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        public synchronized void offer(Particle particle) {
            contexts.add(new ParticleContext(particle));
        }

        private synchronized void tick() {
            for (Iterator<ParticleContext> iterator = contexts.iterator(); iterator.hasNext(); ) {
                ParticleContext context = iterator.next();

                context.tickLifeLeft();

                if (context.isDying()) {
                    double p = context.getLifeLeft();
                    ParticleUtils.setParticleAlpha(context.particle, (float) (context.originalAlpha * p));
                    ParticleUtils.setParticleScale(context.particle, (float) (context.originalScale * p));
                }

                if (context.isDead()) {
                    iterator.remove();
                    context.particle.markDead();
                }
            }
        }
    }

    private static class ParticleContext {
        public final Particle particle;
        public final float originalAlpha;
        public final float originalScale;
        private double lifeLeft;

        public ParticleContext(Particle particle) {
            this.particle = particle;
            originalAlpha = ParticleUtils.getParticleAlpha(particle);
            originalScale = ParticleUtils.getParticleScale(particle);
            lifeLeft = ParticleUtils.getParticleLife(particle) / 20d;
            ParticleUtils.setParticleLife(particle, Integer.MAX_VALUE);
        }

        public double getLifeLeft() {
            return lifeLeft;
        }

        public void tickLifeLeft() {
            lifeLeft -= 1 / frameRate;
        }

        public boolean isDying() {
            return lifeLeft < 1;
        }

        public boolean isDead() {
            return lifeLeft < 0 || !particle.isAlive();
        }
    }
}
