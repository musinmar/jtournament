package com.clocktower.tournament.simulation;

public class MatchResult<T> {
    private final T p1;
    private final T p2;
    public final SimpleResult rounds = new SimpleResult();
    public final SimpleResult games = new SimpleResult();

    public MatchResult(T p1, T p2) {
        this.p1 = p1;
        this.p2 = p2;
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

    public T getWinner() {
        if (rounds.r1 > rounds.r2) {
            return p1;
        } else if (rounds.r1 < rounds.r2) {
            return p2;
        } else {
            throw new IllegalStateException();
        }
    }

    public T getLoser() {
        return p1 == getWinner() ? p2 : p1;
    }
}
