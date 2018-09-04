package com.clocktower.tournament;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.IntStream;

import static com.clocktower.tournament.Player.increaseDeckAtPosition;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerTest {
    //@Test
    void testGenerateRandomDeckKind() {
        Map<Integer, Long> counts = IntStream.range(0, 100000)
                .mapToObj(i -> Player.generateRandomDeckKind())
                .collect(groupingBy(PlayerTest::getId, counting()));
        System.out.println(counts);
    }

    private static int getId(int[] deckKind) {
        int c = 1;
        for (int i = 1; i <= 8; ++i) {
            for (int j = i + 1; j <= 9; ++j) {
                if (deckKind[0] == i && deckKind[1] == j) {
                    return c;
                } else {
                    ++c;
                }
            }
        }
        throw new IllegalArgumentException("Invalid deck kind");
    }

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