package com.clocktower.tournament;

public class Settings {
    public static boolean getIsPromptDisabled() {
        return "true".equals(System.getProperty("prompt.disabled"));
    }

    public static int getSimulatedSeasonCount() {
        String seasonCount = System.getProperty("season.count");
        return seasonCount != null ? Integer.valueOf(seasonCount) : 1;
    }
}
