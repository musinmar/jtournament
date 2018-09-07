package com.clocktower.tournament.utils;

public class RandomUtils {
    public static int random(int endExclusive) {
        return org.apache.commons.lang3.RandomUtils.nextInt(0, endExclusive);
    }

    public static double random() {
        return org.apache.commons.lang3.RandomUtils.nextDouble(0, 1);
    }
}
