package com.clocktower.tournament.simulation;

import com.clocktower.tournament.Player;

public class PlayerSeriesResult {
    private Player player1;
    private Player player2;
    private SimpleResult result = new SimpleResult();

    public PlayerSeriesResult(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public SimpleResult getResult() {
        return result;
    }

    public void setResult(SimpleResult result) {
        this.result = result;
    }

    public Player getWinner() {
        if (result.r1 > result.r2) {
            return player1;
        } else if (result.r1 < result.r2) {
            return player2;
        } else {
            throw new IllegalStateException();
        }
    }

    public Player getLoser() {
        return player1 == getWinner() ? player2 : player1;
    }
}
