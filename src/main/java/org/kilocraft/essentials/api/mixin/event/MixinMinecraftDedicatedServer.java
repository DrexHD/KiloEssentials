package org.kilocraft.essentials.api.mixin.event;


import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MixinMinecraftDedicatedServer {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(at = @At(value = "INVOKE", target = "net.minecraft.util.SystemUtil.getMeasuringTimeNano()J", ordinal = 1), method = "setupServer")
    private void kilo$setupServer$ready(CallbackInfoReturnable<Boolean> cir) {
        LOGGER.info("Starting the event manager...");

    }

}