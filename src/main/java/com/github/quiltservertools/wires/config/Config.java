package com.github.quiltservertools.wires.config;

import com.github.quiltservertools.wires.StaffChat;
import com.github.quiltservertools.wires.Wires;
import com.github.quiltservertools.wires.command.mute.Mute;
import com.github.quiltservertools.wires.command.mute.ServerMute;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Config {
    /*
    Contains config values from the JSON config file
     */

    public JsonObject json;
    private List<Mute> muted;
    private final ServerMute serverMute;
    private final StaffChat staffChat;

    public Config() {
        String path = FabricLoader.getInstance().getConfigDir().toString() + "/wires.json";
        try {
            this.json = new JsonParser().parse(new String(Files.readAllBytes(Paths.get(path)))).getAsJsonObject();
            muted = initMuted();
            Wires.LOGGER.info("Loaded Wires config");
        } catch (IOException e) {
            muted = new ArrayList<>();
            Wires.LOGGER.info("Unable to find existing Wires config file");
            Wires.LOGGER.info("A config file will be created on server shutdown");
        }
        this.serverMute = new ServerMute();
        this.staffChat = new StaffChat();

    }

    private List<Mute> initMuted() {
        JsonArray muted = json.get("muted").getAsJsonArray();
        List<Mute> list = new ArrayList<>();
        muted.forEach(jsonElement -> {
            JsonObject json = jsonElement.getAsJsonObject();
            list.add(new Mute(json.get("reason").getAsString(), UUID.fromString(json.get("uuid").getAsString()), json.get("time").getAsLong()));
        });
        return list;
    }

    public void shutdown() {
        JsonObject json = new JsonObject();

        // Saving for mutes
        JsonArray muteArray = new JsonArray();
        // Don't save finished mutes
        muted.removeIf(mute -> mute.getTime() < Instant.now().getEpochSecond());
        muted.forEach(mute -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("uuid", new JsonPrimitive(mute.getUuid().toString()));
                    jsonObject.add("time", new JsonPrimitive(mute.getTime()));
                    jsonObject.add("reason", new JsonPrimitive(mute.getReason()));
                    muteArray.add(jsonObject);
                }
        );
        json.add("muted", muteArray);

        // Saving for other config options

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.write(Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "\\wires.json"), gson.toJson(json).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Wires.LOGGER.error("Unable to save configuration in config file");
        }

    }

    public List<Mute> getMutedPlayers() {
        return this.muted;
    }

    public boolean isPlayerMuted(UUID uuid) {
        muted.removeIf(mute -> mute.getTime() < Instant.now().getEpochSecond());
        return muted.stream().anyMatch(mute -> mute.getUuid().equals(uuid));
    }

    public void mute(GameProfile player, long unmuteTime, String reason) {
        Mute mute = new Mute(reason, player.getId(), unmuteTime);
        muted.add(mute);
    }

    public Optional<Mute> getMute(ServerPlayerEntity player) {
        return muted.stream().filter(mute -> mute.getUuid().equals(player.getUuid())).findFirst();
    }

    public boolean serverMute(long time) {
        serverMute.set(time, !serverMute.getState());
        return serverMute.getState();
    }

    public ServerMute getServerMute() {
        return this.serverMute;
    }

    public StaffChat getStaffChat() {
        return this.staffChat;
    }
}
