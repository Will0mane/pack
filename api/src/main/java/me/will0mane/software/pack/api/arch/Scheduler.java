package me.will0mane.software.pack.api.arch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface Scheduler {

    CompletableFuture<Void> after(TimeUnit unit, long duration);

    CompletableFuture<Void> fixedRate(TimeUnit unit, long delay, long period);

    CompletableFuture<Void> now();

}
