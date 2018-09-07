package com.clocktower.tournament;

import org.junit.jupiter.api.Test;

import static com.clocktower.tournament.Player.increaseDeckAtPosition;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerTest {
    @Test
    void testIncreaseDeckAtPosition() {
        int[] deck;
        deck = new int[]{0, 1, 1, 1, 2, 2};
        increaseDeckAtPosition(deck, 0);
        assertArrayEquals(new int[]{1, 1, 1, 1, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        increaseDeckAtPosition(deck, 1);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        increaseDeckAtPosition(deck, 2);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        increaseDeckAtPosition(deck, 3);
        assertArrayEquals(new int[]{0, 1, 1, 2, 2, 2}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        increaseDeckAtPosition(deck, 4);
        assertArrayEquals(new int[]{0, 1, 1, 1, 2, 3}, deck);

        deck = new int[]{0, 1, 1, 1, 2, 2};
        increaseDeckAtPosition(deck, 5);
        assertArrayEquals(new int[]{0, 1, 1, 1, 2, 3}, deck);

        deck = new int[]{0, 1, 2, 3, 4, 5};
        increaseDeckAtPosition(deck, 3);
        assertArrayEquals(new int[]{0, 1, 2, 4, 4, 5}, deck);
    }

    @Test
    void testIncreaseDeckAtPosition2() {
        int[] deck = new int[]{0, 1, 1, 1, 2, 2};
        assertThrows(IndexOutOfBoundsException.class, () -> increaseDeckAtPosition(deck, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> increaseDeckAtPosition(deck, 6));
    }
}