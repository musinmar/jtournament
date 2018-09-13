package com.clocktower.tournament.simulation;

import com.clocktower.tournament.Player;

public class MatchResult {
    public final Player player1;
    public final Player player2;
    public final SimpleResult rounds = new SimpleResult();
    public final SimpleResult games = new SimpleResult();

    public MatchResult(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
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

    public Player getWinner() {
        if (rounds.r1 > rounds.r2) {
            return player1;
        } else if (rounds.r1 < rounds.r2) {
            return player2;
        } else {
            throw new IllegalStateException();
        }
    }

    public Player getLoser() {
        return player1 == getWinner() ? player2 : player1;
    }
}
