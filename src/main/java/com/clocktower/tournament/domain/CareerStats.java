package com.clocktower.tournament.domain;

import com.clocktower.tournament.simulation.SimpleResult;

public class CareerStats {
    private int played;
    private int wins;
    private int draws;
    private int losses;

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void applyMatchResult(SimpleResult r) {
        ++played;
        if (r.r1 > r.r2) {
            ++wins;
        } else if (r.r1 < r.r2) {
            ++losses;
        } else {
            ++draws;
        }
    }
}
