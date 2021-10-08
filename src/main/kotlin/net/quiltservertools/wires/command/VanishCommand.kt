package net.quiltservertools.wires.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.quiltservertools.wires.util.Permissions.hasPermission

object VanishCommand {

    val players: MutableList<ServerPlayerEntity> = ArrayList()

    fun addOrRemove(player: ServerPlayerEntity) {
        if (players.contains(player)) {
            players.remove(player)
            player.removeStatusEffect(StatusEffects.INVISIBILITY)
            player.getServer()!!.playerManager.sendToAll(
                PlayerListS2CPacket(
                    PlayerListS2CPacket.Action.ADD_PLAYER,
                    player
                )
            )
        } else {
            players.add(player)
            player.addStatusEffect(EFFECT_INSTANCE)
            player.getServer()!!.playerManager.sendToAll(
                PlayerListS2CPacket(
                    PlayerListS2CPacket.Action.REMOVE_PLAYER,
                    player
                )
            )
        }
    }

    private const val name = "vanish"
    private val EFFECT_INSTANCE = StatusEffectInstance(StatusEffects.INVISIBILITY, Int.MAX_VALUE, 1, false, false, false)

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal(name)
            .requires {
                it.hasPermission(name)
            }
            .then(CommandManager.argument("player", EntityArgumentType.player())
                .executes { ctx: CommandContext<ServerCommandSource> ->
                    modifyPlayer(
                        ctx
                    )
                })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun modifyPlayer(ctx: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(ctx, "player")
        addOrRemove(player)
        ctx.source.sendFeedback(
            LiteralText("${if (players.contains(player)) "Vanished" else "Unvanished"} ${player.name.asString()}"),
            true
        )
        return 1
    }
}