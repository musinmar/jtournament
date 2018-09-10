package com.clocktower.tournament;

import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.match.MatchResult;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.clocktower.tournament.Logger.print;
import static com.clocktower.tournament.Logger.println;
import static com.clocktower.tournament.Logger.readln;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;

public class NationRating {
    private static final int SEASON_COUNT = 4;

    private Map<Nation, PointHistoryItem> pointHistory = new HashMap<>();
    private List<Nation> ranking;
    private Map<Nation, MutableDouble> seasonPoints = new HashMap<>();

    private static class PointHistoryItem {
        double[] seasons = new double[SEASON_COUNT];

        PointHistoryItem(double defaultPoints) {
            for (int i = 0; i < SEASON_COUNT; i++) {
                seasons[i] = defaultPoints;
            }
        }
    }

    public NationRating() {
        for (Nation nation : Nation.values()) {
            seasonPoints.put(nation, new MutableDouble(0));
        }
    }

    public Nation getRankedNation(int rank) {
        return ranking.get(rank);
    }

    public void initDefault() {
        pointHistory.put(Nation.ALMAGEST, new PointHistoryItem(3.5));
        pointHistory.put(Nation.BELLEROFON, new PointHistoryItem(3));
        pointHistory.put(Nation.GALILEO, new PointHistoryItem(2.5));
        pointHistory.put(Nation.KAMELEOPARD, new PointHistoryItem(4.5));
        pointHistory.put(Nation.OBERON_22, new PointHistoryItem(4));
    }

    public void write(PrintWriter writer) {
        Arrays.stream(Nation.values())
                .map(pointHistory::get)
                .flatMapToDouble(pointHistoryItem -> Arrays.stream(pointHistoryItem.seasons))
                .forEach(writer::println);
    }

    public void read(Scanner sc) {
        for (Nation nation : Nation.values()) {
            PointHistoryItem pointHistoryItem = new PointHistoryItem(0);
            for (int j = 0; j < SEASON_COUNT; j++) {
                pointHistoryItem.seasons[j] = sc.nextDouble();
            }
            pointHistory.put(nation, pointHistoryItem);
        }
    }

    public void printPointHistory() {
        println("Federations table");
        println();
        int index = 0;
        for (Nation nation : Nation.values()) {
            PointHistoryItem pointHistoryItem = pointHistory.get(nation);
            print(String.format("%d) %-11s", index + 1, nation.getName()));
            Arrays.stream(pointHistoryItem.seasons).forEach(d -> print(String.format("%7.2f", d)));
            println();
            ++index;
        }
        println();
    }

    public void calculateRankingsAndPrint() {
        double[] sums = Arrays.stream(Nation.values())
                .map(pointHistory::get)
                .mapToDouble(item -> {
                    return Arrays.stream(item.seasons).sum();
                })
                .toArray();

        ranking = Arrays.stream(Nation.values())
                .sorted(comparingDouble((Nation nation) -> sums[nation.getId()]).reversed())
                .collect(toList());

        println("Start of season federations ranking:");
        println();
        for (int i = 0; i < ranking.size(); ++i) {
            Nation nation = ranking.get(i);
            println(String.format("%d) %-11s %7.2f", i + 1, nation.getName(), sums[nation.getId()]));
        }
        readln();
    }

    public void advanceYear() {
        int[] countCoefficient = new int[]{5, 5, 5, 4, 4};
        for (int i = 0; i < Nation.COUNT; i++) {
            Nation nation = getRankedNation(i);
            PointHistoryItem item = pointHistory.get(nation);
            for (int j = SEASON_COUNT - 1; j >= 1; --j) {
                item.seasons[j] = item.seasons[j - 1];
            }
            item.seasons[0] = seasonPoints.get(nation).doubleValue() / countCoefficient[i];
        }
    }

    public void updateRatings(Player p1, Player p2, MatchResult mr, int points) {
        if (mr.rounds.r1 > mr.rounds.r2) {
            addSeasonPoints(p1.getNation(), points);
        } else if (mr.rounds.r2 > mr.rounds.r1) {
            addSeasonPoints(p2.getNation(), points);
        } else {
            addSeasonPoints(p1.getNation(), points / 2.0);
            addSeasonPoints(p2.getNation(), points / 2.0);
        }
    }

    private void addSeasonPoints(Nation nation, double points) {
        seasonPoints.get(nation).add(points);
    }
}
