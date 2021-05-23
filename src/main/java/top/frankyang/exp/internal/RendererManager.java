package top.frankyang.exp.internal;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import top.frankyang.exp.Main;

public final class RendererManager {
    public static final RendererManager INSTANCE = new RendererManager();

    private RendererManager() {
    }

    public void call(Renderer renderer, RendererContext context1, CommandContext<ServerCommandSource> context2) {
        context1.setSource(context2.getSource());

        if (Main.isAsync) {
            Main.pool.submit(() ->
                    renderer.renderContext(context1)
            );
        } else {
            renderer.renderContext(context1);
        }
    }
}