package org.uiop.easyplacefix.util;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
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

import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;

public final class NoteBlockHelper {
    public static final int MAX_NOTE = 24;
    public static final int NOTE_COUNT = 25;
    private static final int MAX_ATTEMPTS = 30;
    private static final Set<BlockPos> TUNING_POSITIONS = ConcurrentHashMap.newKeySet();

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

        if (!TUNING_POSITIONS.add(pos)) {
            return;
        }

        scheduleNextTuneStep(mc, pos, targetNote, MAX_ATTEMPTS);
    }

    private static void scheduleNextTuneStep(Minecraft mc, BlockPos pos, int targetNote, int remainingAttempts) {
        TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(1).build(() -> {
            try {
                if (remainingAttempts <= 0) {
                    TUNING_POSITIONS.remove(pos);
                    return;
                }

                if (mc.player == null || mc.level == null) {
                    TUNING_POSITIONS.remove(pos);
                    return;
                }

                BlockState state = mc.level.getBlockState(pos);
                if (!(state.getBlock() instanceof NoteBlock)) {
                    TUNING_POSITIONS.remove(pos);
                    return;
                }

                int currentNote = state.getValue(BlockStateProperties.NOTE);
                if (currentNote == targetNote) {
                    TUNING_POSITIONS.remove(pos);
                    return;
                }

                MultiPlayerGameMode gameMode = mc.gameMode;
                if (gameMode == null) {
                    TUNING_POSITIONS.remove(pos);
                    return;
                }

                RelativeBlockHitResult hitResult = new RelativeBlockHitResult(
                        new Vec3(0.5, 0.5, 0.5),
                        Direction.UP,
                        pos,
                        false
                );

                gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
                mc.player.swing(InteractionHand.MAIN_HAND);

                scheduleNextTuneStep(mc, pos, targetNote, remainingAttempts - 1);
            } catch (Exception error) {
                LOGGER.error("Error during NoteBlock tuning at {}", pos, error);
                TUNING_POSITIONS.remove(pos);
            }
        }));
    }

    public static void clear() {
        TUNING_POSITIONS.clear();
    }
}
