package com.tick_ins.tick;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;

public final class TickThread {
    private static final int MAX_PENDING_TASKS = 512;
    private static final long LOOK_LOCK_MAX_MS = 1500L;

    private static final Object LOCK = new Object();
    private static final List<ScheduledTask> PENDING = new ArrayList<>();
    private static long currentTick = 0L;

    private static volatile boolean clientStopping = false;
    public static volatile boolean notChangPlayerLook = false;
    private static volatile long lookLockExpiryMs = 0L;
    public static volatile float yawLock = 0.0F;
    public static volatile float pitchLock = 0.0F;

    private record ScheduledTask(long dueTick, Runnable runnable) {
    }

    private TickThread() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(TickThread::onClientTick);
    }

    public static long currentTick() {
        synchronized (LOCK) {
            return currentTick;
        }
    }

    public static int pendingTaskCount() {
        synchronized (LOCK) {
            return PENDING.size();
        }
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
                try {
                    second.task().run();
                } finally {
                    clearLookLock();
                }
            } else {
                clearLookLock();
            }
        }, 1);
    }

    public static void addLastTask(RunnableWithLast task) {
        if (task == null || clientStopping) {
            return;
        }

        applyLookLock(task.yawAndPitch());
        runNow(task.task());
        runAfterTick(() -> {
            try {
                task.cache().run();
            } finally {
                clearLookLock();
            }
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
        } catch (RejectedExecutionException error) {
            LOGGER.debug("Minecraft rejected an EasyPlaceFix task during shutdown", error);
        }
    }

    private static void runAfterTick(Runnable runnable, int ticks) {
        if (runnable == null || clientStopping) {
            return;
        }
        if (ticks <= 0) {
            runNow(runnable);
            return;
        }
        synchronized (LOCK) {
            if (PENDING.size() >= MAX_PENDING_TASKS) {
                LOGGER.warn("Discarding EasyPlaceFix delayed task because the bounded queue is full ({})",
                        MAX_PENDING_TASKS);
                return;
            }
            PENDING.add(new ScheduledTask(currentTick + ticks, runnable));
        }
    }

    private static void onClientTick(Minecraft client) {
        List<Runnable> due = new ArrayList<>();
        synchronized (LOCK) {
            currentTick++;
            Iterator<ScheduledTask> iterator = PENDING.iterator();
            while (iterator.hasNext()) {
                ScheduledTask task = iterator.next();
                if (task.dueTick() <= currentTick) {
                    iterator.remove();
                    due.add(task.runnable());
                }
            }
        }
        if (due.isEmpty() || clientStopping || client.player == null || client.level == null) {
            return;
        }
        for (Runnable runnable : due) {
            try {
                runnable.run();
            } catch (RuntimeException error) {
                LOGGER.error("EasyPlaceFix delayed task failed", error);
            }
        }
    }

    private static void applyLookLock(Pair<Float, Float> yawAndPitch) {
        if (yawAndPitch == null) {
            return;
        }

        yawLock = yawAndPitch.getA();
        pitchLock = yawAndPitch.getB();
        lookLockExpiryMs = System.currentTimeMillis() + LOOK_LOCK_MAX_MS;
        notChangPlayerLook = true;
    }

    public static boolean isLookLocked() {
        return notChangPlayerLook && System.currentTimeMillis() < lookLockExpiryMs;
    }

    public static void clearLookLock() {
        notChangPlayerLook = false;
        lookLockExpiryMs = 0L;
    }

    private static void clearPending() {
        synchronized (LOCK) {
            PENDING.clear();
        }
    }

    public static void onClientDisconnected() {
        clearPending();
        clearLookLock();
        clientStopping = false;
        org.uiop.easyplacefix.util.NoteBlockHelper.clear();
        org.uiop.easyplacefix.util.TerrainAutoReplace.clear();
    }

    public static void onClientShutdown() {
        clientStopping = true;
        clearPending();
        clearLookLock();
        org.uiop.easyplacefix.util.NoteBlockHelper.clear();
        org.uiop.easyplacefix.util.TerrainAutoReplace.clear();
    }
}
