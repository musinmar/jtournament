package com.clocktower.tournament.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

public class Deck {

    @JsonValue
    private List<Integer> items;

    private Deck(List<Integer> items) {
        var sortedItems = new ArrayList<>(items);
        sort(sortedItems);
        this.items = sortedItems;
    }

    public static Deck newDefaultDeck() {
        Integer[] items = new Integer[]{
                0, 0, 0, 1, 1,
                1, 1, 2, 2, 2,
                3, 3, 3, 4, 4,
                4, 4, 5, 5, 5
        };
        return new Deck(Arrays.asList(items));
    }

    @JsonCreator
    public static Deck from(List<Integer> items) {
        return new Deck(items);
    }

    public void changeAtPosition(int pos, int dif) {
        items.set(pos, items.get(pos) + dif);
        sort(items);
    }

    public List<Integer> getItems() {
        return unmodifiableList(items);
    }

    public int[] getShuffledItems() {
        int[] d = items.stream().mapToInt(v -> v).toArray();
        ArrayUtils.shuffle(d);
        return d;
    }
}
