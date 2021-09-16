package com.github.quiltservertools.wires.mixins;

import com.github.quiltservertools.wires.Wires;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerQueryNetworkHandler.class)
public abstract class MixinServerQueryNetworkHandler {
    @Redirect(method = "onRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getServerMetadata()Lnet/minecraft/server/ServerMetadata;"))
    private ServerMetadata showMaintenance(MinecraftServer server) {
        var realData = server.getServerMetadata();

        if(Wires.CONFIG.isMaintenanceMode()) {
            var metadata = new ServerMetadata();
            metadata.setVersion(new ServerMetadata.Version("Maintenance", -1));
            metadata.setDescription(realData.getDescription());
            metadata.setFavicon(realData.getFavicon());
            metadata.setPlayers(realData.getPlayers());

            return metadata;
        }

        return realData;
    }
}
