package com.clocktower.tournament.domain;

import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public enum Title {
    COMMON(""),
    SIR("Sir"),
    LORD("Lord");

    private String name;

    Title(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return !isEmpty(name) ? name + " " : "";
    }

    public static Title fromName(String name) {
        return Arrays.stream(values())
                .filter(v -> v.getName().equals(name))
                .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
