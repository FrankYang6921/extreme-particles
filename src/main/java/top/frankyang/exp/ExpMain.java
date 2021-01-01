package top.frankyang.exp;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.util.Objects;

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
import static net.minecraft.command.argument.Vec3ArgumentType.getVec3;
import static net.minecraft.command.argument.Vec3ArgumentType.vec3;
import static top.frankyang.exp.ExpFunctional.functionalPattern;


public class ExpMain implements ModInitializer {
    private static final int MAJOR_VERSION = 0;
    private static final int MINOR_VERSION = 1;
    private static final int REVISION = 0;
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static void sendFunctionalFeedback(String ret, CommandContext<ServerCommandSource> context) {
        if (ret == null) {
            context.getSource().sendFeedback(new LiteralText("按照函数批量构造了粒子。"), false);
        } else {
            context.getSource().sendError(new LiteralText(ret));
        }
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
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("single")
                    .then(CommandManager.argument("particle", particle()).executes(context -> {
                        constructParticle(getParticle(context, "particle"), context.getSource().getPosition(), Vec3d.ZERO, null, -1, -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子。"), false);
                        return 1;
                    }).then(CommandManager.argument("position", vec3()).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), Vec3d.ZERO, null, -1, -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子。"), false);
                        return 1;
                    }).then(CommandManager.argument("delta", vec3(false)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), null, -1, -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子。"), false);
                        return 1;
                    }).then(CommandManager.argument("color", vec3(false)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), getVec3(context, "color"), -1, -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子。"), false);
                        return 1;
                    }).then(CommandManager.argument("alpha", floatArg(0)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), getVec3(context, "color"), getFloat(context, "alpha"), -1, -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子。"), false);
                        return 1;
                    }).then(CommandManager.argument("life", integer(0)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), getVec3(context, "color"), getFloat(context, "alpha"), getInteger(context, "life"), -1);
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子。"), false);
                        return 1;
                    }).then(CommandManager.argument("scale", floatArg(0)).executes(context -> {
                        constructParticle(getParticle(context, "particle"), getVec3(context, "position"), getVec3(context, "delta"), getVec3(context, "color"), getFloat(context, "alpha"), getInteger(context, "life"), getFloat(context, "scale"));
                        context.getSource().sendFeedback(new LiteralText("构造了1个粒子。"), false);
                        return 1;
                    })))))))))
            );
            dispatcher.register(CommandManager.literal("exp").then(CommandManager.literal("animate").then(CommandManager.argument("particle", particle())
                    .then(CommandManager.argument("func", string()).executes(context -> {
                        String ret = functionalPattern(getParticle(context, "particle"), getString(context, "func"), context.getSource().getPosition(), 1e3, 100);
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("origin", vec3()).executes(context -> {
                        String ret = functionalPattern(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), 1e3, 100);
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("time", doubleArg(0)).executes(context -> {
                        String ret = functionalPattern(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), getDouble(context, "time"), 100);
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    }).then(CommandManager.argument("count", integer(1)).executes(context -> {
                        String ret = functionalPattern(getParticle(context, "particle"), getString(context, "func"), getVec3(context, "origin"), getDouble(context, "time"), getInteger(context, "count"));
                        sendFunctionalFeedback(ret, context);
                        return 1;
                    })))))))
            );
        });
    }

    static void setAlpha(Particle particle, float alpha) {
        Class<?> clazz = particle.getClass();
        try {
            Field field = clazz.getField("colorAlpha");
            field.setAccessible(true);
            field.set(particle, alpha);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static void constructParticle(ParticleEffect effect, Vec3d position, Vec3d delta, Vec3d color, float alpha, int life, float scale) {
        double x = position.x;
        double y = position.y;
        double z = position.z;
        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;

        Particle particle = Objects.requireNonNull(client.particleManager.addParticle(effect, x, y, z, dx, dy, dz));

        if (color != null) {
            particle.setColor((float) color.x / 255, (float) color.y / 255, (float) color.z / 255);
        }
        if (alpha >= 0) {
            setAlpha(particle, alpha);
        }
        if (life >= 0) {
            particle.setMaxAge(life);
        }
        if (scale >= 0) {
            particle.scale(scale);
        }
    }
}
