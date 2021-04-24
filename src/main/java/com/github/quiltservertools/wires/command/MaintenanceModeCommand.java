package com.github.quiltservertools.wires.command;

import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class MaintenanceModeCommand {
    private static boolean maintenanceMode = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("maintenance")
                        .requires(scs -> Permissions.check(scs, "wires.maintenance", 3))
                        .then(literal("on").executes(context -> setMaintenanceState(true, context.getSource())))
                        .then(literal("off").executes(context -> setMaintenanceState(false, context.getSource())))
        );
    }

    public static int setMaintenanceState(boolean state, ServerCommandSource scs) {
        maintenanceMode = state;
        scs.getMinecraftServer().getPlayerManager().getPlayerList().forEach(player -> {
                    if (!Permissions.check(player, "wires.maintenance", 3))
                        player.networkHandler.disconnect(new LiteralText("Server closed for maintenance"));
                }
        );
        scs.sendFeedback(new LiteralText(state ? "Enabled" : "Disabled").formatted(state ? Formatting.RED : Formatting.GREEN), true);
        return 1;
    }

    public static boolean getMaintenanceMode() {
        return maintenanceMode;
    }
}
