package net.quiltservertools.wires.mute

import java.util.*

data class Mute(var reason: String, val uuid: UUID, val time: Long)
