package com.github.quiltservertools.wires.mixins;

import com.github.quiltservertools.wires.Wires;
import com.github.quiltservertools.wires.config.Config;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
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

    private static final Text muted = new LiteralText("Unable to send message: you have been muted. Contact a moderator if you believe this is a mistake.").formatted(Formatting.RED);
    private static final Text serverMuted = new LiteralText("Unable to send message: the chat has been server muted. You can still use commands. Contact a moderator if you believe this is a mistake.").formatted(Formatting.YELLOW);

    @Shadow
    public ServerPlayerEntity player;

    @Final
    @Shadow
    private MinecraftServer server;

    @Inject(method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at = @At("HEAD"), cancellable = true)
    public void interceptChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        Config config = Wires.config;
        if (config.isPlayerMuted(player.getUuid()) && !Permissions.check(player, "wires.mute", 2)) {
            player.sendSystemMessage(muted, Util.NIL_UUID);
            player.sendSystemMessage(new LiteralText("Reason: " + (config.getMute(player).isPresent() ? config.getMute(player).get().getReason() : "<None provided>")), Util.NIL_UUID);
            ci.cancel();
        } else if (config.getServerMute().getState() && !Permissions.check(player, "wires.servermute", 2) && !packet.getChatMessage().startsWith("/") && !packet.getChatMessage().startsWith("/me")) {
            player.sendSystemMessage(serverMuted, Util.NIL_UUID);
            ci.cancel();
        }
    }
}
