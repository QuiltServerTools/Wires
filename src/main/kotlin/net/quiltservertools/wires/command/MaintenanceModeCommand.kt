package net.quiltservertools.wires.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import net.quiltservertools.wires.config.Config
import net.quiltservertools.wires.util.Permissions.hasPermission

object MaintenanceModeCommand {

    private const val name = "maintenance"

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal(name)
                .requires {
                    it.hasPermission(name)
                }
                .then(CommandManager.literal("on").executes { context: CommandContext<ServerCommandSource> ->
                    setMaintenanceState(
                        true,
                        context.source
                    )
                })
                .then(CommandManager.literal("off").executes { context: CommandContext<ServerCommandSource> ->
                    setMaintenanceState(
                        false,
                        context.source
                    )
                })
                .executes { context: CommandContext<ServerCommandSource> ->
                    setMaintenanceState(
                        !Config.isMaintenanceMode(),
                        context.source
                    )
                }
        )
    }

    private fun setMaintenanceState(state: Boolean, scs: ServerCommandSource): Int {
        Config.setMaintenanceMode(state)
        if (state) {
            scs.server.playerManager.playerList.forEach { player: ServerPlayerEntity ->
                if (!player.commandSource.hasPermission(name)
                ) player.networkHandler.disconnect(LiteralText("Server closed for maintenance"))
            }
        }
        scs.sendFeedback(
            LiteralText("Maintenance mode ").append(LiteralText(if (state) "enabled" else "disabled").formatted(if (state) Formatting.RED else Formatting.GREEN)),
            true
        )
        return 1
    }
}