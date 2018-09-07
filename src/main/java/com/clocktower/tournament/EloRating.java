package com.clocktower.tournament;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;

public class EloRating {
    private static final double k_factor = 20;

    private List<EloRatingItem> itemsList;

    public static class EloRatingItem {
        public Player player;
        public double opg;
        public double os;
    }

    public EloRating() {
    }

    public void init(Player[] players) {

        itemsList = Arrays.stream(players)
                .map(p -> {
                    EloRatingItem item = new EloRatingItem();
                    item.player = p;
                    return item;
                })
                .collect(toList());

        itemsList.forEach(item -> {
            resetPlayer(item.player);
            item.opg = item.os;
        });
    }

    public void save(PrintWriter writer) {
        itemsList.forEach(item -> {
            writer.println(item.player.id);
            writer.println(item.opg);
            writer.println(item.os);
        });
    }

    public void load(Scanner sc, Player[] players) {
        int playerCount = players.length;
        itemsList = Arrays.stream(players)
                .map(p -> {
                    EloRatingItem item = new EloRatingItem();
                    item.player = p;
                    return item;
                })
                .collect(toList());

        for (int i = 0; i < playerCount; i++) {
            int k = sc.nextInt();
            EloRatingItem item = itemsList.get(k);
            item.opg = sc.nextDouble();
            item.os = sc.nextDouble();
        }
    }

    public List<Player> getPlayersByRating() {
        return itemsList.stream()
                .sorted(comparingDouble((EloRatingItem item) -> item.os).reversed())
                .map(item -> item.player)
                .collect(toList());
    }

    public void update(int id1, int id2, double r1, double r2) {
        EloRatingItem item1 = itemsList.get(id1);
        EloRatingItem item2 = itemsList.get(id2);
        double rat1 = item1.os;
        double rat2 = item2.os;
        item1.os += ratingDif(rat1, rat2, r1);
        item2.os += ratingDif(rat2, rat1, r2);
    }

    public int playerIsBetterThan(int id1, int id2) {
        return (int) Math.signum(itemsList.get(id1).os - itemsList.get(id2).os);
    }

    public void sortPlayers(int[] players) {
        int[] sortedPlayers = Arrays.stream(players)
                .boxed()
                .sorted(comparingDouble((Integer id) -> itemsList.get(id).os).reversed())
                .mapToInt(i -> i)
                .toArray();
        System.arraycopy(sortedPlayers, 0, players, 0, players.length);
    }

    public void print(PrintWriter writer, boolean withDifs) {
        int maxNameLength = itemsList.stream()
                .map(item -> item.player.getPlayerName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%-2d: %-" + (maxNameLength + 1) + "s %-7.2f";

        List<EloRatingItem> sortedItems = itemsList.stream()
                .sorted(comparingDouble((EloRatingItem item) -> item.os).reversed())
                .collect(toList());

        for (int i = 0; i < sortedItems.size(); i++) {
            EloRatingItem item = sortedItems.get(i);
            writer.print(String.format(formatString, (i + 1), item.player.getPlayerName(), item.os));
            if (withDifs) {
                writer.println(String.format("   %+5.2f", item.os - item.opg));
            } else {
                writer.println();
            }
        }
    }

    public void advanceYear() {
        itemsList.forEach(item -> item.opg = item.os);
    }

    public void resetPlayer(Player player) {
        EloRatingItem item = itemsList.get(player.id);
        //items[i].os = 500 + (items[i].player.level - 5) * 50;
        item.os = 500;
    }

    private double ratingDif(double rat1, double rat2, double res) {
        double dif = rat2 - rat1;
        double e = 1 / (1 + Math.exp(dif / 400 * Math.log(10)));
        double dif2 = k_factor * (res - e);
        return dif2;
    }
}
