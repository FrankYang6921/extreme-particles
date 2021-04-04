package top.frankyang.exp.internal;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public abstract class RendererContext {
    private ServerCommandSource source;

    public void sendFeedback(String s) {
        source.sendFeedback(Text.of(s != null ? s : getSuccessfulFeedback()), true);
    }

    public void catchFeedback(Runnable r) {
        try {
            r.run();
            sendFeedback(null);
        } catch (Exception e) {
            sendFeedback(e.getMessage());
        }
    }

    public void setSource(ServerCommandSource source) {
        this.source = source;
    }

    public abstract String getSuccessfulFeedback();
}
