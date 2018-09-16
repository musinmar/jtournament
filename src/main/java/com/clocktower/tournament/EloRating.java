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
        double rating;
        double ratingLastYear;

        Item(Player player) {
            this.player = player;
        }

        EloRatingDto.ItemDto toDto() {
            EloRatingDto.ItemDto itemDto = new EloRatingDto.ItemDto();
            itemDto.setPlayerId(player.id);
            itemDto.setPoints(rating);
            return itemDto;
        }

        static Item fromDto(EloRatingDto.ItemDto itemDto, List<Player> players) {
            Item item = new Item(players.get(itemDto.getPlayerId()));
            item.rating = itemDto.getPoints();
            item.ratingLastYear = itemDto.getPoints();
            return item;
        }
    }

    public EloRating() {
    }

    public void init(List<Player> players) {
        items = players.stream()
                .map(Item::new)
                .collect(toList());

        items.forEach(item -> {
            item.rating = AVERAGE_RATING;
            item.ratingLastYear = AVERAGE_RATING;
        });
    }

    public EloRatingDto toDto() {
        EloRatingDto eloRatingDto = new EloRatingDto();
        eloRatingDto.setItems(items.stream().map(Item::toDto).collect(toList()));
        return eloRatingDto;
    }

    public static EloRating fromDto(EloRatingDto eloRatingDto, List<Player> players) {
        EloRating eloRating = new EloRating();
        eloRating.items = eloRatingDto.getItems().stream().map(i -> Item.fromDto(i, players)).collect(toList());
        eloRating.normalize();
        eloRating.savePointsAsLastYearPoints();
        return eloRating;
    }

    private void normalize() {
        double ratingSum = items.stream().mapToDouble(i -> i.rating).sum();
        double dif = ratingSum - AVERAGE_RATING * items.size();
        if (Math.abs(dif) > 1) {
            items.forEach(i -> i.rating -= dif / items.size());
        }
    }

    public List<Player> getPlayersByRating() {
        return items.stream()
                .sorted(comparingDouble((Item item) -> item.rating).reversed())
                .map(item -> item.player)
                .collect(toList());
    }

    public void update(int id1, int id2, double r1, double r2) {
        //TODO: replace id with player
        Item item1 = items.get(id1);
        Item item2 = items.get(id2);
        double rat1 = item1.rating;
        double rat2 = item2.rating;
        item1.rating += calculateRatingChange(rat1, rat2, r1);
        item2.rating += calculateRatingChange(rat2, rat1, r2);
    }

    public int playerIsBetterThan(Player p1, Player p2) {
        return (int) Math.signum(items.get(p1.id).rating - items.get(p2.id).rating);
    }

    public void sortPlayers(List<Player> players) {
        players.sort(comparingDouble((Player p) -> items.get(p.id).rating).reversed());
    }

    public void print(PrintWriter writer, boolean withDifs) {
        int maxNameLength = items.stream()
                .map(item -> item.player.getPlayerName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%-2d: %-" + (maxNameLength + 1) + "s %-7.2f";

        List<Item> sortedItems = items.stream()
                .sorted(comparingDouble((Item item) -> item.rating).reversed())
                .collect(toList());

        for (int i = 0; i < sortedItems.size(); i++) {
            Item item = sortedItems.get(i);
            writer.print(String.format(formatString, (i + 1), item.player.getPlayerName(), item.rating));
            if (withDifs) {
                writer.println(String.format("   %+5.2f", item.rating - item.ratingLastYear));
            } else {
                writer.println();
            }
        }
    }

    private void savePointsAsLastYearPoints() {
        items.forEach(item -> item.ratingLastYear = item.rating);
    }

    public void resetPlayer(Player player) {
        double[][] data = items.stream()
                .map(item -> new double[]{item.player.getLevel(), item.rating})
                .toArray(double[][]::new);

        SimpleRegression simpleRegression = new SimpleRegression();
        simpleRegression.addData(data);

        Item item = items.get(player.id);
        item.rating = simpleRegression.getIntercept() + simpleRegression.getSlope() * player.getLevel();
        normalize();
    }

    private static double calculateRatingChange(double rat1, double rat2, double res) {
        double dif = rat2 - rat1;
        double e = 1 / (1 + Math.exp(dif / 400 * Math.log(10)));
        return K_FACTOR * (res - e);
    }
}
