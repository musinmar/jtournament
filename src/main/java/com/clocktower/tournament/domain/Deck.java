package com.clocktower.tournament.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class Deck {

    public static int STANDARD_SIZE = 20;

    @JsonValue
    private int[] items;

    private Deck(int[] items) {
        int[] sortedItems = ArrayUtils.clone(items);
        Arrays.sort(sortedItems);
        this.items = sortedItems;
    }

    public static Deck newDefaultDeck() {
        int[] items = new int[]{
                0, 0, 0, 1, 1,
                1, 1, 2, 2, 2,
                3, 3, 3, 4, 4,
                4, 4, 5, 5, 5
        };
        return new Deck(items);
    }

    @JsonCreator
    public static Deck from(int[] items) {
        return new Deck(items);
    }

    public void changeAtPosition(int pos, int dif) {
        items[pos] += dif;
        Arrays.sort(items);
    }

    public int[] getItems() {
        return items;
    }

    public int[] getShuffledItems() {
        int[] d = ArrayUtils.clone(items);
        ArrayUtils.shuffle(d);
        return d;
    }
}
