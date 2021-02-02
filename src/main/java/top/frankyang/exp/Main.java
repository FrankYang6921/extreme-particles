package top.frankyang.exp;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
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
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import top.frankyang.exp.anime.AnimationMgr;
import top.frankyang.exp.render.Functional;
import top.frankyang.exp.render.ImgRender;
import top.frankyang.exp.render.TxtRender;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

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


public final class Main implements ModInitializer {
    private static final int MAJOR_VERSION = 0;
    private static final int MINOR_VERSION = 3;
    private static final int REVISION = 0;

    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static boolean disabled = false;
    public static boolean useAsync = false;
    public static double frameRate = 30.30303030303030d;

    private static final Object particleAccessLock = new Object();

    private static ScriptEngine host;

    private static void sendFunctionalFeedback(String ret, CommandContext<ServerCommandSource> context) {
        if (ret == null) {
            context.getSource().sendFeedback(new LiteralText("按照函数批量构造了粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
        } else {
            context.getSource().sendError(new LiteralText(ret));
        }
    }

    private static void sendImgRenderFeedback(String ret, CommandContext<ServerCommandSource> context) {
        if (ret == null) {
            context.getSource().sendFeedback(new LiteralText("按照位图批量构造了粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
        } else {
            context.getSource().sendError(new LiteralText(ret));
        }
    }

    private static void sendTxtRenderFeedback(String ret, CommandContext<ServerCommandSource> context) {
        if (ret == null) {
            context.getSource().sendFeedback(new LiteralText("按照文本批量构造了粒子" + (disabled ? "，但是并未被显示。" : "。")), false);
        } else {
            context.getSource().sendError(new LiteralText(ret));
        }
    }

    private static void sendAnimationFeedback(String ret, CommandContext<ServerCommandSource> context) {
        if (ret == null) {
            context.getSource().sendFeedback(new LiteralText("注册了一组动画。"), false);
        } else {
            context.getSource().sendError(new LiteralText(ret));
        }
    }

    private static void sendWithdrawFeedback(String ret, CommandContext<ServerCommandSource> context) {
        if (ret == null) {
            context.getSource().sendFeedback(new LiteralText("注销了一组动画。"), false);
        } else {
            context.getSource().sendError(new LiteralText(ret));
        }
    }

    private static Particle addSyncParticle(
            ParticleEffect effect,
            double x,
            double y,
            double z,
            double dx,
            double dy,
            double dz) {
        synchronized (particleAccessLock) {
            return Objects.requireNonNull(client.particleManager.addParticle(effect, x, y, z, dx, dy, dz));
        }
    }

    private static Particle addAsyncParticle(
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

        Particle particle;

        if (useAsync) {
            particle = addAsyncParticle(effect, x, y, z, dx, dy, dz);
        } else {
            particle = addSyncParticle(effect, x, y, z, dx, dy, dz);
        }


        if (color != null) {
            particle.setColor(
                    (float) color.x / 255.0f,
                    (float) color.y / 255.0f,
                    (float) color.z / 255.0f
            );
        }
        if (alpha >= 0) {
            Util.setParticleAlpha(particle, alpha);
        }
        if (life >= 0) {
            Util.setParticleLife(particle, life);
        }
        if (scale >= 0) {
            Util.setParticleScale(particle, scale);
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
                Util.setParticleAlpha(particle, 1);
            }
        });
    }

    private static synchronized void hideAll() {
        Map<ParticleTextureSheet, Queue<Particle>> particles = getParticles();

        // For every kind of particle
        particles.forEach((particleTextureSheet, queue) -> {
            // For every single particle
            for (Particle particle : queue) {
                Util.setParticleAlpha(particle, 0);
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
            throw new RuntimeException(
                    "OpenJDK above 15 is not allowed to use."
            );
        }

        try {  // Remove the latency at the first command call
            host.eval("0");
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        return host;
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("exp")
                    .then(CommandManager.literal("about").executes(context -> {
                        context.getSource().sendFeedback(new LiteralText(
                                String.format(
                                        "§e§lExtreme Particles§r v%d.%d.%d 是§9§nkworker§r制作的的自由软件。遵循GPLv3协议。", MAJOR_VERSION, MINOR_VERSION, REVISION
                                )
                        ), false);
                        return 1;
                    }))
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
                        String ret = Functional.renderPattern(getParticle(context, "particle"), getString(context, "func"), context.getSource().getPosition(), 1e3, 100, null);
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("origin", vec3()).executes(context -> {
                        String ret = Functional.renderPattern(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), 1e3, 100, null);
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("time", doubleArg(0)).executes(context -> {
                        String ret = Functional.renderPattern(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), getDouble(context, "time"), 100, null);
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("count", integer(1)).executes(context -> {
                        String ret = Functional.renderPattern(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), getDouble(context, "time"), getInteger(context, "count"), null);
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("animate", string()).executes(context -> {
                        String ret = Functional.renderPattern(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), getDouble(context, "time"), getInteger(context, "count"), getString(context, "animate"));
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    }))))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("imgRender").then(CommandManager.argument("particle", particle())
                    .then(CommandManager.argument("path", string()).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), context.getSource().getPosition(), Vec3d.ZERO, null, false, Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("origin", vec3()).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), Vec3d.ZERO, null, false, Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("delta", vec3(false)).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), null, false, Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("color", vec3(false)).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), false, Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("mono", bool()).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("size", new Vec2ArgumentType(false)).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), 1, 1, -1, 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("type", integer(0)).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), 1, -1, 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("alpha", floatArg(0)).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), -1, 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("life", integer(0)).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), 1, null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("scale", floatArg(0)).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), null);
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("animate", string()).executes(context -> {
                        String ret = ImgRender.renderPattern(getParticle(context, "particle"), getString(context, "path"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getBool(context, "mono"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), getString(context, "animate"));
                        sendImgRenderFeedback(ret, context);
                        return 1;
                    }))))))))))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("txtRender").then(CommandManager.argument("particle", particle())
                    .then(CommandManager.argument("text", string()).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), context.getSource().getPosition(), Vec3d.ZERO, new Vec3d(255, 255, 255), null, Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("origin", vec3()).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), Vec3d.ZERO, new Vec3d(255, 255, 255), null, Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("delta", vec3(false)).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), new Vec3d(255, 255, 255), null, Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("color", vec3(false)).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), null, Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("font", string()).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), Vec2f.ZERO, 1, 1, -1, 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("size", new Vec2ArgumentType(false)).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), 1, 1, -1, 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("type", integer(0)).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), 1, -1, 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("alpha", floatArg(0)).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), -1, 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("life", integer(0)).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), 1, null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("scale", floatArg(0)).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), null);
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("animate", string()).executes(context -> {
                        String ret = TxtRender.renderPattern(getParticle(context, "particle"), getString(context, "text"), getVec3(context, "origin"), getVec3(context, "delta"), getVec3(context, "color"), getString(context, "font"), getVec2(context, "size"), getInteger(context, "type"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"), getString(context, "animate"));
                        sendTxtRenderFeedback(ret, context);
                        return 1;
                    }))))))))))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("animation")
                    .then(CommandManager.argument("id", string()).executes(context -> {
                        String ret = AnimationMgr.withdraw(getString(context, "id"));
                        sendWithdrawFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("func", string()).executes(context -> {
                        String ret = AnimationMgr.register(getString(context, "id"), getString(context, "func"), 1000, null);
                        sendAnimationFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("time", doubleArg(0)).executes(context -> {
                        String ret = AnimationMgr.register(getString(context, "id"), getString(context, "func"), getDouble(context, "time"), null);
                        sendAnimationFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("then", string()).executes(context -> {
                        String ret = AnimationMgr.register(getString(context, "id"), getString(context, "func"), getDouble(context, "time"), getString(context, "then"));
                        sendAnimationFeedback(ret, context);
                        return 1;
                    }))))))
            );

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("configure").then(CommandManager.literal("disabled").then(CommandManager.argument("value", bool()).executes(context -> {
                        disabled = getBool(context, "value");
                        context.getSource().sendFeedback(new LiteralText("粒子启用状态已更新。"), false);
                        return 1;
                    })))
            ));
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("configure").then(CommandManager.literal("useAsync").then(CommandManager.argument("value", bool()).executes(context -> {
                        useAsync = getBool(context, "value");
                        context.getSource().sendFeedback(new LiteralText("粒子异步状态已更新。"), false);
                        return 1;
                    })))
            ));

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("utilities").then(CommandManager.literal("showAll").executes(context -> {
                        showAll();
                        context.getSource().sendFeedback(new LiteralText("显示了所有粒子。"), false);
                        return 1;
                    }))
            ));

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("utilities").then(CommandManager.literal("hideAll").executes(context -> {
                        hideAll();
                        context.getSource().sendFeedback(new LiteralText("隐藏了所有粒子。"), false);
                        return 1;
                    }))
            ));

            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("utilities").then(CommandManager.literal("killAll").executes(context -> {
                        killAll();
                        context.getSource().sendFeedback(new LiteralText("杀死了所有粒子。"), false);
                        return 1;
                    }))
            ));
        });
    }
}
