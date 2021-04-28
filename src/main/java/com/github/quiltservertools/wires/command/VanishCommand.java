package com.github.quiltservertools.wires.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VanishCommand {
    public static final StatusEffectInstance EFFECT_INSTANCE = new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 1, false, false, false);

    private static final VanishCommand vanishCommand = new VanishCommand();
    private List<ServerPlayerEntity> players = new ArrayList<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("vanish")
                .requires(scs -> Permissions.check(scs, "wires.vanish", 3))
                .then(argument("player", EntityArgumentType.player())
                        .executes(VanishCommand::modifyPlayer)));
    }

    private static int modifyPlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        getInstance().addOrRemove(player);
        ctx.getSource().sendFeedback(new LiteralText((getInstance().players.contains(player) ? "Vanished" : "Unvanished") + player.getName().asString()), true);
        return 1;
    }

    public static VanishCommand getInstance() {
        return vanishCommand;
    }

    public void addOrRemove(ServerPlayerEntity player) {
        if (this.players.contains(player)) {
            this.players.remove(player);
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
            Objects.requireNonNull(player.getServer()).getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
        } else {
            this.players.add(player);
            player.addStatusEffect(EFFECT_INSTANCE);
            Objects.requireNonNull(player.getServer()).getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, player));
        }
    }

    public List<ServerPlayerEntity> getPlayers() {
        return this.players;
    }
}
