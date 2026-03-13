package me.will0mane.software.pack.api.arch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThreadScheduler implements Scheduler {

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @Override
    public CompletableFuture<Void> after(TimeUnit unit, long duration) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        service.schedule(() -> future.complete(null), duration, unit);
        return future;
    }

    @Override
    public ScheduledFuture<?> fixedRate(Runnable task, TimeUnit unit, long delay, long period) {
        return service.scheduleAtFixedRate(task, delay, period, unit);
    }

    @Override
    public CompletableFuture<Void> now() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        service.execute(() -> future.complete(null));
        return future;
    }

    @Override
    public void shutdown() {
        service.shutdownNow();
    }
}
