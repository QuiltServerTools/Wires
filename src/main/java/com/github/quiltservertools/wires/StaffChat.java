package com.github.quiltservertools.wires;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StaffChat {
    private List<UUID> players = new ArrayList<>();

    public void toggle(UUID uuid) {
        if (!players.removeIf(uuid1 -> uuid1.equals(uuid))) {
            players.add(uuid);
        }
    }

    public boolean isInStaffChat(UUID uuid) {
        return this.players.stream().anyMatch(uuid1 -> uuid1.equals(uuid));
    }

    public void sendMessage(ServerPlayerEntity sender, String message) {
        Text text = new LiteralText("[Staff Chat] <" + sender.getName().asString() +"> " + message).formatted(Formatting.YELLOW);
        Objects.requireNonNull(sender.getServer()).getPlayerManager().getPlayerList().stream().filter(serverPlayerEntity -> Permissions.check(serverPlayerEntity, "wires.staffchat", 3)).forEach(player -> {
            player.sendSystemMessage(text, Util.NIL_UUID);
        });
    }
}
