package com.clocktower.tournament;

import com.clocktower.tournament.dto.EloRatingDto;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;

public class EloRating {
    private static final double K_FACTOR = 20;
    private static final double AVERAGE_RATING = 500;

    private List<Item> items;

    private static class Item {
        Player player;
        double points;
        double pointsLastYear;

        Item(Player player) {
            this.player = player;
        }

        EloRatingDto.ItemDto toDto() {
            EloRatingDto.ItemDto itemDto = new EloRatingDto.ItemDto();
            itemDto.setPlayerId(player.id);
            itemDto.setPoints(points);
            itemDto.setPointsLastYear(pointsLastYear);
            return itemDto;
        }

        static Item fromDto(EloRatingDto.ItemDto itemDto, Player[] players) {
            Item item = new Item(players[itemDto.getPlayerId()]);
            item.points = itemDto.getPoints();
            item.pointsLastYear = itemDto.getPointsLastYear();
            return item;
        }
    }

    public EloRating() {
    }

    public void init(Player[] players) {
        items = Arrays.stream(players)
                .map(Item::new)
                .collect(toList());

        items.forEach(item -> {
            item.points = AVERAGE_RATING;
        });

        advanceYear();
    }

    public EloRatingDto toDto() {
        EloRatingDto eloRatingDto = new EloRatingDto();
        eloRatingDto.setItems(items.stream().map(Item::toDto).collect(toList()));
        return eloRatingDto;
    }

    public static EloRating fromDto(EloRatingDto eloRatingDto, Player[] players) {
        EloRating eloRating = new EloRating();
        eloRating.items = eloRatingDto.getItems().stream().map(i -> Item.fromDto(i, players)).collect(toList());
        eloRating.normalize();
        eloRating.advanceYear();
        return eloRating;
    }

    private void normalize() {
        double ratingSum = items.stream().mapToDouble(i -> i.points).sum();
        double dif = ratingSum - AVERAGE_RATING * items.size();
        if (Math.abs(dif) > 1) {
            items.forEach(i -> i.points -= dif / items.size());
        }
    }

    public List<Player> getPlayersByRating() {
        return items.stream()
                .sorted(comparingDouble((Item item) -> item.points).reversed())
                .map(item -> item.player)
                .collect(toList());
    }

    public void update(int id1, int id2, double r1, double r2) {
        //TODO: replace id with player
        Item item1 = items.get(id1);
        Item item2 = items.get(id2);
        double rat1 = item1.points;
        double rat2 = item2.points;
        item1.points += calculateRatingChange(rat1, rat2, r1);
        item2.points += calculateRatingChange(rat2, rat1, r2);
    }

    public int playerIsBetterThan(Player p1, Player p2) {
        return (int) Math.signum(items.get(p1.id).points - items.get(p2.id).points);
    }

    public void sortPlayers(List<Integer> players) {
        players.sort(comparingDouble((Integer id) -> items.get(id).points).reversed());
    }

    public void print(PrintWriter writer, boolean withDifs) {
        int maxNameLength = items.stream()
                .map(item -> item.player.getPlayerName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%-2d: %-" + (maxNameLength + 1) + "s %-7.2f";

        List<Item> sortedItems = items.stream()
                .sorted(comparingDouble((Item item) -> item.points).reversed())
                .collect(toList());

        for (int i = 0; i < sortedItems.size(); i++) {
            Item item = sortedItems.get(i);
            writer.print(String.format(formatString, (i + 1), item.player.getPlayerName(), item.points));
            if (withDifs) {
                writer.println(String.format("   %+5.2f", item.points - item.pointsLastYear));
            } else {
                writer.println();
            }
        }
    }

    public void advanceYear() {
        items.forEach(item -> item.pointsLastYear = item.points);
    }

    public void resetPlayer(Player player) {
        double[][] data = items.stream()
                .map(item -> new double[]{item.player.getLevel(), item.points})
                .toArray(double[][]::new);

        SimpleRegression simpleRegression = new SimpleRegression();
        simpleRegression.addData(data);

        Item item = items.get(player.id);
        item.points = simpleRegression.getIntercept() + simpleRegression.getSlope() * player.getLevel();
        normalize();
    }

    private static double calculateRatingChange(double rat1, double rat2, double res) {
        double dif = rat2 - rat1;
        double e = 1 / (1 + Math.exp(dif / 400 * Math.log(10)));
        return K_FACTOR * (res - e);
    }
}
