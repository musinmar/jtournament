package com.clocktower.tournament;

import com.clocktower.tournament.domain.Player;
import com.clocktower.tournament.simulation.SimpleResult;
import com.clocktower.tournament.utils.Logger;

class SeasonContextTest {
    //@Test
    void testPlayPenalties() {
        Player p1 = new Player();
        p1.restartCareer(true);

        Player p2 = new Player();
        p2.restartCareer(true);

        for (int i = 0; i < 1000; i++) {
            SimpleResult r = SeasonContext.playGamePenalties(p1, p2);
            Logger.println(r.r1 + ":" + r.r2);
        }
        Logger.println();
    }
}