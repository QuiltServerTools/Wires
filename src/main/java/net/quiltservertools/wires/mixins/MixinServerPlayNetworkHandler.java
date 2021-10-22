package net.quiltservertools.wires.mixins;

import net.quiltservertools.wires.StaffChat;
import net.quiltservertools.wires.command.MuteCommands;
import net.quiltservertools.wires.command.VanishCommand;
import net.quiltservertools.wires.config.Config;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.quiltservertools.wires.util.Permissions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    private static final Text muted = new LiteralText("Unable to send message: you have been muted. Contact a moderator if you believe this is a mistake.").formatted(Formatting.RED);
    private static final Text serverMuted = new LiteralText("Unable to send message: the chat has been server muted. You can still use commands. Contact a moderator if you believe this is a mistake.").formatted(Formatting.YELLOW);

    @Shadow
    public ServerPlayerEntity player;

    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at = @At("HEAD"), cancellable = true)
    public void interceptChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        Config config = Config.INSTANCE;
        String message = packet.getChatMessage();
        if (config.isPlayerMuted(player.getUuid()) && Permissions.INSTANCE.hasPermission(player.getCommandSource(), MuteCommands.name)) {
            player.sendSystemMessage(muted, Util.NIL_UUID);
            player.sendSystemMessage(new LiteralText("Reason: " + (config.getMute(player).isPresent() ? config.getMute(player).get().getReason() : "<None provided>")), Util.NIL_UUID);
            ci.cancel();
        } else if (config.getServerMute().getState() && !Permissions.INSTANCE.hasPermission(player.getCommandSource(), MuteCommands.serverMute) && !message.startsWith("/") && !message.startsWith("/me")) {
            player.sendSystemMessage(serverMuted, Util.NIL_UUID);
            ci.cancel();
        } else if (StaffChat.INSTANCE.isInStaffChat(player.getUuid()) && !message.startsWith("/")) {
            StaffChat.INSTANCE.sendMessage(player, message);
            ci.cancel();
        }
    }

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    public void removeFromVanish(Text reason, CallbackInfo ci) {
        VanishCommand.INSTANCE.removePlayer(this.getPlayer());
    }
}
