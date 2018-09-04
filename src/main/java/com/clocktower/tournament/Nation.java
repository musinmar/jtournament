package com.clocktower.tournament;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Optional;

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
        Optional<Nation> faction = Arrays.stream(values())
                .filter(v -> v.getName().equals(name))
                .findAny();
        return faction.orElseThrow(IllegalArgumentException::new);
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
