package com.clocktower.tournament.domain;

import java.util.Objects;

public class Trophy {
    private String name;
    private int year;

    public Trophy() {
    }

    public Trophy(String name, int year) {
        this.name = name;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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

    public static int getValue(String name) {
        switch (name) {
            case "World Cup":
                return 10;
            case "Division A":
                return 9;
            case "Golden Cup":
                return 8;
            case "Champions League":
                return 7;
            case "National World Cup":
                return 6;
            case "Federations Cup":
                return 4;
            case "Division B":
                return 3;
            case "Cup of Almagest":
            case "Cup of Bellerofon":
            case "Cup of Galileo":
            case "Cup of Kameleopard":
            case "Cup of Oberon-22":
                return 2;
            case "Division C":
                return -3;
            case "Division D":
                return -4;
        }
        throw new IllegalStateException("Unknown trophy: " + name);
    }
}
