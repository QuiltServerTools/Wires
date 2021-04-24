package com.github.quiltservertools.wires;

import com.github.quiltservertools.wires.command.MaintenanceModeCommand;
import com.github.quiltservertools.wires.command.VanishCommand;
import com.github.quiltservertools.wires.command.mute.MuteCommand;
import com.github.quiltservertools.wires.config.Config;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Wires implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("Wires");
    public static Config config;

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            MaintenanceModeCommand.register(dispatcher);
            VanishCommand.register(dispatcher);
            MuteCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
            VanishCommand.getInstance().getPlayers().forEach(player -> VanishCommand.getInstance().addOrRemove(player));
            config.shutdown();
        }));

        config = new Config();
    }
}