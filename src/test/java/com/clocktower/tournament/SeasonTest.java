package com.clocktower.tournament;

class SeasonTest {

    //@Test
    void testPlayPenalties() {
        Player p1 = new Player();
        p1.restartCareer(true);

        Player p2 = new Player();
        p2.restartCareer(true);

        for (int i = 0; i < 1000; i++) {
            Season.SimpleResult r = Season.playPenalties(p1, p2);
            Logger.println(r.r1 + ":" + r.r2);
        }
        Logger.println();
    }
}