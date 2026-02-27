package org.uiop.easyplacefix.Mixin.block.signBlock;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientWorld;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(AbstractSignBlock.class)
public class MixinAbstractSignBlock implements IBlock {

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (clientPlayNetworkHandler == null || MinecraftClient.getInstance().world == null) {
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

        clientPlayNetworkHandler.sendPacket(
                new UpdateSignC2SPacket(
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
                clientPlayNetworkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
                        Hand.MAIN_HAND,
                        blockHitResult,
                        ((IClientWorld) MinecraftClient.getInstance().world).Sequence()

                ));

                clientPlayNetworkHandler.sendPacket(
                        new UpdateSignC2SPacket(
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
