package net.quiltservertools.wires

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.command.ServerCommandSource
import net.quiltservertools.wires.command.MaintenanceModeCommand
import net.quiltservertools.wires.command.MuteCommands
import net.quiltservertools.wires.command.StaffChatCommand
import net.quiltservertools.wires.command.VanishCommand
import net.quiltservertools.wires.config.Config
import org.apache.logging.log4j.LogManager

object Wires : DedicatedServerModInitializer {

    val LOGGER = LogManager.getLogger("Wires")

    override fun onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource>, dedicated: Boolean ->
            MaintenanceModeCommand.register(dispatcher)
            VanishCommand.register(dispatcher)
            MuteCommands.registerCommands(dispatcher)
            StaffChatCommand.register(dispatcher)
        })

        ServerLifecycleEvents.SERVER_STOPPING.register {
            VanishCommand.players
                .forEach {
                    VanishCommand.addOrRemove(it)
                }
            Config.shutdown()
        }

        Config
    }
}