package com.github.quiltservertools.wires.command.mute;

import com.github.quiltservertools.wires.Utils;
import com.github.quiltservertools.wires.Wires;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.time.Instant;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MuteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Mute command
        dispatcher.register(literal("mute").requires(scs -> Permissions.check(scs, "wires.mute", 3))
                .then(argument("target", GameProfileArgumentType.gameProfile())
                        .then(argument("time", StringArgumentType.string())
                                .executes(ctx -> mutePlayer(ctx.getSource(),
                                        GameProfileArgumentType.getProfileArgument(ctx, "target").stream().findFirst().isPresent() ? GameProfileArgumentType.getProfileArgument(ctx, "target").stream().findFirst().get() : null,
                                        Utils.parseTime(StringArgumentType.getString(ctx, "time")), ""))
                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(ctx -> mutePlayer(ctx.getSource(),
                                                GameProfileArgumentType.getProfileArgument(ctx, "target").stream().findFirst().isPresent() ? GameProfileArgumentType.getProfileArgument(ctx, "target").stream().findFirst().get() : null,
                                                Utils.parseTime(StringArgumentType.getString(ctx, "time")), StringArgumentType.getString(ctx, "reason")))
                                ))
                )
        );

        // Server mute command
        dispatcher.register(literal("servermute").requires(scs -> Permissions.check(scs, "wires.servermute", 3))
                .executes(ctx -> serverMute(ctx.getSource(), -1))
                .then(argument("time", StringArgumentType.string())
                        .executes(ctx -> serverMute(ctx.getSource(), Utils.parseTime(StringArgumentType.getString(ctx, "time"))))));
    }

    private static int mutePlayer(ServerCommandSource scs, GameProfile profile, long seconds, String reason) {
        if (profile == null) {
            scs.sendError(new LiteralText("Unable to locate player profile with provided username").formatted(Formatting.RED));
            return 0;
        }
        Wires.config.mute(profile, seconds, reason);
        scs.sendFeedback(new LiteralText("Muted " + profile.getName() + " for " + (seconds - Instant.now().getEpochSecond()) + "seconds"), true);
        return 1;
    }

    private static int serverMute(ServerCommandSource scs, long time) {
        boolean serverMuteStatus = Wires.config.serverMute(time);
        scs.sendFeedback(new LiteralText("Server mute " + (serverMuteStatus ? "enabled" : "disabled")), true);
        return 1;
    }

}
