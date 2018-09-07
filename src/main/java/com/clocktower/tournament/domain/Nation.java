package com.clocktower.tournament.domain;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public enum Nation {
    ALMAGEST("Almagest"),
    BELLEROFON("Bellerofon"),
    GALILEO("Galileo"),
    KAMELEOPARD("Kameleopard"),
    OBERON_22("Oberon-22");

    private String name;

    Nation(String name) {
        this.name = name;
    }

    public static Nation fromName(String name) {
        return Arrays.stream(values())
                .filter(v -> v.getName().equals(name))
                .findAny().orElseThrow(IllegalArgumentException::new);
    }

    public static Nation fromId(int id) {
        return values()[id];
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return ArrayUtils.indexOf(values(), this);
    }
}
