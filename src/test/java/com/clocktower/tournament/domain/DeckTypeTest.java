package com.clocktower.tournament.domain;

import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

class DeckTypeTest {

    //@Test
    void testCreateRandom() {
        Map<Integer, Long> counts = IntStream.range(0, 100000)
                .mapToObj(i -> DeckType.createRandom())
                .collect(groupingBy(DeckType::getId, counting()));
        System.out.println(counts);
    }
}