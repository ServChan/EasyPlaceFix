package org.uiop.easyplacefix.util;

import com.tick_ins.packet.Ping2Server;
import org.uiop.easyplacefix.config.easyPlacefixConfig;

import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerBlockAction {

    private static final long SUPPRESSION_TIMEOUT_MS = 3000L;

    public static int suppressionReleaseTicks() {
        return Math.max(3, Ping2Server.getRtt() / 50 + 3);
    }

    public static class openScreenAction {
        public static volatile int count = 0;
        private static volatile long armedAtMs = 0L;

        public static void arm() {
            armedAtMs = System.currentTimeMillis();
            count++;
        }

        public static boolean run() {
            if (count > 0 && System.currentTimeMillis() - armedAtMs > SUPPRESSION_TIMEOUT_MS) {
                count = 0;
            }
            return count == 0;
        }
    }

    public static class openSignEditorAction {
        public static volatile int count = 0;
        private static volatile long armedAtMs = 0L;

        public static void arm() {
            armedAtMs = System.currentTimeMillis();
            count++;
        }

        public static boolean run() {
            if (count > 0 && System.currentTimeMillis() - armedAtMs > SUPPRESSION_TIMEOUT_MS) {
                count = 0;
            }
            return count == 0;
        }
    }

    public static class useItemOnAction {
        public static boolean modifyBoolean = false;

        public static Map<BlockPos, Long> lastPlacementTimeMap = new ConcurrentHashMap<>();
        public static BlockState pistonBlockState = null;

        private static volatile int lastGlobalPlacementTick = Integer.MIN_VALUE;
        private static volatile long lastGlobalPlacementTimeMs = 0L;
        private static volatile int jitterExtraTicks = 0;
        private static final long PLACEMENT_OVERRIDE_TTL_MS = 1200L;
        private static final int PLACEMENT_OVERRIDE_MAX_SIZE = 512;
        private static final int PLACEMENT_OVERRIDE_USES = 4;
        private static final ConcurrentLinkedDeque<PlacementStateOverride> placementStateOverrides = new ConcurrentLinkedDeque<>();

        private static final class PlacementStateOverride {
            private final BlockPos targetPos;
            private final Class<? extends Block> blockClass;
            private final Direction hitSide;
            private final BlockState state;
            private final long expiresAt;
            private int usesLeft;

            private PlacementStateOverride(
                    BlockPos targetPos,
                    Class<? extends Block> blockClass,
                    Direction hitSide,
                    BlockState state,
                    long expiresAt,
                    int usesLeft
            ) {
                this.targetPos = targetPos;
                this.blockClass = blockClass;
                this.hitSide = hitSide;
                this.state = state;
                this.expiresAt = expiresAt;
                this.usesLeft = usesLeft;
            }

            private synchronized boolean consumeOneUse() {
                if (this.usesLeft <= 0) {
                    return false;
                }
                this.usesLeft--;
                return true;
            }

            private synchronized boolean exhausted() {
                return this.usesLeft <= 0;
            }
        }

        private static void pruneExpiredOverrides() {
            long now = System.currentTimeMillis();
            Iterator<PlacementStateOverride> iterator = placementStateOverrides.iterator();
            while (iterator.hasNext()) {
                PlacementStateOverride entry = iterator.next();
                if (entry.expiresAt < now || entry.exhausted()) {
                    iterator.remove();
                }
            }
        }

        public static void armPlacementStateOverride(BlockPos targetPos, BlockState state, Direction hitSide) {
            if (state == null || targetPos == null) {
                return;
            }

            pruneExpiredOverrides();
            placementStateOverrides.addLast(new PlacementStateOverride(
                    targetPos.immutable(),
                    state.getBlock().getClass(),
                    hitSide,
                    state,
                    System.currentTimeMillis() + PLACEMENT_OVERRIDE_TTL_MS,
                    PLACEMENT_OVERRIDE_USES
            ));
            while (placementStateOverrides.size() > PLACEMENT_OVERRIDE_MAX_SIZE) {
                placementStateOverrides.pollFirst();
            }
        }

        public static BlockState consumePlacementStateOverrideFor(Class<? extends Block> blockClass, BlockPos targetPos) {
            if (blockClass == null || targetPos == null || !Minecraft.getInstance().isSameThread()) {
                return null;
            }

            pruneExpiredOverrides();
            Iterator<PlacementStateOverride> iterator = placementStateOverrides.descendingIterator();
            while (iterator.hasNext()) {
                PlacementStateOverride entry = iterator.next();
                if (matchesPlacementOverride(entry, blockClass, targetPos, false)) {
                    if (entry.consumeOneUse()) {
                        if (entry.exhausted()) {
                            iterator.remove();
                        }
                        return entry.state;
                    }
                }
            }

            iterator = placementStateOverrides.descendingIterator();
            while (iterator.hasNext()) {
                PlacementStateOverride entry = iterator.next();
                if (matchesPlacementOverride(entry, blockClass, targetPos, true)) {
                    if (entry.consumeOneUse()) {
                        if (entry.exhausted()) {
                            iterator.remove();
                        }
                        return entry.state;
                    }
                }
            }
            return null;
        }

        private static boolean matchesPlacementOverride(
                PlacementStateOverride entry,
                Class<? extends Block> blockClass,
                BlockPos targetPos,
                boolean allowOffsetFallback
        ) {
            if (!blockClass.isAssignableFrom(entry.blockClass) || !blockClass.isInstance(entry.state.getBlock())) {
                return false;
            }
            if (entry.targetPos.equals(targetPos)) {
                return true;
            }
            return allowOffsetFallback
                    && entry.hitSide != null
                    && entry.targetPos.relative(entry.hitSide).equals(targetPos);
        }

        public static void clearPlacementStateOverride() {
            placementStateOverrides.clear();
        }

        public static boolean isGlobalPlacementCooling() {
            int delayTicks = easyPlacefixConfig.getEffectivePlacementDelayTicks();
            if (delayTicks <= 0) {
                return false;
            }
            int effectiveDelay = delayTicks + Math.max(0, jitterExtraTicks);

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && lastGlobalPlacementTick != Integer.MIN_VALUE) {
                int elapsedTicks = mc.player.tickCount - lastGlobalPlacementTick;
                if (elapsedTicks >= 0 && elapsedTicks < effectiveDelay) {
                    return true;
                }
            }

            return System.currentTimeMillis() - lastGlobalPlacementTimeMs < effectiveDelay * 50L;
        }

        public static void markGlobalPlacement() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                lastGlobalPlacementTick = mc.player.tickCount;
            }
            lastGlobalPlacementTimeMs = System.currentTimeMillis();
            jitterExtraTicks = easyPlacefixConfig.PLACEMENT_JITTER.getBooleanValue()
                    ? ThreadLocalRandom.current().nextInt(0, 2)
                    : 0;
        }

        public static void resetGlobalPlacement() {
            lastGlobalPlacementTick = Integer.MIN_VALUE;
            lastGlobalPlacementTimeMs = 0L;
            jitterExtraTicks = 0;
        }

        public static boolean isPlacementCooling(BlockPos pos) {
            long now = System.currentTimeMillis();
            long threshold = Ping2Server.getRtt() + 100;

            if (lastPlacementTimeMap.size() > 256) {
                lastPlacementTimeMap.entrySet().removeIf(e -> now - e.getValue() > 10_000L);
            }

            if (lastPlacementTimeMap.containsKey(pos)) {
                long lastPlaceTime = lastPlacementTimeMap.get(pos);
                if (now - lastPlaceTime > threshold) {
                    lastPlacementTimeMap.put(pos, now);
                    return false;
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("EasyPlace cooldown hit at {} (elapsed={}ms, threshold={}ms)",
                                pos, now - lastPlaceTime, threshold);
                    }
                    return true;
                }
            }
            lastPlacementTimeMap.put(pos, now);
            return false;

        }
    }
}
