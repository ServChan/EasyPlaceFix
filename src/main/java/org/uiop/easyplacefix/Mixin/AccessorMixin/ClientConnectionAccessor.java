package org.uiop.easyplacefix.Mixin.AccessorMixin;


import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public interface ClientConnectionAccessor {
   @Accessor("channel")
    Channel getChannel();
}
