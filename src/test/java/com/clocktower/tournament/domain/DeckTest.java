package com.clocktower.tournament.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeckTest {
    @Test
    void testChangeAtPositionIncrease() {
        Deck deck;
        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(0, 1);
        assertArrayEquals(new int[]{1, 1, 1, 1, 2, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(1, 1);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(2, 1);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(3, 1);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(4, 1);
        assertArrayEquals(new int[]{0, 1, 1, 1, 2, 3}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(5, 1);
        assertArrayEquals(new int[]{0, 1, 1, 1, 2, 3}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 2, 3, 4, 5});
        deck.changeAtPosition(3, 1);
        assertArrayEquals(new int[]{0, 1, 2, 4, 4, 5}, deck.getItems());
    }

    @Test
    void testChangeAtPositionDecrease() {
        Deck deck;
        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(0, -1);
        assertArrayEquals(new int[]{-1, 1, 1, 1, 2, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(1, -1);
        assertArrayEquals(new int[]{0, 0, 1, 1, 2, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(2, -1);
        assertArrayEquals(new int[]{0, 0, 1, 1, 2, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(3, -1);
        assertArrayEquals(new int[]{0, 0, 1, 1, 2, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(4, -1);
        assertArrayEquals(new int[]{0, 1, 1, 1, 1, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        deck.changeAtPosition(5, -1);
        assertArrayEquals(new int[]{0, 1, 1, 1, 1, 2}, deck.getItems());

        deck = Deck.from(new int[]{0, 1, 2, 3, 4, 5});
        deck.changeAtPosition(3, -1);
        assertArrayEquals(new int[]{0, 1, 2, 2, 4, 5}, deck.getItems());
    }

    @Test
    void testChangeDeckAtPositionFailed() {
        Deck deck = Deck.from(new int[]{0, 1, 1, 1, 2, 2});
        assertThrows(IndexOutOfBoundsException.class, () -> deck.changeAtPosition(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> deck.changeAtPosition(6, 1));
    }
}