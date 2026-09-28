package org.uiop.easyplacefix.util;

import com.tick_ins.packet.Ping2Server;
import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;

public final class NoteBlockHelper {
    public static final int MAX_NOTE = 24;
    public static final int NOTE_COUNT = 25;
    private static final int MAX_CLICKS = 60;
    private static final int TUNE_INTERVAL_TICKS = 2;
    private static final long SERVER_ACK_GRACE_MS = 500L;

    private static final ConcurrentLinkedDeque<BlockPos> QUEUE = new ConcurrentLinkedDeque<>();
    private static final ConcurrentHashMap<BlockPos, TuneJob> JOBS = new ConcurrentHashMap<>();
    private static final AtomicBoolean PUMP_SCHEDULED = new AtomicBoolean(false);

    private static final class TuneJob {
        private int targetNote;
        private int baseNote = -1;
        private int clicksSent;
        private int confirmed;
        private int clicksLeft = MAX_CLICKS;
        private long lastProgressMs = System.currentTimeMillis();

        private TuneJob(int targetNote) {
            this.targetNote = targetNote;
        }

        private void rebase(int currentNote) {
            this.baseNote = currentNote;
            this.clicksSent = 0;
            this.confirmed = 0;
            this.lastProgressMs = System.currentTimeMillis();
        }
    }

    private NoteBlockHelper() {
    }

    public static boolean isTuning(BlockPos pos) {
        return JOBS.containsKey(pos);
    }

    public record Progress(int blocks, int clicksLeft, BlockPos next) {
    }

    public static Progress progress(Minecraft mc) {
        if (JOBS.isEmpty() || mc.level == null) {
            return new Progress(0, 0, null);
        }
        int clicks = 0;
        BlockPos next = QUEUE.peekFirst();
        for (var entry : JOBS.entrySet()) {
            BlockState state = mc.level.getBlockState(entry.getKey());
            if (state.getBlock() instanceof NoteBlock) {
                clicks += calculateClicks(state.getValue(BlockStateProperties.NOTE), entry.getValue().targetNote);
            }
        }
        return new Progress(JOBS.size(), clicks, next);
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
        TuneJob existing = JOBS.putIfAbsent(key, new TuneJob(targetNote));
        if (existing == null) {
            QUEUE.addLast(key);
        } else if (existing.targetNote != targetNote) {
            existing.targetNote = targetNote;
            existing.baseNote = -1;
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

            int pending = QUEUE.size();
            BlockPos pos;
            while (pending-- > 0 && (pos = QUEUE.pollFirst()) != null) {
                TuneJob job = JOBS.get(pos);
                if (job == null) {
                    continue;
                }

                BlockState state = mc.level.getBlockState(pos);
                if (!(state.getBlock() instanceof NoteBlock)) {
                    JOBS.remove(pos);
                    continue;
                }

                int currentNote = state.getValue(BlockStateProperties.NOTE);
                if (currentNote == job.targetNote) {
                    JOBS.remove(pos);
                    continue;
                }

                if (job.baseNote < 0) {
                    job.rebase(currentNote);
                }

                int confirmed = calculateClicks(job.baseNote, currentNote);
                if (confirmed > job.clicksSent) {
                    job.rebase(currentNote);
                } else if (confirmed != job.confirmed) {
                    job.confirmed = confirmed;
                    job.lastProgressMs = System.currentTimeMillis();
                }

                int needed = calculateClicks(job.baseNote, job.targetNote);
                if (job.clicksSent < needed) {
                    if (job.clicksLeft <= 0) {
                        JOBS.remove(pos);
                        continue;
                    }
                    if (!clickNoteBlock(mc, pos)) {
                        QUEUE.addLast(pos);
                        continue;
                    }
                    job.clicksSent++;
                    job.clicksLeft--;
                    job.lastProgressMs = System.currentTimeMillis();
                    QUEUE.addLast(pos);
                    break;
                }

                long ackTimeoutMs = Ping2Server.getRtt() + SERVER_ACK_GRACE_MS;
                if (System.currentTimeMillis() - job.lastProgressMs > ackTimeoutMs) {
                    job.rebase(currentNote);
                }
                QUEUE.addLast(pos);
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

    private static boolean clickNoteBlock(Minecraft mc, BlockPos pos) {
        LocalPlayer player = mc.player;
        if (player.isSecondaryUseActive()) {
            return false;
        }

        Direction face = Direction.getApproximateNearest(player.getEyePosition().subtract(Vec3.atCenterOf(pos)));
        if (face == Direction.UP && player.getMainHandItem().is(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS)) {
            face = player.getDirection().getOpposite();
        }

        RelativeBlockHitResult hitResult = new RelativeBlockHitResult(
                new Vec3(0.5 + 0.5 * face.getStepX(), 0.5 + 0.5 * face.getStepY(), 0.5 + 0.5 * face.getStepZ()),
                face,
                pos,
                false
        );
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
        player.swing(InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND).getInteractAnimation(), false);
        return true;
    }

    public static void clear() {
        QUEUE.clear();
        JOBS.clear();
        PUMP_SCHEDULED.set(false);
    }
}
