package com.clocktower.tournament.domain;

import static com.clocktower.tournament.utils.RandomUtils.random;

public class DeckType {
    private final int v1;
    private final int v2;

    public DeckType(int v1, int v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public int getV1() {
        return v1;
    }

    public int getV2() {
        return v2;
    }

    public int generateRandomDeckPosition() {
        int r = random(10);
        if (r < getV1()) {
            r = random(7);
        } else if (r < getV2()) {
            r = random(6) + 7;
        } else {
            r = random(7) + 13;
        }
        return r;
    }

    @Override
    public String toString() {
        return v1 + "/" + v2;
    }

    int getId() {
        int c = 1;
        for (int i = 1; i <= 8; ++i) {
            for (int j = i + 1; j <= 9; ++j) {
                if (v1 == i && v2 == j) {
                    return c;
                } else {
                    ++c;
                }
            }
        }
        throw new IllegalArgumentException("Invalid deck type");
    }


    public static DeckType createRandom() {
        int r = random(36) + 1;
        int k = 0;
        for (int i = 8; i >= 1; i--) {
            if (k + i >= r) {
                return new DeckType(9 - i, 9 - i + (r - k));
            } else {
                k = k + i;
            }
        }
        throw new RuntimeException("Should not get here");
    }
}
