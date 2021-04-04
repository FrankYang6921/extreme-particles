package top.frankyang.exp.internal;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import top.frankyang.exp.Main;

public final class RendererManager {
    private RendererManager() {

    }

    public static void call(Renderer renderer, RendererContext context1, CommandContext<ServerCommandSource> context2) {
        context1.setSource(context2.getSource());

        if (Main.isAsync) {
            Main.pool.submit(() ->
                    renderer.renderPattern(context1)
            );
        } else {
            renderer.renderPattern(context1);
        }
    }
}
