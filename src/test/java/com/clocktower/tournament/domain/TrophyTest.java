package com.clocktower.tournament.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrophyTest {

    @Test
    void testValueOf() {
        assertEquals(new Trophy("Abc", 2), Trophy.valueOf("Abc 2"));
        assertEquals(new Trophy("A", 10), Trophy.valueOf("A 10"));
        assertEquals(new Trophy("Some trophy", 5), Trophy.valueOf("Some trophy 5"));
    }
}