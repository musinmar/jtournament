package com.clocktower.tournament;

import com.clocktower.tournament.domain.DeckType;
import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.domain.Title;
import com.clocktower.tournament.domain.Trophy;
import com.clocktower.tournament.utils.IntToRomanConverter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.clocktower.tournament.Logger.println;
import static com.clocktower.tournament.utils.RandomUtils.random;
import static java.util.stream.Collectors.counting;

public class Player {
    private static final int RANDOM_DECK_CHANGES = 3;
    private static final int LEVEL_UP_COEFFICIENT = 3;
    private static final int POINTS_PER_LEVEL = 4;

    public int id;

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

    private int at;
    private int def;
    private int s;
    private int l;
    private int v;

    private DeckType deckType;
    // TODO: create class for deck
    public int[] deck = new int[20];

    public Player() {
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

    public void setDeckType(DeckType deckType) {
        this.deckType = deckType;
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

        deck[0] = 0;
        deck[1] = 0;
        deck[2] = 0;
        deck[3] = 1;
        deck[4] = 1;
        deck[5] = 1;
        deck[6] = 1;
        deck[7] = 2;
        deck[8] = 2;
        deck[9] = 2;
        deck[10] = 3;
        deck[11] = 3;
        deck[12] = 3;
        deck[13] = 4;
        deck[14] = 4;
        deck[15] = 4;
        deck[16] = 4;
        deck[17] = 5;
        deck[18] = 5;
        deck[19] = 5;

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

    public void save(PrintWriter writer) {
        writer.println(id);
        writer.println(name);
        writer.println(surname);
        writer.println(title.getPrefix());
        writer.println(nation.getName());
        writer.println(town);
        writer.println(age);
        writer.println(generation);
        writer.println(persistentLevel);
        writer.println(level);
        writer.println(at);
        writer.println(def);
        writer.println(s);
        writer.println(l);
        writer.println(v);
        writer.println(exp);

        writer.println(trophies.size());
        trophies.forEach(writer::println);

        writer.println(deckType.getV1());
        writer.println(deckType.getV2());

        Arrays.stream(deck).forEach(writer::println);
    }

    public void load(Scanner sc) {
        id = sc.nextInt();
        name = sc.next();
        surname = sc.next();
        sc.nextLine();
        String titleLabel = sc.nextLine();
        title = Title.fromName(titleLabel.trim());
        String nationName = sc.next();
        this.nation = Nation.fromName(nationName);
        town = sc.next();
        age = sc.nextInt();
        generation = sc.nextInt();
        persistentLevel = sc.nextInt();
        level = sc.nextInt();
        at = sc.nextInt();
        def = sc.nextInt();
        s = sc.nextInt();
        l = sc.nextInt();
        v = sc.nextInt();
        exp = sc.nextInt();

        int k = sc.nextInt();
        sc.nextLine();
        for (int i = 0; i < k; i++) {
            String s = sc.nextLine();
            trophies.add(Trophy.valueOf(s));
        }

        int deckTypeV1 = sc.nextInt();
        int deckTypeV2 = sc.nextInt();
        deckType = new DeckType(deckTypeV1, deckTypeV2);

        for (int i = 0; i < 20; i++) {
            deck[i] = sc.nextInt();
        }
    }

    public void addTrophy(String trophy, int year) {
        trophies.add(new Trophy(trophy, year));
    }

    public Map<String, Long> getTrophiesByType() {
        return trophies.stream()
                .collect(Collectors.groupingBy(Trophy::getName, counting()));
    }

    public void increaseDeck() {
        int r = deckType.generateRandomDeckPosition();
        changeDeckAtPosition(deck, r, 1);
    }

    public void decreaseDeck() {
        int k = deckType.generateRandomDeckPosition();
        changeDeckAtPosition(deck, k, -1);
    }

    public void applyRandomDeckChanges() {
        for (int i = 1; i <= RANDOM_DECK_CHANGES; ++i) {
            int k = deckType.generateRandomDeckPosition();
            int change = random(2) * 2 - 1;
            changeDeckAtPosition(deck, k, change);
        }
    }

    static void changeDeckAtPosition(int[] deck, int pos, int dif) {
        deck[pos] += dif;
        Arrays.sort(deck);
    }

    public int[] getShuffledDeck() {
        int[] d = ArrayUtils.clone(deck);
        ArrayUtils.shuffle(d);
        return d;
    }
}
