package com.tick_ins.tick;

import oshi.util.tuples.Pair;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;

public final class TickThread {
    private static final ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "easyplacefix-tick-thread");
                t.setDaemon(true);
                return t;
            });
    private static final AtomicLong TASK_EPOCH = new AtomicLong();
    private static volatile boolean clientStopping = false;
    public static volatile boolean notChangPlayerLook = false;
    public static volatile float yawLock = 0.0F;
    public static volatile float pitchLock = 0.0F;

    private TickThread() {
    }

    public static void addTask(RunnableWithLast first, RunnableWithLast second) {
        if (clientStopping) {
            return;
        }
        Pair<Float, Float> yawAndPitch = first == null ? null : first.yawAndPitch();
        applyLookLock(yawAndPitch);

        runNow(first == null ? null : first.task());
        runAfterTick(() -> {
            if (second != null) {
                runNow(() -> {
                    second.task().run();
                    clearLookLock();
                });
            } else {
                runNow(TickThread::clearLookLock);
            }
        }, 1);
    }

    public static void addLastTask(RunnableWithLast task) {
        if (task == null || clientStopping) {
            return;
        }

        Pair<Float, Float> yawAndPitch = task.yawAndPitch();
        applyLookLock(yawAndPitch);
        runNow(task.task());
        runAfterTick(() -> {
            runNow(() -> {
                task.cache().run();
                clearLookLock();
            });
        }, 1);
    }

    public static void addCountDownTask(RunnableWithCountDown task) {
        if (task == null || clientStopping) {
            return;
        }
        runAfterTick(task.task(), task.count());
    }

    private static void runNow(Runnable runnable) {
        if (runnable == null || clientStopping) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.level == null) {
            return;
        }
        try {
            client.execute(() -> {
                if (!clientStopping) {
                    runnable.run();
                }
            });
        } catch (RejectedExecutionException ignored) {
        }
    }

    private static void runAfterTick(Runnable runnable, int ticks) {
        if (runnable == null || clientStopping) {
            return;
        }
        long delayMs = Math.max(0, ticks) * 50L;
        long epoch = TASK_EPOCH.get();
        EXECUTOR.schedule(() -> {
            if (!clientStopping && epoch == TASK_EPOCH.get()) {
                runNow(runnable);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private static void applyLookLock(Pair<Float, Float> yawAndPitch) {
        if (yawAndPitch == null) {
            return;
        }

        yawLock = yawAndPitch.getA();
        pitchLock = yawAndPitch.getB();
        notChangPlayerLook = true;
    }

    public static void clearLookLock() {
        notChangPlayerLook = false;
    }

    public static void onClientDisconnected() {
        TASK_EPOCH.incrementAndGet();
        clearLookLock();
        clientStopping = false;
    }

    public static void onClientShutdown() {
        TASK_EPOCH.incrementAndGet();
        clearLookLock();
        clientStopping = true;
    }
}
