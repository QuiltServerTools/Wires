package net.quiltservertools.wires.util

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

object Permissions {
    fun getPermission(command: String): Predicate<ServerCommandSource> {
        return Permissions.require("wires.$command", 3)
    }

    fun ServerCommandSource.hasPermission(command: String): Boolean {
        return Permissions.check(this, "wires.$command", 3)
    }
}