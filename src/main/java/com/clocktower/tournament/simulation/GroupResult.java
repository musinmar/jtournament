package com.clocktower.tournament.simulation;

import com.clocktower.tournament.domain.Player;

public class GroupResult {
    public Player player;
    public int roundsWon;
    public int gamesWon;
    public int gamesLost;

    public GroupResult(Player player) {
        this.player = player;
    }
}
