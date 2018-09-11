package com.clocktower.tournament.dto;

import java.util.List;

public class EloRatingDto {
    private List<ItemDto> items;

    public static class ItemDto {
        private int playerId;
        private double points;
        private double pointsLastYear;

        public int getPlayerId() {
            return playerId;
        }

        public void setPlayerId(int playerId) {
            this.playerId = playerId;
        }

        public double getPoints() {
            return points;
        }

        public void setPoints(double points) {
            this.points = points;
        }

        public double getPointsLastYear() {
            return pointsLastYear;
        }

        public void setPointsLastYear(double pointsLastYear) {
            this.pointsLastYear = pointsLastYear;
        }
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }
}
