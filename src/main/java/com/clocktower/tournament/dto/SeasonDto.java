package com.clocktower.tournament.dto;

import java.util.List;

public class SeasonDto {
    private static final int VERSION = 2;

    private int version = VERSION;

    private int year;
    private List<PlayerDto> players;
    private List<Integer> leagues;
    private NationRatingDto nationRating;
    private EloRatingDto eloRating;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<PlayerDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDto> players) {
        this.players = players;
    }

    public List<Integer> getLeagues() {
        return leagues;
    }

    public void setLeagues(List<Integer> leagues) {
        this.leagues = leagues;
    }

    public NationRatingDto getNationRating() {
        return nationRating;
    }

    public void setNationRating(NationRatingDto nationRating) {
        this.nationRating = nationRating;
    }

    public EloRatingDto getEloRating() {
        return eloRating;
    }

    public void setEloRating(EloRatingDto eloRating) {
        this.eloRating = eloRating;
    }
}
