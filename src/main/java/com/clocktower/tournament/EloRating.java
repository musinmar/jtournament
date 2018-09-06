package com.clocktower.tournament;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import static java.util.Comparator.comparingDouble;

public class EloRating {
    private static final double k_factor = 20;

    public EloRatingItem[] items = new EloRatingItem[30];

    public static class EloRatingItem {
        public Player player;
        public double opg;
        public double os;
    }

    public EloRating() {
    }

    public void init(Player[] players) {
        items = Arrays.stream(players)
                .map(p -> {
                    EloRatingItem item = new EloRatingItem();
                    item.player = p;
                    return item;
                })
                .toArray(EloRatingItem[]::new);

        Arrays.stream(items)
                .forEach(item -> {
                    resetPlayer(item.player);
                    item.opg = item.os;
                });

        sort();
    }

    public void save(PrintWriter writer) {
        Arrays.stream(items)
                .forEach(item -> {
                    writer.println(item.player.id);
                    writer.println(item.opg);
                    writer.println(item.os);
                });
    }

    public void load(Scanner sc, Player[] players) {
        int playerCount = players.length;
        items = new EloRatingItem[playerCount];
        for (int i = 0; i < playerCount; i++) {
            items[i] = new EloRatingItem();
            int k = sc.nextInt();
            items[i].player = players[k];
            items[i].opg = sc.nextDouble();
            items[i].os = sc.nextDouble();
        }
    }

    public void update(int id1, int id2, double r1, double r2) {
        int sl1 = findItem(id1);
        int sl2 = findItem(id2);
        double rat1 = items[sl1].os;
        double rat2 = items[sl2].os;
        items[sl1].os = items[sl1].os + ratingDif(rat1, rat2, r1);
        items[sl2].os = items[sl2].os + ratingDif(rat2, rat1, r2);
    }

    public int playerIsBetterThan(int id1, int id2) {
        int sl1 = findItem(id1);
        int sl2 = findItem(id2);
        return sl2 - sl1;
    }

    public void sortPlayers(int[] players) {
        int len = players.length;
        int[] sorted = new int[len];
        int k = 0;
        for (int i = 0; i < 30; ++i) {
            for (int j = 0; j < len; ++j) {
                if (items[i].player.id == players[j]) {
                    sorted[k] = players[j];
                    k = k + 1;
                    break;
                }

            }
            if (k >= len) {
                break;
            }
        }

        for (int i = 0; i < len; ++i) {
            players[i] = sorted[i];
        }
    }

    public void sort() {
        Arrays.sort(items, comparingDouble((EloRatingItem item) -> item.os).reversed());
    }

    public void print(PrintWriter writer, boolean withDifs) {
        int maxNameLength = Arrays.stream(items)
                .map(item -> item.player.getPlayerName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%-2d: %-" + (maxNameLength + 1) + "s %-7.2f";
        for (int i = 0; i < 30; i++) {
            writer.print(String.format(formatString, (i + 1), items[i].player.getPlayerName(), items[i].os));
            if (withDifs) {
                writer.println(String.format("   %+5.2f", items[i].os - items[i].opg));
            } else {
                writer.println();
            }
        }
    }

    public void advanceYear() {
        for (int i = 0; i < 30; ++i) {
            items[i].opg = items[i].os;
        }
    }

    public void resetPlayer(Player player) {
        int i = findItem(player);
        //items[i].os = 500 + (items[i].player.level - 5) * 50;
        items[i].os = 500;
    }

    private int findItem(int id) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].player.id == id) {
                return i;
            }
        }
        throw new IllegalArgumentException("Player with such ID is not found");
    }

    private int findItem(Player player) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].player == player) {
                return i;
            }
        }
        throw new IllegalArgumentException("Player is not found");
    }

    private double ratingDif(double rat1, double rat2, double res) {
        double dif = rat2 - rat1;
        double e = 1 / (1 + Math.exp(dif / 400 * Math.log(10)));
        double dif2 = k_factor * (res - e);
        return dif2;
    }
}
