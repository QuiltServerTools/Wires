package net.quiltservertools.wires

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import net.quiltservertools.wires.util.Permissions
import net.quiltservertools.wires.util.Permissions.hasPermission
import java.util.*
import kotlin.collections.ArrayList

object StaffChat {

    private val players: MutableList<UUID> = ArrayList()
    private val name = "staffchat"

    fun toggle(uuid: UUID) {
        if (!players.removeIf { it == uuid }) {
            players.add(uuid)
        }
    }

    fun isInStaffChat(uuid: UUID): Boolean {
        return players.stream().anyMatch { it == uuid }
    }

    fun sendMessage(sender: ServerPlayerEntity, message: String) {
        val text: Text =
            LiteralText("[Staff Chat]").formatted(Formatting.GOLD).append(LiteralText(" <${sender.name.asString()}> $message").formatted(Formatting.WHITE))
        sender.getServer()?.playerManager!!.playerList!!.stream()
            .filter {
                it.commandSource.hasPermission(name)
            }.forEach {
                it.sendSystemMessage(
                    text,
                    Util.NIL_UUID
                )
            }
    }
}