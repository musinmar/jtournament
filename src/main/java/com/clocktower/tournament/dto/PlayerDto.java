package com.clocktower.tournament.dto;

import com.clocktower.tournament.domain.CareerStats;
import com.clocktower.tournament.domain.Deck;
import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.domain.Title;
import com.clocktower.tournament.domain.Trophy;

import java.util.List;

public class PlayerDto {
    private int id;

    private String name;
    private String surname;
    private String town;
    private Nation nation;
    private Title title;

    private int age = 1;
    private int generation = 0;
    private int level;
    private int persistentLevel;
    private int exp = 0;

    private List<Trophy> trophies;
    private CareerStats careerStats;

    private int at;
    private int def;
    private int s;
    private int l;
    private int v;

    private int[] deckType;
    private Deck deck;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPersistentLevel() {
        return persistentLevel;
    }

    public void setPersistentLevel(int persistentLevel) {
        this.persistentLevel = persistentLevel;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public List<Trophy> getTrophies() {
        return trophies;
    }

    public void setTrophies(List<Trophy> trophies) {
        this.trophies = trophies;
    }

    public int getAt() {
        return at;
    }

    public void setAt(int at) {
        this.at = at;
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public int[] getDeckType() {
        return deckType;
    }

    public void setDeckType(int[] deckType) {
        this.deckType = deckType;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public CareerStats getCareerStats() {
        return careerStats;
    }

    public void setCareerStats(CareerStats careerStats) {
        this.careerStats = careerStats;
    }
}
