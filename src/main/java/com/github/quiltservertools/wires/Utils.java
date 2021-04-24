package com.github.quiltservertools.wires;

import java.time.Duration;
import java.time.Instant;

public class Utils {
    public static long parseTime(String input) {
        Duration duration = Duration.ZERO;

        KeyValuePair<Integer, String> timePair;
        // Parse years
        timePair = locateCharAndReplace(input, 'y');
        duration = duration.plus(Duration.ofDays(365L * timePair.key));
        input = timePair.value;

        // Parse weeks
        timePair = locateCharAndReplace(input, 'w');
        duration = duration.plus(Duration.ofDays(7L * timePair.key));
        input = timePair.value;

        // Parse days
        timePair = locateCharAndReplace(input, 'd');
        duration = duration.plus(Duration.ofDays(timePair.key));
        input = timePair.value;

        // Parse hours
        timePair = locateCharAndReplace(input, 'h');
        duration = duration.plus(Duration.ofHours(timePair.key));
        input = timePair.value;

        // Parse minutes
        timePair = locateCharAndReplace(input, 'm');
        duration = duration.plus(Duration.ofMinutes(timePair.key));
        input = timePair.value;

        // Parse seconds
        timePair = locateCharAndReplace(input, 's');
        duration = duration.plus(Duration.ofSeconds(timePair.key));
        input = timePair.value;


        return Instant.now().plus(duration).getEpochSecond();
    }

    private static KeyValuePair<Integer, String> locateCharAndReplace(String string, char c) {
        int indexOfChar = string.indexOf(c);
        // If not found return
        if (indexOfChar == -1) return new KeyValuePair<>(0, string);
        // Find the string before the char
        String firstValue = string.substring(0, indexOfChar);
        // Now find the value afterwards
        string = string.substring(indexOfChar + 1);
        //Return the number and the remaining string
        return new KeyValuePair<>(Integer.valueOf(firstValue), string);
    }

    public static class KeyValuePair<K, V> {
        public K key;
        public V value;

        public KeyValuePair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
