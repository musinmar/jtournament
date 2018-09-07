package com.clocktower.tournament;

import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.domain.Title;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.clocktower.tournament.Logger.println;
import static com.clocktower.tournament.RandomUtils.random;

public class Player {
    public static final int RANDOM_DECK_CHANGES = 3;
    public static final int LEVEL_UP_COEFFICIENT = 3;
    public static final int POINTS_PER_LEVEL = 4;

    public int id;

    public String name;
    public String surname;
    public String town;
    private Nation nation;
    private Title title;

    public int age = 1;
    public int generation = 0;

    public List<String> dost = new ArrayList<>();

    public int at;
    public int def;
    public int s;
    public int l;
    public int v;

    public int persistentLevel;
    public int level;
    public int exp = 0;

    // TODO: create class for deck kind
    public int[] deckKind = new int[2];
    // TODO: create class for deck
    public int[] deck = new int[20];

    public Player() {
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

    public String getPlayerName() {
        String ret = title.getPrefix() + name + ' ' + surname;
        if (generation > 1) {
            ret += ' ' + IntToRomanConverter.convert(generation);
        }
        return ret;
    }

    public String getNameWithNation() {
        return title.getPrefix() + name + " " + surname + " (" + nation.getName().charAt(0) + ")";
    }

    public void restartCareer(boolean randomizeDeckKind) {
        age = 1;
        generation = generation + 1;
        dost.clear();
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
            deckKind = generateRandomDeckKind();
            println("New deck settings: " + deckKind[0] + ", " + deckKind[1]);
        }

        int k = level * POINTS_PER_LEVEL;
        for (int i = 0; i < k; i++) {
            incDeck();
        }
    }

    static int[] generateRandomDeckKind() {
        int[] deckKind = new int[2];
        int r = random(36) + 1;

        int k = 0;
        for (int i = 8; i >= 1; i--) {
            if (k + i >= r) {
                deckKind[0] = 9 - i;
                deckKind[1] = 9 - i + (r - k);
                break;
            } else {
                k = k + i;
            }
        }
        return deckKind;
    }

    public void levelup() {
        println(getPlayerName() + " has gained level!");
        for (int i = 0; i < POINTS_PER_LEVEL; i++) {
            incDeck();
        }
        exp = exp - level * level * LEVEL_UP_COEFFICIENT;
        level += 1;
        if (exp >= level * level * LEVEL_UP_COEFFICIENT) {
            levelup();
        }
    }

    public void incDeck() {
        int r = generateRandomDeckPosition(deckKind);
        increaseDeckAtPosition(deck, r);
    }

    private static int generateRandomDeckPosition(int[] deckKind) {
        int r = random(10);
        if (r < deckKind[0]) {
            r = random(7);
        } else if (r < deckKind[1]) {
            r = random(6) + 7;
        } else {
            r = random(7) + 13;
        }
        return r;
    }

    static void increaseDeckAtPosition(int[] deck, int pos) {
        // TODO: can just sort array after increase

        int size = deck.length;
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException();
        }

        if (pos == size - 1) {
            ++deck[pos];
        } else {
            int k = pos + 1;
            while ((k < size) && (deck[k] == deck[pos])) {
                ++k;
            }
            if (k >= size) {
                ++deck[size - 1];
            } else {
                ++deck[k - 1];
            }
        }
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

        writer.println(dost.size());
        dost.forEach(writer::println);

        Arrays.stream(deckKind).forEach(writer::println);

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
            dost.add(s);
        }

        deckKind[0] = sc.nextInt();
        deckKind[1] = sc.nextInt();

        for (int i = 0; i < 20; i++) {
            deck[i] = sc.nextInt();
        }
    }

    public void addTrophy(String trophy, int year) {
        dost.add(trophy + " " + year);
    }

    public void applyRandomDeckChanges() {
        for (int i = 1; i <= RANDOM_DECK_CHANGES; ++i) {
            int k = random(20);
            int change = random(2) * 2 - 1;
            if (change > 0) {
                while (k < 20 - 1 && deck[k] == deck[k + 1]) {
                    ++k;
                }
            } else {
                while (k > 0 && deck[k] == deck[k - 1]) {
                    --k;
                }
            }
            deck[k] = deck[k] + change;
        }
    }

    public void decDeck() {
        int k = random(20);
        while (k > 0 && deck[k] == deck[k - 1]) {
            --k;
        }
        deck[k] -= 1;
    }
}
