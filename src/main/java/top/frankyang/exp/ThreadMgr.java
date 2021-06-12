package top.frankyang.exp;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadMgr {
    public static final ThreadMgr INSTANCE = new ThreadMgr();

    private final Object frameSignal = new Object();

    private final ExecutorService parallelPool = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });  // Better in performance, always works as a daemon.

    private final ExecutorService serialPool = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });  // Ensures execution order, always works as a daemon.

    private ThreadMgr() {
        getParallelPool().submit(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (frameSignal) {
                    frameSignal.notifyAll();
                }

                try {
                    //noinspection BusyWait
                    Thread.sleep(Math.round(1000 / Main.getGlobalAnimationFrameRate()));  // ~ 30 FPS
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
            }
        });
    }

    public ExecutorService getParallelPool() {
        return parallelPool;
    }

    public ExecutorService getSerialPool() {
        return serialPool;
    }

    public void catchFeedback(Runnable r, String successful, CommandContext<ServerCommandSource> context) {
        serialPool.submit(() -> {
            try {
                r.run();
                context.getSource().sendFeedback(Text.of(successful), true);
            } catch (Exception e) {
                e.printStackTrace();
                context.getSource().sendError(Text.of(e.getMessage()));
            }
        });
    }

    public void waitForFrame() {
        try {
            synchronized (frameSignal) {
                frameSignal.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
