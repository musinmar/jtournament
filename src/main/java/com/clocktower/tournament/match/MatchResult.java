package com.clocktower.tournament.match;

public class MatchResult {
    public int id1;
    public int id2;
    public final SimpleResult rounds = new SimpleResult();
    public final SimpleResult games = new SimpleResult();

    public MatchResult(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public void addRoundResult(SimpleResult r, boolean additionalTime) {
        games.r1 += r.r1;
        games.r2 += r.r2;
        if (r.r1 > r.r2) {
            rounds.r1 += additionalTime ? 1 : 2;
        } else if (r.r2 > r.r1) {
            rounds.r2 += additionalTime ? 1 : 2;
        } else {
            rounds.r1 += 1;
            rounds.r2 += 1;
        }
    }

    public void addSubMatchResult(SimpleResult r) {
        if (r.r1 > r.r2) {
            rounds.r1 += 1;
        } else {
            rounds.r2 += 1;
        }
        games.r1 += r.r1;
        games.r2 += r.r2;
    }
}
