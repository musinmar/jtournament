package com.clocktower.tournament.simulation;

import com.clocktower.tournament.domain.Player;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Team {
    private final String name;
    private final List<Player> players;

    public Team(String name, List<Player> players) {
        this.name = name;
        this.players = newArrayList(players);
    }

    public String getName() {
        return name;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
