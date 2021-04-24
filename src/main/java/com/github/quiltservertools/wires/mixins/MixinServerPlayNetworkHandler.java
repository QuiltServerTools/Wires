package com.github.quiltservertools.wires.mixins;

import com.github.quiltservertools.wires.Wires;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {

    @Shadow
    public ServerPlayerEntity player;

    @Final
    @Shadow
    private MinecraftServer server;

    @Inject(method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at = @At("HEAD"), cancellable = true)
    public void interceptChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (Wires.config.isPlayerMuted(player.getUuid()) && !Permissions.check(player, "wires.mute", 2) && !packet.getChatMessage().startsWith("/")) {
            player.sendSystemMessage(new LiteralText("You were muted! Could not send message, contact a moderator if you feel this is a mistake").formatted(Formatting.RED), Util.NIL_UUID);
            player.sendSystemMessage(new LiteralText("Reason: " + (Wires.config.getMute(player).isPresent() ? Wires.config.getMute(player).get().getReason() : "<None provided>")), Util.NIL_UUID);
            ci.cancel();
        }
    }
}
