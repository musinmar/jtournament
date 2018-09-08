package com.clocktower.tournament.match;

public class MatchResult {
    public int id1;
    public int id2;
    public int rw1;
    public int rw2;
    public int gw1;
    public int gw2;

    public MatchResult(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public void addRoundResult(SimpleResult r) {
        gw1 += r.r1;
        gw2 += r.r2;
        if (r.r1 > r.r2) {
            rw1 += 2;
        } else if (r.r2 > r.r1) {
            rw2 += 2;
        } else {
            rw1 += 1;
            rw2 += 1;
        }
    }
}
