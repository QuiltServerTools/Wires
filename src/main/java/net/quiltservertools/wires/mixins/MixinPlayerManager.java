package net.quiltservertools.wires.mixins;

import net.quiltservertools.wires.command.VanishCommand;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.quiltservertools.wires.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Inject(method = "checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    public void checkMaintenanceMode(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        if(Config.INSTANCE.isMaintenanceMode() && !((PlayerManager) (Object) this).isOperator(profile)) {
            cir.setReturnValue(new LiteralText("Server is closed for maintenance"));
        }
    }
    @Inject(method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("RETURN"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity entity, CallbackInfo ci) {
        VanishCommand.INSTANCE.getPlayers().forEach(player -> connection.send(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, player)));
    }
}
