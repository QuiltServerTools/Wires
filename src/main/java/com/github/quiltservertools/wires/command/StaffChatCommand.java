package com.github.quiltservertools.wires.command;

import com.github.quiltservertools.wires.Wires;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class StaffChatCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("staffchat")
                .requires(scs -> Permissions.check(scs, "wires.staffchat", 2))
                .executes(ctx -> {
                    Wires.config.getStaffChat().toggle(ctx.getSource().getPlayer().getUuid());
                    ctx.getSource().sendFeedback(new LiteralText(Wires.config.getStaffChat().isInStaffChat(ctx.getSource().getPlayer().getUuid()) ? "Moved to staff chat" : "Moved to game chat"), false);
                    return 1;
                })
                .then(argument("message", StringArgumentType.greedyString()).executes(
                        ctx -> {
                            Wires.config.getStaffChat().sendMessage(ctx.getSource().getPlayer(), StringArgumentType.getString(ctx, "message"));
                            return 1;
                        }
                )));

    }
}
