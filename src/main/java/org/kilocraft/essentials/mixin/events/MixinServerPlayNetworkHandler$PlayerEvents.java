package org.kilocraft.essentials.mixin.events;

import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import net.minecraft.server.network.packet.HandSwingC2SPacket;
import net.minecraft.server.network.packet.PlayerInteractBlockC2SPacket;
import net.minecraft.server.network.packet.PlayerInteractItemC2SPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.*;
import org.kilocraft.essentials.events.player.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler$PlayerEvents {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow public abstract void onClientCommand(ClientCommandC2SPacket clientCommandC2SPacket);

    @Shadow @Final private MinecraftServer server;

    @Inject(at = @At("HEAD"), method = "onDisconnected")
    private void oky$remove(Text text_1, CallbackInfo ci) {
        PlayerDisconnectEvent event = KiloServer.getServer().triggerEvent(new PlayerDisconnectEventImpl(player));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/text/Text;)V"), method = "onDisconnected")
    private void oky$remove$sendToAll(PlayerManager playerManager, Text text_1) {
        //Ignored
    }

    @Inject(method = "onPlayerInteractItem", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onPlayerInteractItem(Lnet/minecraft/server/network/packet/PlayerInteractItemC2SPacket;)V"))
    private void modifyOnPlayerInteractItem(PlayerInteractItemC2SPacket playerInteractItemC2SPacket, CallbackInfo ci) {
        ci.cancel();
        NetworkThreadUtils.forceMainThread(playerInteractItemC2SPacket, (ServerPlayPacketListener) this, this.player.getServerWorld());
        ServerWorld serverWorld = this.server.getWorld(this.player.dimension);
        Hand hand = playerInteractItemC2SPacket.getHand();
        ItemStack itemStack = this.player.getStackInHand(hand);
        this.player.updateLastActionTime();

        if (!itemStack.isEmpty()) {
            PlayerInteractItemStartEvent event = new PlayerInteractItemStartEventImpl(
                    player, player.getEntityWorld(), playerInteractItemC2SPacket.getHand(), player.getStackInHand(playerInteractItemC2SPacket.getHand()));
            KiloServer.getServer().triggerEvent(event);

            if (event.isCancelled())
                this.player.inventory.updateItems();
            else
                this.player.interactionManager.interactItem(this.player, serverWorld, itemStack, hand);
        }
    }

    @Inject(method = "onHandSwing", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onHandSwing(Lnet/minecraft/server/network/packet/HandSwingC2SPacket;)V"))
    private void modifyOnHandSwing(HandSwingC2SPacket handSwingC2SPacket, CallbackInfo ci) {
        PlayerOnHandSwingEvent event = new PlayerOnHandSwingEventImpl(player, handSwingC2SPacket.getHand());
        KiloServer.getServer().triggerEvent(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "onPlayerInteractBlock", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onPlayerInteractBlock(Lnet/minecraft/server/network/packet/PlayerInteractBlockC2SPacket;)V"))
    private void modifyOnIteractBlock(PlayerInteractBlockC2SPacket playerInteractBlockC2SPacket, CallbackInfo ci) {
        PlayerInteractBlockEvent event = new PlayerInteractBlockEventImpl(player, playerInteractBlockC2SPacket.getHitY(), playerInteractBlockC2SPacket.getHand());
        KiloServer.getServer().triggerEvent(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "onClientCommand", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onClientCommand(Lnet/minecraft/server/network/packet/ClientCommandC2SPacket;)V"))
    private void modifyOnClientCommand(ClientCommandC2SPacket clientCommandC2SPacket, CallbackInfo ci) {
        PlayerClientCommandEvent event = new PlayerClientCommandEventImpl(player, clientCommandC2SPacket.getMode());
        KiloServer.getServer().triggerEvent(event);
        if (event.isCancelled())
            ci.cancel();
    }

}