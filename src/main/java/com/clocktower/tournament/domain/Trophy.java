package com.clocktower.tournament.domain;

import java.util.Objects;

public class Trophy {
    private final String name;
    private final int year;

    public Trophy(String name, int year) {
        this.name = name;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return name + " " + year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trophy trophy = (Trophy) o;
        return year == trophy.year &&
                Objects.equals(name, trophy.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, year);
    }

    public static Trophy valueOf(String s) {
        int i = s.lastIndexOf(' ');
        String name = s.substring(0, i);
        switch (name) {
            case "WC":
                name = "World Cup";
                break;
            case "FC":
                name = "Federations Cup";
                break;
            case "CL":
                name = "Champions League";
                break;
        }
        if (name.length() == 1) {
            name = "Division " + name;
        }
        int year = Integer.valueOf(s.substring(i + 1, s.length()));
        return new Trophy(name, year);
    }
}
