package top.frankyang.exp.internal;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import top.frankyang.exp.Main;

public final class RendererManager {
    private RendererManager() {

    }

    public static void call(Renderer renderer, RendererContext context, CommandContext<ServerCommandSource> source) {
        if (Main.useAsync) {
            Main.pool.submit(() -> {
                renderer.renderPattern(context);
                source.getSource().sendFeedback(
                        Text.of(context.getMessage()), true
                );
            });
        } else {
            renderer.renderPattern(context);
            source.getSource().sendFeedback(
                    Text.of(context.getMessage()), true
            );
        }
    }
}
