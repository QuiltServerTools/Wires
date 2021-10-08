package net.quiltservertools.wires.command

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import net.quiltservertools.wires.Utils.parseTime
import net.quiltservertools.wires.config.Config
import net.quiltservertools.wires.util.Permissions.hasPermission
import java.time.Instant

object MuteCommands {

    const val name = "mute"
    const val serverMute = "servermute"

    private const val noneProvided = "<none provided>"

    fun registerCommands(dispatcher: CommandDispatcher<ServerCommandSource>) {

        // Mute command
        dispatcher.register(CommandManager.literal(name).requires { scs: ServerCommandSource ->
            scs.hasPermission(name)
        }
            .then(CommandManager.argument("target", GameProfileArgumentType.gameProfile())
                .executes {
                    mutePlayer(it.source, GameProfileArgumentType.getProfileArgument(it, "target").first(), -1, noneProvided)
                }
                .then(CommandManager.argument("time", StringArgumentType.string())
                    .executes { ctx: CommandContext<ServerCommandSource> ->
                        mutePlayer(
                            ctx.source,
                            GameProfileArgumentType.getProfileArgument(ctx, "target").first(),
                            parseTime(StringArgumentType.getString(ctx, "time")), noneProvided
                        )
                    }
                    .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                        .executes { ctx: CommandContext<ServerCommandSource> ->
                            mutePlayer(
                                ctx.source,
                                GameProfileArgumentType.getProfileArgument(ctx, "target").first(),
                                parseTime(StringArgumentType.getString(ctx, "time")),
                                StringArgumentType.getString(ctx, "reason")
                            )
                        }
                    )
                )
            )
        )

        // Unmute command
        dispatcher.register(CommandManager.literal("unmute")
            .requires { it.hasPermission(name) }
            .then(CommandManager.argument("target", GameProfileArgumentType.gameProfile())
                .executes {
                    unmutePlayer(it.source, GameProfileArgumentType.getProfileArgument(it, "target").first())
                }))

        // Server mute command
        dispatcher.register(CommandManager.literal(serverMute).requires { scs: ServerCommandSource ->
            scs.hasPermission(serverMute)
        }
            .executes { ctx: CommandContext<ServerCommandSource> ->
                serverMute(
                    ctx.source,
                    -1
                )
            }
            .then(CommandManager.argument("time", StringArgumentType.string())
                .executes { ctx: CommandContext<ServerCommandSource> ->
                    serverMute(
                        ctx.source,
                        parseTime(StringArgumentType.getString(ctx, "time"))
                    )
                }))
    }

    private fun mutePlayer(scs: ServerCommandSource, profile: GameProfile?, seconds: Long, reason: String): Int {
        if (profile == null) {
            scs.sendError(LiteralText("Unable to locate player profile with provided username").formatted(Formatting.RED))
            return 0
        }
        Config.mute(profile, seconds, reason)
        scs.sendFeedback(
            LiteralText("Muted ${profile.name} for${
                if (seconds < 0) {
                    "ever"
                } else {
                    " ${seconds - Instant.now().epochSecond} seconds"
                }
            }"),
            true
        )
        return 1
    }
    private fun unmutePlayer(scs: ServerCommandSource, profile: GameProfile): Int {
        Config.unmute(profile)
        scs.sendFeedback(
            LiteralText("Unmuted ${profile.name}"),
            true
        )
        return 1
    }

    private fun serverMute(scs: ServerCommandSource, time: Long): Int {
        val serverMuteStatus: Boolean = Config.serverMute(time)
        scs.sendFeedback(LiteralText("Server mute ${if (serverMuteStatus) "enabled" else "disabled"}"), true)
        return 1
    }
}