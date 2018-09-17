package com.clocktower.tournament.domain;

import org.junit.jupiter.api.Test;

import static com.clocktower.tournament.domain.Player.changeDeckAtPosition;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerTest {
    @Test
    void testChangeDeckAtPositionIncrease() {
        int[] deck;
        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 0, 1);
        assertArrayEquals(new int[]{1, 1, 1, 1, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 1, 1);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 2, 1);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 3, 1);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 4, 1);
        assertArrayEquals(new int[]{0, 1, 1, 1, 2, 3}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 5, 1);
        assertArrayEquals(new int[]{0, 1, 1, 1, 2, 3}, deck);

        deck = new int[]{0, 1, 2, 3, 4, 5};
        changeDeckAtPosition(deck, 3, 1);
        assertArrayEquals(new int[]{0, 1, 2, 4, 4, 5}, deck);
    }

    @Test
    void testChangeDeckAtPositionDecrease() {
        int[] deck;
        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 0, -1);
        assertArrayEquals(new int[]{-1, 1, 1, 1, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 1, -1);
        assertArrayEquals(new int[]{0, 0, 1, 1, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 2, -1);
        assertArrayEquals(new int[]{0, 0, 1, 1, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 3, -1);
        assertArrayEquals(new int[]{0, 0, 1, 1, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 4, -1);
        assertArrayEquals(new int[]{0, 1, 1, 1, 1, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        changeDeckAtPosition(deck, 5, -1);
        assertArrayEquals(new int[]{0, 1, 1, 1, 1, 2}, deck);

        deck = new int[]{0, 1, 2, 3, 4, 5};
        changeDeckAtPosition(deck, 3, -1);
        assertArrayEquals(new int[]{0, 1, 2, 2, 4, 5}, deck);
    }

    @Test
    void testChangeDeckAtPositionFailed() {
        int[] deck = new int[]{0, 1, 1, 1, 2, 2};
        assertThrows(IndexOutOfBoundsException.class, () -> changeDeckAtPosition(deck, -1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> changeDeckAtPosition(deck, 6, 1));
    }
}