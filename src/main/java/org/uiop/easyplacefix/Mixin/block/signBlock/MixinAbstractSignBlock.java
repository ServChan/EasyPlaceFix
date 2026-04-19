package org.uiop.easyplacefix.Mixin.block.signBlock;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientWorld;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(SignBlock.class)
public class MixinAbstractSignBlock implements IBlock {

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ClientPacketListener clientPlayNetworkHandler = Minecraft.getInstance().getConnection();
        if (clientPlayNetworkHandler == null || Minecraft.getInstance().level == null) {
            return;
        }
        SignBlockEntity blockEntity = (SignBlockEntity) SchematicWorldHandler.getSchematicWorld().getBlockEntity(blockHitResult.getBlockPos());
        if (blockEntity == null) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                    .setCount(3)
                    .build(() -> PlayerBlockAction.openSignEditorAction.count = Math.max(PlayerBlockAction.openSignEditorAction.count - 1, 0))
            );
            return;
        }
        SignText backText = blockEntity.getBackText();
        SignText frontText = blockEntity.getFrontText();

        clientPlayNetworkHandler.send(
                new ServerboundSignUpdatePacket(
                        blockHitResult.getBlockPos(),
                        true,
                        frontText.getMessage(0, false).getString(),
                        frontText.getMessage(1, false).getString(),
                        frontText.getMessage(2, false).getString(),
                        frontText.getMessage(3, false).getString()


                )
        );

        for (int i = 0; i < backText.getMessages(false).length; i++) {
            if (!backText.getMessage(i, false).getString().isEmpty()) {
                clientPlayNetworkHandler.send(new ServerboundUseItemOnPacket(
                        InteractionHand.MAIN_HAND,
                        blockHitResult,
                        ((IClientWorld) Minecraft.getInstance().level).Sequence()

                ));

                clientPlayNetworkHandler.send(
                        new ServerboundSignUpdatePacket(
                                blockHitResult.getBlockPos(),
                                false,
                                backText.getMessage(0, false).getString(),
                                backText.getMessage(1, false).getString(),
                                backText.getMessage(2, false).getString(),
                                backText.getMessage(3, false).getString()


                        )
                );


                break;
            }
        }

        TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                .setCount(3)
                .build(() -> PlayerBlockAction.openSignEditorAction.count = Math.max(PlayerBlockAction.openSignEditorAction.count - 1, 0))
        );
    }
}
