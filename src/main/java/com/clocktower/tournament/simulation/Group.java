package com.clocktower.tournament.simulation;

import com.clocktower.tournament.EloRating;
import com.clocktower.tournament.domain.Player;

import java.util.List;

import static com.clocktower.tournament.utils.Logger.println;
import static com.clocktower.tournament.utils.Logger.readln;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.toList;

public class Group {
    private String name;
    private List<GroupResult> groupResults;
    private EloRating eloRating;

    public Group(String name, List<Player> players, EloRating eloRating) {
        this.name = name;
        groupResults = players.stream().map(GroupResult::new).collect(toList());
        this.eloRating = eloRating;
    }

    public List<GroupResult> getGroupResults() {
        return groupResults;
    }

    public GroupResult getGroupResult(int index) {
        return groupResults.get(index);
    }

    public void applyMatchResult(int index1, int index2, MatchResult<Player> matchResult) {
        GroupResult groupResult1 = groupResults.get(index1);
        groupResult1.roundsWon += matchResult.rounds.r1;
        groupResult1.gamesWon += matchResult.games.r1;
        groupResult1.gamesLost += matchResult.games.r2;
        GroupResult groupResult2 = groupResults.get(index2);
        groupResult2.roundsWon += matchResult.rounds.r2;
        groupResult2.gamesWon += matchResult.games.r2;
        groupResult2.gamesLost += matchResult.games.r1;
    }

    public void printGroupResults() {
        List<GroupResult> groupResultsCopy = newArrayList(groupResults);
        sortGroupResults(groupResultsCopy);
        printGroupResults(name, groupResultsCopy);
    }

    public List<Player> getResult() {
        List<GroupResult> groupResultsCopy = newArrayList(groupResults);
        sortGroupResults(groupResultsCopy);
        return groupResultsCopy.stream().map(groupResult -> groupResult.player).collect(toList());
    }

    private void sortGroupResults(List<GroupResult> results) {
        results.sort(reverseOrder(this::compareGroupResults));
    }

    private int compareGroupResults(GroupResult res1, GroupResult res2) {
        if (res1.roundsWon > res2.roundsWon) {
            return 1;
        } else if (res1.roundsWon < res2.roundsWon) {
            return -1;
        } else {
            int dif1 = res1.gamesWon - res1.gamesLost;
            int dif2 = res2.gamesWon - res2.gamesLost;
            if (dif1 > dif2) {
                return 1;
            } else if (dif1 < dif2) {
                return -1;
            } else {
                return eloRating.comparePlayersByRating(res1.player, res2.player);
            }
        }
    }

    private void printGroupResults(String name, List<GroupResult> results) {
        println(name);
        int len = results.size();
        int maxNameLength = results.stream()
                .map(r -> r.player.getPlayerName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%d. %-" + (maxNameLength + 1) + "s %2d:%2d  %d";
        for (int i = 0; i < len; i++) {
            GroupResult r = results.get(i);
            println(String.format(formatString, (i + 1), r.player.getPlayerName(),
                    r.gamesWon, r.gamesLost, r.roundsWon));
        }
        readln();
    }
}
