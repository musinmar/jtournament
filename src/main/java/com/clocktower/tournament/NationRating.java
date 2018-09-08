package com.clocktower.tournament;

import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.match.MatchResult;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.clocktower.tournament.Logger.print;
import static com.clocktower.tournament.Logger.println;
import static com.clocktower.tournament.Logger.readln;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;

public class NationRating {
    public static final int SEASON_COUNT = 4;

    public NationRatingItem[] nation_rating = new NationRatingItem[Nation.COUNT];
    private List<Nation> nationRanking;

    public static class NationRatingItem {
        public double[] seasons = new double[SEASON_COUNT];
    }

    public NationRating() {
        Arrays.setAll(nation_rating, i -> new NationRatingItem());
    }

    public Nation getRankedNation(int rank) {
        return nationRanking.get(rank);
    }

    public void initDefault() {
        for (int i = 0; i < SEASON_COUNT; i++) {
            nation_rating[0].seasons[i] = 3.5;
            nation_rating[1].seasons[i] = 3;
            nation_rating[2].seasons[i] = 2.5;
            nation_rating[3].seasons[i] = 4.5;
            nation_rating[4].seasons[i] = 4;
        }
    }

    public void write(PrintWriter writer) {
        Arrays.stream(nation_rating)
                .flatMapToDouble(nationRatingItem -> Arrays.stream(nationRatingItem.seasons))
                .forEach(writer::println);
    }

    public void read(Scanner sc) {
        for (int i = 0; i < Nation.COUNT; i++) {
            for (int j = 0; j < SEASON_COUNT; j++) {
                nation_rating[i].seasons[j] = sc.nextDouble();
            }
        }
    }

    public void printNationTable() {
        println("Federations table");
        int index = 0;
        for (NationRatingItem item : nation_rating) {
            print(String.format("%d) %-11s", index + 1, Nation.fromId(index).getName()));
            Arrays.stream(item.seasons).forEach(d -> print(String.format("%7.2f", d)));
            println();
            ++index;
        }
    }

    public void calculateNationRankingsAndPrint() {
        double[] sums = Arrays.stream(nation_rating)
                .mapToDouble(item -> {
                    return Arrays.stream(item.seasons).sum();
                })
                .toArray();

        nationRanking = Arrays.stream(Nation.values())
                .sorted(comparingDouble((Nation nation) -> sums[nation.getId()]).reversed())
                .collect(toList());

        println("Start of season federations ranking:");
        println();
        for (int i = 0; i < nationRanking.size(); ++i) {
            Nation nation = nationRanking.get(i);
            println(String.format("%d) %-11s %7.2f", i + 1, nation.getName(), sums[nation.getId()]));
        }
        readln();
    }

    public void advanceYear() {
        for (int i = 0; i < Nation.COUNT; ++i) {
            for (int j = SEASON_COUNT - 1; j >= 1; --j) {
                nation_rating[i].seasons[j] = nation_rating[i].seasons[j - 1];
            }
            nation_rating[i].seasons[0] = 0;
        }
    }

    public void updateNationRatings(Player p1, Player p2, MatchResult mr, int points) {
        if (mr.rounds.r1 > mr.rounds.r2) {
            nation_rating[p1.getNation().getId()].seasons[0] = nation_rating[p1.getNation().getId()].seasons[0] + points;
        } else if (mr.rounds.r2 > mr.rounds.r1) {
            nation_rating[p2.getNation().getId()].seasons[0] = nation_rating[p2.getNation().getId()].seasons[0] + points;
        } else {
            nation_rating[p1.getNation().getId()].seasons[0] = nation_rating[p1.getNation().getId()].seasons[0] + points / 2.0;
            nation_rating[p2.getNation().getId()].seasons[0] = nation_rating[p2.getNation().getId()].seasons[0] + points / 2.0;
        }
    }

    public void normalizeCurrentYearRating() {
        nation_rating[nationRanking.get(0).getId()].seasons[0] /= 5;
        nation_rating[nationRanking.get(1).getId()].seasons[0] /= 5;
        nation_rating[nationRanking.get(2).getId()].seasons[0] /= 5;
        nation_rating[nationRanking.get(3).getId()].seasons[0] /= 4;
        nation_rating[nationRanking.get(4).getId()].seasons[0] /= 4;
    }
}
