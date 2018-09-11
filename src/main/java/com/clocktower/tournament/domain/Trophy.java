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
}
