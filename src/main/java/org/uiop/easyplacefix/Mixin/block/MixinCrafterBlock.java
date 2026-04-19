package org.uiop.easyplacefix.Mixin.block;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
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
        FrontAndTop orientation = blockState.getValue(BlockStateProperties.ORIENTATION);
        Direction facing = orientation.front();//Determines vertical vs horizontal orientation
        return facing != Direction.UP && facing != Direction.DOWN;
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openScreenAction.count++;
    }

    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {

        FrontAndTop orientation = blockState.getValue(BlockStateProperties.ORIENTATION);
        Direction facing = orientation.front();//Determines vertical vs horizontal orientation
        Direction rotation = orientation.top();//Determines rotation when vertical
        return switch (facing) {
            case UP -> switch (rotation) {
                case SOUTH -> new Tuple<>(LookAt.South, LookAt.Down);
                case WEST -> new Tuple<>(LookAt.West, LookAt.Down);
                case EAST -> new Tuple<>(LookAt.East, LookAt.Down);
                default -> new Tuple<>(LookAt.North, LookAt.Down);
            };
            case DOWN -> switch (rotation) {
                case SOUTH -> new Tuple<>(LookAt.North, LookAt.Up);
                case WEST -> new Tuple<>(LookAt.East, LookAt.Up);
                case EAST -> new Tuple<>(LookAt.West, LookAt.Up);
                default -> new Tuple<>(LookAt.South, LookAt.Up);
            };
            case SOUTH -> new Tuple<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            case NORTH -> new Tuple<>(LookAt.South, LookAt.Horizontal);
        };
    }

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ClientPacketListener clientPlayNetworkHandler = Minecraft.getInstance().getConnection();
        if (clientPlayNetworkHandler == null || Minecraft.getInstance().level == null) {
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
            int sequence = ((IClientWorld) Minecraft.getInstance().level).Sequence();
            clientPlayNetworkHandler.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, sequence));
//            TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(5).build(()->{
                for (short slot = 0; slot < crafterSlot.size(); slot++) {
                    boolean isDisable = crafterSlot.get(slot);
                    if (isDisable) {
                        clientPlayNetworkHandler.send(new ServerboundContainerSlotStateChangedPacket(slot, EasyPlaceFix.screenId, false));//TODO
                        clientPlayNetworkHandler.send(new ServerboundContainerClickPacket(EasyPlaceFix.screenId, 1, slot, (byte) 0, ContainerInput.PICKUP, Int2ObjectMaps.<HashedStack>emptyMap(), HashedStack.EMPTY));
                    }

                }
                clientPlayNetworkHandler.send(new ServerboundContainerClosePacket(EasyPlaceFix.screenId));

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
