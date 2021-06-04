package top.frankyang.exp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadUtils {
    public static final ExecutorService parallelPool = Executors.newCachedThreadPool();  // Better in performance
    public static final ExecutorService serialPool = Executors.newSingleThreadExecutor();  // Ensures execution order

    private ThreadUtils() {
    }
}
