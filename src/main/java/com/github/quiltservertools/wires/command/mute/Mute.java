package com.github.quiltservertools.wires.command.mute;

import java.util.UUID;

public class Mute {
    private final long time;
    private final UUID uuid;
    private final String reason;

    public Mute(String reason, UUID uuid, long time){
        this.reason = reason;
        this.time = time;
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public long getTime() {
        return this.time;
    }

    public String getReason() {
        return this.reason.equals("") ? this.reason : "<None provided>";
    }
}
