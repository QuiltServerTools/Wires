package net.quiltservertools.wires.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.quiltservertools.wires.StaffChat
import net.quiltservertools.wires.util.Permissions.hasPermission

object StaffChatCommand {

    private const val name = "staffchat"

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal(name)
            .requires {
                it.hasPermission(name)
            }
            .executes { ctx: CommandContext<ServerCommandSource> ->
                StaffChat.toggle(ctx.source.player!!.uuid)
                ctx.source.sendFeedback(
                    Text.literal(
                        if (StaffChat.isInStaffChat(ctx.source.player!!.uuid)
                        ) "Moved to staff chat" else "Moved to game chat"
                    ), false
                )
                1
            }
            .then(
                CommandManager.argument("message", StringArgumentType.greedyString())
                    .executes {
                        StaffChat.sendMessage(it.source.player!!, StringArgumentType.getString(it, "message"))
                        1
                    })
        )
    }
}