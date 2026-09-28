package org.uiop.easyplacefix.mixin.block.signBlock;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientWorld;
import org.uiop.easyplacefix.util.PlayerBlockAction;

import java.util.ArrayList;
import java.util.List;

@Mixin(SignBlock.class)
public class MixinAbstractSignBlock implements IBlock {

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ClientPacketListener clientPlayNetworkHandler = Minecraft.getInstance().getConnection();
        if (clientPlayNetworkHandler == null || Minecraft.getInstance().level == null) {
            return;
        }
        var schematicWorld = SchematicWorldHandler.getSchematicWorld();
        SignBlockEntity blockEntity = schematicWorld != null
                && schematicWorld.getBlockEntity(blockHitResult.getBlockPos()) instanceof SignBlockEntity sign ? sign : null;
        if (blockEntity == null) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                    .setCount(PlayerBlockAction.suppressionReleaseTicks())
                    .build(() -> PlayerBlockAction.openSignEditorAction.count = Math.max(PlayerBlockAction.openSignEditorAction.count - 1, 0))
            );
            return;
        }
        SignText backText = blockEntity.getText(SignTextSlot.BACK);
        SignText frontText = blockEntity.getText(SignTextSlot.FRONT);

        clientPlayNetworkHandler.send(
                new ServerboundSignUpdatePacket(
                        blockHitResult.getBlockPos(),
                        signLines(frontText),
                        SignTextSlot.FRONT
                )
        );

        for (Component line : backText.getMessages(false)) {
            if (!line.getString().isEmpty()) {
                clientPlayNetworkHandler.send(new ServerboundUseItemOnPacket(
                        InteractionHand.MAIN_HAND,
                        blockHitResult,
                        ((IClientWorld) Minecraft.getInstance().level).Sequence()

                ));

                clientPlayNetworkHandler.send(
                        new ServerboundSignUpdatePacket(
                                blockHitResult.getBlockPos(),
                                signLines(backText),
                                SignTextSlot.BACK
                        )
                );

                break;
            }
        }

        TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                .setCount(PlayerBlockAction.suppressionReleaseTicks())
                .build(() -> PlayerBlockAction.openSignEditorAction.count = Math.max(PlayerBlockAction.openSignEditorAction.count - 1, 0))
        );
    }

    private static List<String> signLines(SignText text) {
        List<String> lines = new ArrayList<>(SignText.LINES);
        for (Component line : text.getMessages(false)) {
            lines.add(line.getString());
        }
        return lines;
    }
}
