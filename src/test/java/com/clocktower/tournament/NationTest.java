package com.clocktower.tournament;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NationTest {
    @Test
    public void testGetId() {
        assertEquals(0, Nation.ALMAGEST.getId());
        assertEquals(4, Nation.OBERON_22.getId());
    }

}