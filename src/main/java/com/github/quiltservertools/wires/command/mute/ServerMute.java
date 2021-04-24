package com.github.quiltservertools.wires.command.mute;

import java.time.Instant;

public class ServerMute {
    private boolean enabled;
    private long time;
    public ServerMute() {
        enabled = false;
        time = -1;
    }

    public void set(long time, boolean state) {
        this.time = time;
        enabled = state;
    }

    public boolean getState() {
        if (time != -1) {
            return this.enabled && time > Instant.now().getEpochSecond();
        }
        return this.enabled;
    }
}
