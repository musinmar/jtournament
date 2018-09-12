package com.clocktower.tournament.simulation;

public class SimpleResult {
    public int r1;
    public int r2;

    @Override
    public String toString() {
        return r1 + ":" + r2;
    }

    public int getTopScore() {
        return Math.max(r1, r2);
    }
}
