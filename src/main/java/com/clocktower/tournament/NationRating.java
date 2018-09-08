package com.clocktower.tournament;

import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.match.MatchResult;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import static com.clocktower.tournament.Logger.print;
import static com.clocktower.tournament.Logger.println;
import static com.clocktower.tournament.Logger.readln;
import static java.util.Comparator.comparingDouble;

public class NationRating {
    public static final int SEASON_COUNT = 4;

    public NationRatingItem[] nation_rating = new NationRatingItem[Nation.COUNT];
    public int[] nation_pos = new int[Nation.COUNT];

    public static class NationRatingItem {
        public double[] seasons = new double[SEASON_COUNT];
    }

    public NationRating() {
        Arrays.setAll(nation_rating, i -> new NationRatingItem());
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

    public void makeSeasonNationPos() {
        double[] sums = Arrays.stream(nation_rating)
                .mapToDouble(item -> {
                    return Arrays.stream(item.seasons).sum();
                })
                .toArray();

        Arrays.setAll(nation_pos, i -> i);
        nation_pos = Arrays.stream(nation_pos)
                .boxed()
                .sorted(comparingDouble((Integer i) -> sums[i]).reversed())
                .mapToInt(i -> i)
                .toArray();

        println("Start of season federations ranking:");
        println();
        for (int i = 0; i < nation_pos.length; ++i) {
            int nationId = nation_pos[i];
            println(String.format("%d) %-11s %7.2f", i + 1, Nation.fromId(nationId).getName(), sums[nationId]));
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
        nation_rating[nation_pos[0]].seasons[0] = nation_rating[nation_pos[0]].seasons[0] / 5;
        nation_rating[nation_pos[1]].seasons[0] = nation_rating[nation_pos[1]].seasons[0] / 5;
        nation_rating[nation_pos[2]].seasons[0] = nation_rating[nation_pos[2]].seasons[0] / 5;
        nation_rating[nation_pos[3]].seasons[0] = nation_rating[nation_pos[3]].seasons[0] / 4;
        nation_rating[nation_pos[4]].seasons[0] = nation_rating[nation_pos[4]].seasons[0] / 4;
    }
}
