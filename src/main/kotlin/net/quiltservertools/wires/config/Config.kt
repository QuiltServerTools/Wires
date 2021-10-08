package net.quiltservertools.wires.config

import com.google.gson.*
import com.mojang.authlib.GameProfile
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity
import net.quiltservertools.wires.Wires
import net.quiltservertools.wires.mute.Mute
import net.quiltservertools.wires.mute.ServerMute
import java.io.IOException
import java.nio.file.Files
import java.time.Instant
import java.util.*
import java.util.function.Consumer

object Config {
    /*
    Contains config values from the JSON config file
     */
    val PATH = FabricLoader.getInstance().configDir.resolve("wires.json")

    private lateinit var json: JsonObject
    private var muted = mutableListOf<Mute>()
    private val serverMute = ServerMute()
    private var maintenanceMode = false

    init {
        try {
            json = JsonParser().parse(String(Files.readAllBytes(PATH))).asJsonObject
            initMuted()
            maintenanceMode = initMaintenanceMode()
            Wires.LOGGER.info("Loaded Wires config")
        } catch (e: IOException) {
            muted = ArrayList()
            Wires.LOGGER.info("Unable to find existing Wires config file")
            Wires.LOGGER.info("A config file will be created on server shutdown")
        }
    }

    private fun initMaintenanceMode(): Boolean {
        return try {
            json["maintenance_mode"].asBoolean
        } catch (e: JsonSyntaxException) {
            false
        }
    }

    private fun initMuted() {
        val muted = json["muted"].asJsonArray
        val list: MutableList<Mute> = ArrayList()
        muted.forEach {
            val json = it.asJsonObject
            list.add(
                Mute(
                    json["reason"].asString,
                    UUID.fromString(json["uuid"].asString),
                    json["time"].asLong
                )
            )
        }
        Config.muted = list
    }

    fun shutdown() {
        val json = JsonObject()

        // Saving for mutes
        val muteArray = JsonArray()
        // Don't save finished mutes
        muted.removeIf { mute: Mute ->
            mute.time < Instant.now().epochSecond
        }
        muted.forEach(
            Consumer { mute: Mute ->
                val jsonObject = JsonObject()
                jsonObject.add("uuid", JsonPrimitive(mute.uuid.toString()))
                jsonObject.add("time", JsonPrimitive(mute.time))
                jsonObject.add("reason", JsonPrimitive(mute.reason))
                muteArray.add(jsonObject)
            }
        )
        json.add("muted", muteArray)
        json.addProperty("maintenance_mode", maintenanceMode)

        // Saving for other config options
        val gson = GsonBuilder().setPrettyPrinting().create()
        try {
            Files.write(PATH, gson.toJson(json).toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
            Wires.LOGGER.error("Unable to save configuration in config file")
        }
    }

    fun getMutedPlayers(): List<Mute?> {
        return muted
    }

    fun isPlayerMuted(uuid: UUID): Boolean {
        muted.removeIf {
            it.time < Instant.now().epochSecond
        }
        return muted.stream().anyMatch { it.uuid == uuid }
    }

    fun mute(player: GameProfile, unmuteTime: Long, reason: String) {
        val mute = Mute(reason, player.id, unmuteTime)
        muted.add(mute)
    }

    fun unmute(player: GameProfile) {
        muted.removeIf { it.uuid == player.id }
    }

    fun getMute(player: ServerPlayerEntity): Optional<Mute> {
        return muted.stream().filter { it.uuid == player.uuid }
            .findFirst()
    }

    fun serverMute(time: Long): Boolean {
        serverMute[time] = !serverMute.state
        return serverMute.state
    }

    fun getServerMute(): ServerMute {
        return serverMute
    }

    fun isMaintenanceMode(): Boolean {
        return maintenanceMode
    }

    fun setMaintenanceMode(maintenanceMode: Boolean) {
        Config.maintenanceMode = maintenanceMode
    }
}