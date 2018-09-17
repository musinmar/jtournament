package com.clocktower.tournament;

import com.clocktower.tournament.domain.Player;
import com.clocktower.tournament.dto.EloRatingDto;
import com.clocktower.tournament.simulation.SimpleResult;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingDouble;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class EloRating {
    private static final double K_FACTOR = 20;
    private static final double AVERAGE_RATING = 500;

    private Map<Player, Rating> ratings;

    private static class Rating {
        private double value;
        private double valuePreviousYear;

        Rating(double rating) {
            this.value = rating;
            this.valuePreviousYear = rating;
        }
    }

    public EloRating() {
    }

    public void init(List<Player> players) {
        ratings = players.stream().collect(toMap(identity(), p -> new Rating(AVERAGE_RATING)));
    }

    public EloRatingDto toDto() {
        EloRatingDto eloRatingDto = new EloRatingDto();
        List<EloRatingDto.ItemDto> itemDtos = ratings.entrySet().stream()
                .map(e -> new EloRatingDto.ItemDto(e.getKey().id, e.getValue().value))
                .collect(toList());
        eloRatingDto.setItems(itemDtos);
        return eloRatingDto;
    }

    public static EloRating fromDto(EloRatingDto eloRatingDto, List<Player> players) {
        EloRating eloRating = new EloRating();
        eloRating.ratings = eloRatingDto.getItems().stream()
                .collect(toMap(item -> players.get(item.getPlayerId()), item -> new Rating(item.getPoints())));
        eloRating.normalize();
        eloRating.savePointsAsLastYearPoints();
        return eloRating;
    }

    private void normalize() {
        Collection<Rating> allRatings = ratings.values();
        double ratingSum = allRatings.stream().mapToDouble(i -> i.value).sum();
        double dif = ratingSum - AVERAGE_RATING * ratings.size();
        if (Math.abs(dif) > 1) {
            allRatings.forEach(i -> i.value -= dif / ratings.size());
        }
    }

    public List<Player> getPlayersByRating() {
        return ratings.keySet().stream()
                .sorted(comparingDouble(this::getRating).reversed())
                .collect(toList());
    }

    public void updateRatings(Player p1, Player p2, SimpleResult r) {
        double sum = r.r1 + r.r2;
        double r1 = r.r1 / sum;
        double r2 = r.r2 / sum;

        Rating item1 = ratings.get(p1);
        Rating item2 = ratings.get(p2);
        double rat1 = item1.value;
        double rat2 = item2.value;
        item1.value += calculateRatingChange(rat1, rat2, r1);
        item2.value += calculateRatingChange(rat2, rat1, r2);
    }

    public int comparePlayersByRating(Player p1, Player p2) {
        return comparingDouble(this::getRating).compare(p1, p2);
    }

    public double getRating(Player player) {
        return ratings.get(player).value;
    }

    public void sortPlayersByRating(List<Player> players) {
        players.sort(comparingDouble(this::getRating).reversed());
    }

    public void print(PrintWriter writer, boolean withDifs) {
        int maxNameLength = ratings.keySet().stream()
                .map(Player::getPlayerName)
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%-2d: %-" + (maxNameLength + 1) + "s %-7.2f";

        List<Map.Entry<Player, Rating>> sortedItems = ratings.entrySet().stream()
                .sorted(comparingDouble((Map.Entry<Player, Rating> entry) -> entry.getValue().value).reversed())
                .collect(toList());

        for (int i = 0; i < sortedItems.size(); i++) {
            Map.Entry<Player, Rating> entry = sortedItems.get(i);
            writer.print(String.format(formatString, (i + 1), entry.getKey().getPlayerName(), entry.getValue().value));
            if (withDifs) {
                writer.println(String.format("   %+5.2f", entry.getValue().value - entry.getValue().valuePreviousYear));
            } else {
                writer.println();
            }
        }
    }

    private void savePointsAsLastYearPoints() {
        ratings.values().forEach(item -> item.valuePreviousYear = item.value);
    }

    public void resetRating(Player player) {
        double[][] data = ratings.entrySet().stream()
                .map(entry -> new double[]{entry.getKey().getLevel(), entry.getValue().value})
                .toArray(double[][]::new);

        SimpleRegression simpleRegression = new SimpleRegression();
        simpleRegression.addData(data);

        Rating item = ratings.get(player);
        item.value = simpleRegression.predict(player.getLevel());
        normalize();
    }

    private static double calculateRatingChange(double rat1, double rat2, double res) {
        double dif = rat2 - rat1;
        double e = 1 / (1 + Math.exp(dif / 400 * Math.log(10)));
        return K_FACTOR * (res - e);
    }
}
