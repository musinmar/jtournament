package com.clocktower.tournament;

import org.junit.jupiter.api.Test;

import static com.clocktower.tournament.utils.IntToRomanConverter.convert;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IntToRomanConverterTest {

    @Test
    void testConvert() {
        assertEquals("I", convert(1));
        assertEquals("II", convert(2));
        assertEquals("III", convert(3));
        assertEquals("IV", convert(4));
        assertEquals("V", convert(5));
        assertEquals("VI", convert(6));
        assertEquals("VII", convert(7));
        assertEquals("VIII", convert(8));
        assertEquals("IX", convert(9));
        assertEquals("X", convert(10));
    }
}