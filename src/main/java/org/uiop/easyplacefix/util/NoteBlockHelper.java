package org.uiop.easyplacefix.util;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;

/**
 * Tunes note blocks to their schematic note by sending right-click interactions.
 * <p>
 * All positions being tuned share a single pump that emits at most one
 * interaction every {@link #TUNE_INTERVAL_TICKS} ticks, round-robin. Without this
 * a wall of note blocks would each spawn an independent per-tick clicker and the
 * combined packet rate would trip server "timer" anti-cheat.
 */
public final class NoteBlockHelper {
    public static final int MAX_NOTE = 24;
    public static final int NOTE_COUNT = 25;
    private static final int MAX_ATTEMPTS = 30;
    private static final int TUNE_INTERVAL_TICKS = 2;

    private static final Set<BlockPos> TUNING_POSITIONS = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedDeque<BlockPos> QUEUE = new ConcurrentLinkedDeque<>();
    private static final ConcurrentHashMap<BlockPos, Integer> TARGET_NOTE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<BlockPos, Integer> ATTEMPTS_LEFT = new ConcurrentHashMap<>();
    private static final AtomicBoolean PUMP_SCHEDULED = new AtomicBoolean(false);

    private NoteBlockHelper() {
    }

    public static boolean isTuning(BlockPos pos) {
        return TUNING_POSITIONS.contains(pos);
    }

    public static int calculateClicks(int currentNote, int targetNote) {
        return (targetNote - currentNote + NOTE_COUNT) % NOTE_COUNT;
    }

    public static void tune(Minecraft mc, BlockPos pos, int targetNote) {
        if (targetNote < 0 || targetNote > MAX_NOTE) {
            return;
        }

        if (mc.level != null) {
            BlockState state = mc.level.getBlockState(pos);
            if (state.getBlock() instanceof NoteBlock && state.getValue(BlockStateProperties.NOTE) == targetNote) {
                return;
            }
        }

        BlockPos key = pos.immutable();
        TARGET_NOTE.put(key, targetNote);
        if (TUNING_POSITIONS.add(key)) {
            ATTEMPTS_LEFT.put(key, MAX_ATTEMPTS);
            QUEUE.addLast(key);
        }
        ensurePump(mc);
    }

    private static void ensurePump(Minecraft mc) {
        if (QUEUE.isEmpty()) {
            return;
        }
        if (PUMP_SCHEDULED.compareAndSet(false, true)) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                    .setCount(TUNE_INTERVAL_TICKS)
                    .build(() -> pump(mc)));
        }
    }

    private static void pump(Minecraft mc) {
        PUMP_SCHEDULED.set(false);
        try {
            if (mc.player == null || mc.level == null || mc.gameMode == null) {
                clear();
                return;
            }

            BlockPos pos;
            while ((pos = QUEUE.pollFirst()) != null) {
                if (!TUNING_POSITIONS.contains(pos)) {
                    continue;
                }

                BlockState state = mc.level.getBlockState(pos);
                Integer target = TARGET_NOTE.get(pos);
                if (target == null || !(state.getBlock() instanceof NoteBlock)) {
                    finish(pos);
                    continue;
                }

                if (state.getValue(BlockStateProperties.NOTE) == target) {
                    finish(pos);
                    continue;
                }

                int attempts = ATTEMPTS_LEFT.getOrDefault(pos, 0);
                if (attempts <= 0) {
                    finish(pos);
                    continue;
                }
                ATTEMPTS_LEFT.put(pos, attempts - 1);

                RelativeBlockHitResult hitResult = new RelativeBlockHitResult(
                        new Vec3(0.5, 0.5, 0.5),
                        Direction.UP,
                        pos,
                        false
                );
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
                mc.player.swing(InteractionHand.MAIN_HAND);

                QUEUE.addLast(pos); // still needs more clicks; back of the line
                break;              // exactly one interaction per pump
            }
        } catch (Exception error) {
            LOGGER.error("Error during NoteBlock tuning", error);
            clear();
            return;
        }

        if (!QUEUE.isEmpty()) {
            ensurePump(mc);
        }
    }

    private static void finish(BlockPos pos) {
        TUNING_POSITIONS.remove(pos);
        TARGET_NOTE.remove(pos);
        ATTEMPTS_LEFT.remove(pos);
    }

    public static void clear() {
        TUNING_POSITIONS.clear();
        QUEUE.clear();
        TARGET_NOTE.clear();
        ATTEMPTS_LEFT.clear();
        PUMP_SCHEDULED.set(false);
    }
}
