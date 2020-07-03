package com.clocktower.tournament.domain;

import com.clocktower.tournament.dto.PlayerDto;
import com.clocktower.tournament.utils.IntToRomanConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.clocktower.tournament.utils.Logger.println;
import static com.clocktower.tournament.utils.RandomUtils.random;
import static java.util.stream.Collectors.counting;

public class Player {
    private static final int RANDOM_DECK_CHANGES = 3;
    private static final int LEVEL_UP_COEFFICIENT = 3;
    private static final int POINTS_PER_LEVEL = 4;

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

    private List<Trophy> trophies = new ArrayList<>();
    private CareerStats careerStats = new CareerStats();

    private int at;
    private int def;
    private int s;
    private int l;
    private int v;

    private DeckType deckType;
    private Deck deck;

    public Player() {
    }

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

    public void setPersistentLevel(int persistentLevel) {
        this.persistentLevel = persistentLevel;
    }

    public int getLevel() {
        return level;
    }

    public int getAge() {
        return age;
    }

    public void setAt(int at) {
        this.at = at;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public void setS(int s) {
        this.s = s;
    }

    public void setL(int l) {
        this.l = l;
    }

    public void setV(int v) {
        this.v = v;
    }

    public CareerStats getCareerStats() {
        return careerStats;
    }

    public void setDeckType(DeckType deckType) {
        this.deckType = deckType;
    }

    public Deck getDeck() {
        return deck;
    }

    public String getSimplePlayerName() {
        return name + " " + surname;
    }

    public String getPlayerName() {
        String ret = title.getPrefix() + getSimplePlayerName();
        if (generation > 1) {
            ret += ' ' + IntToRomanConverter.convert(generation);
        }
        return ret;
    }

    public String getNameWithNation() {
        return title.getPrefix() + getSimplePlayerName() + " (" + nation.getName().charAt(0) + ")";
    }

    public void advanceAge() {
        age += 1;
    }

    public void restartCareer(boolean randomizeDeckKind) {
        age = 1;
        generation += 1;
        trophies.clear();
        exp = 0;

        level = persistentLevel;

        deck = Deck.newDefaultDeck();

        if (randomizeDeckKind) {
            deckType = DeckType.createRandom();
            println("New deck type: " + deckType);
        }

        int k = level * POINTS_PER_LEVEL;
        for (int i = 0; i < k; i++) {
            increaseDeck();
        }
    }

    public void addExp(int newExp) {
        exp += newExp;
        while (exp >= getExpForLevelUp()) {
            levelUp();
        }
    }

    private int getExpForLevelUp() {
        return level * level * LEVEL_UP_COEFFICIENT;
    }

    private void levelUp() {
        println(getPlayerName() + " has gained level!");
        for (int i = 0; i < POINTS_PER_LEVEL; i++) {
            increaseDeck();
        }
        exp -= getExpForLevelUp();
        level += 1;
    }

    public PlayerDto toDto() {
        PlayerDto playerDto = new PlayerDto();
        playerDto.setId(id);
        playerDto.setName(name);
        playerDto.setSurname(surname);
        playerDto.setTitle(title);
        playerDto.setNation(nation);
        playerDto.setTown(town);
        playerDto.setAge(age);
        playerDto.setGeneration(generation);
        playerDto.setPersistentLevel(persistentLevel);
        playerDto.setLevel(level);
        playerDto.setAt(at);
        playerDto.setDef(def);
        playerDto.setS(s);
        playerDto.setL(l);
        playerDto.setV(v);
        playerDto.setExp(exp);
        playerDto.setTrophies(trophies);
        playerDto.setCareerStats(careerStats);
        playerDto.setDeckType(new int[]{deckType.getV1(), deckType.getV2()});
        playerDto.setDeck(deck);
        return playerDto;
    }

    public static Player fromDto(PlayerDto playerDto) {
        Player player = new Player();
        player.id = playerDto.getId();
        player.name = playerDto.getName();
        player.surname = playerDto.getSurname();
        player.title = playerDto.getTitle();
        player.nation = playerDto.getNation();
        player.town = playerDto.getTown();
        player.age = playerDto.getAge();
        player.generation = playerDto.getGeneration();
        player.persistentLevel = playerDto.getPersistentLevel();
        player.level = playerDto.getLevel();
        player.at = playerDto.getAt();
        player.def = playerDto.getDef();
        player.s = playerDto.getS();
        player.l = playerDto.getL();
        player.v = playerDto.getV();
        player.exp = playerDto.getExp();
        player.trophies = playerDto.getTrophies();
        player.careerStats = playerDto.getCareerStats() != null ? playerDto.getCareerStats() : new CareerStats();
        player.deckType = new DeckType(playerDto.getDeckType()[0], playerDto.getDeckType()[1]);
        player.deck = playerDto.getDeck();
        return player;
    }

    public void addTrophy(String trophy, int year) {
        trophies.add(new Trophy(trophy, year));
    }

    public Map<String, Long> getTrophiesByType() {
        return trophies.stream()
                .collect(Collectors.groupingBy(Trophy::getName, () -> new TreeMap<>(Comparator.comparingInt(Trophy::getValue).reversed()), counting()));
    }

    public void increaseDeck() {
        int r = deckType.generateRandomDeckPosition();
        deck.changeAtPosition(r, 1);
    }

    public void decreaseDeck() {
        int k = deckType.generateRandomDeckPosition();
        deck.changeAtPosition(k, -1);
    }

    public void applyRandomDeckChanges() {
        for (int i = 1; i <= RANDOM_DECK_CHANGES; ++i) {
            int k = deckType.generateRandomDeckPosition();
            int change = random(2) * 2 - 1;
            deck.changeAtPosition(k, change);
        }
    }

    public int getEffectiveLevel() {
        return deck.sum();
    }
}
