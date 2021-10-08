package net.quiltservertools.wires

import java.time.Duration
import java.time.Instant

object Utils {
    fun parseTime(string: String): Long {
        var input = string
        var duration = Duration.ZERO
        // Parse years
        var timePair: KeyValuePair<Int, String> = locateCharAndReplace(input, 'y')
        duration = duration.plus(Duration.ofDays(365L * timePair.key))
        input = timePair.value

        // Parse weeks
        timePair = locateCharAndReplace(input, 'w')
        duration = duration.plus(Duration.ofDays(7L * timePair.key))
        input = timePair.value

        // Parse days
        timePair = locateCharAndReplace(input, 'd')
        duration = duration.plus(Duration.ofDays(timePair.key.toLong()))
        input = timePair.value

        // Parse hours
        timePair = locateCharAndReplace(input, 'h')
        duration = duration.plus(Duration.ofHours(timePair.key.toLong()))
        input = timePair.value

        // Parse minutes
        timePair = locateCharAndReplace(input, 'm')
        duration = duration.plus(Duration.ofMinutes(timePair.key.toLong()))
        input = timePair.value

        // Parse seconds
        timePair = locateCharAndReplace(input, 's')
        duration = duration.plus(Duration.ofSeconds(timePair.key.toLong()))
        return Instant.now().plus(duration).epochSecond
    }

    private fun locateCharAndReplace(string: String, c: Char): KeyValuePair<Int, String> {
        val indexOfChar = string.indexOf(c)
        // If not found return
        if (indexOfChar == -1) return KeyValuePair(0, string)
        // Find the string before the char
        val firstValue = string.substring(0, indexOfChar)
        // Now find the value afterwards
        val text = string.substring(indexOfChar + 1)
        //Return the number and the remaining string
        return KeyValuePair(Integer.valueOf(firstValue), text)
    }

    class KeyValuePair<K, V>(var key: K, var value: V)
}