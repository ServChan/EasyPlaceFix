package org.uiop.easyplacefix.Mixin.block;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.enums.Orientation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.EasyPlaceFix;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientWorld;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.until.PlayerBlockAction;

import static org.uiop.easyplacefix.EasyPlaceFix.crafterOperation;
import static org.uiop.easyplacefix.EasyPlaceFix.crafterSlot;

@Mixin(value = CrafterBlock.class)
public class MixinCrafterBlock implements IBlock {
    @Override
    public boolean HasSleepTime(BlockState blockState) {
        Orientation orientation = blockState.get(Properties.ORIENTATION);
        Direction facing = orientation.getFacing();//Determines vertical vs horizontal orientation
        return facing != Direction.UP && facing != Direction.DOWN;
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openScreenAction.count++;
    }

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {

        Orientation orientation = blockState.get(Properties.ORIENTATION);
        Direction facing = orientation.getFacing();//Determines vertical vs horizontal orientation
        Direction rotation = orientation.getRotation();//Determines rotation when vertical
        return switch (facing) {
            case UP -> switch (rotation) {
                case SOUTH -> new Pair<>(LookAt.South, LookAt.Down);
                case WEST -> new Pair<>(LookAt.West, LookAt.Down);
                case EAST -> new Pair<>(LookAt.East, LookAt.Down);
                default -> new Pair<>(LookAt.North, LookAt.Down);
            };
            case DOWN -> switch (rotation) {
                case SOUTH -> new Pair<>(LookAt.North, LookAt.Up);
                case WEST -> new Pair<>(LookAt.East, LookAt.Up);
                case EAST -> new Pair<>(LookAt.West, LookAt.Up);
                default -> new Pair<>(LookAt.South, LookAt.Up);
            };
            case SOUTH -> new Pair<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            case NORTH -> new Pair<>(LookAt.South, LookAt.Horizontal);
        };
    }

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (clientPlayNetworkHandler == null || MinecraftClient.getInstance().world == null) {
            return;
        }
        crafterOperation = false;
        CrafterBlockEntity blockEntity = (CrafterBlockEntity) SchematicWorldHandler.getSchematicWorld().getBlockEntity(blockHitResult.getBlockPos());
        if (blockEntity == null) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                    .setCount(3).build(() -> PlayerBlockAction.openScreenAction.count = Math.max(PlayerBlockAction.openScreenAction.count - 1, 0))
            );
            return;
        }
        for (int i = 0; i < 9; i++) {//TODO
            boolean isDisabled = blockEntity.isSlotDisabled(i);
            crafterSlot.set(i, isDisabled);
            if (!crafterOperation && isDisabled) {
                crafterOperation = true;
            }
        }
        if (crafterOperation) {
            int sequence = ((IClientWorld) MinecraftClient.getInstance().world).Sequence();
            clientPlayNetworkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, sequence));
//            TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(5).build(()->{
                for (short slot = 0; slot < crafterSlot.size(); slot++) {
                    boolean isDisable = crafterSlot.get(slot);
                    if (isDisable) {
                        clientPlayNetworkHandler.sendPacket(new SlotChangedStateC2SPacket(slot, EasyPlaceFix.screenId, false));//TODO
                        clientPlayNetworkHandler.sendPacket(new ClickSlotC2SPacket(EasyPlaceFix.screenId, 1, slot, (byte) 0, SlotActionType.PICKUP, Int2ObjectMaps.emptyMap(), ItemStackHash.EMPTY));
                    }

                }
                clientPlayNetworkHandler.sendPacket(new CloseHandledScreenC2SPacket(EasyPlaceFix.screenId));

//            }));


        }
        TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                .setCount(3).build(() -> PlayerBlockAction.openScreenAction.count = Math.max(PlayerBlockAction.openScreenAction.count - 1, 0))
        );

//        var BlockActionPacket = new PlayerInteractBlockC2SPacket(
//                Hand.MAIN_HAND,
//                blockHitResult,
//                ((IClientWorld) MinecraftClient.getInstance().world).Sequence()
//        );
//
//        ((IisSimpleHitPos) BlockActionPacket).setSimpleHitPos();
//        clientPlayNetworkHandler.sendPacket(BlockActionPacket);
    }

}
//  if (stateSchematic.contains(Properties.ORIENTATION)) {
//CrafterBlockEntity blockEntity = (CrafterBlockEntity) SchematicWorldHandler.getSchematicWorld().getBlockEntity(trace.getBlockPos());
//                for (int i = 0; i < 9; i++) {//TODO
//boolean isDisabled = blockEntity.isSlotDisabled(i);
//                    crafterSlot.set(i, isDisabled);
//                    if (!crafterOperation && isDisabled) {
//crafterOperation = true;
//        }
//        }
//
//        }
