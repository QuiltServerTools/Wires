package net.quiltservertools.wires.mute

import java.time.Instant

class ServerMute {
    private var enabled = false
    private var time: Long
    operator fun set(time: Long, state: Boolean) {
        this.time = time
        enabled = state
    }

    val state: Boolean
        get() = if (time != -1L) {
            enabled && time > Instant.now().epochSecond
        } else enabled

    init {
        time = -1
    }
}