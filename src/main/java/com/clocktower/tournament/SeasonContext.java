package com.clocktower.tournament;

import com.clocktower.tournament.domain.Player;
import com.clocktower.tournament.simulation.MatchResult;
import com.clocktower.tournament.simulation.SimpleResult;

import java.util.List;

import static com.clocktower.tournament.utils.Logger.print;
import static com.clocktower.tournament.utils.Logger.println;

public class SeasonContext {
    private static final int NORMAL_TIME_LENGTH = 9;
    private static final int ADDITIONAL_TIME_LENGTH = 7;

    private List<Player> knights;
    private EloRating elo;
    private NationRating nationRating;

    public SeasonContext(List<Player> players) {
        this.knights = players;
        elo = new EloRating(players);
        nationRating = new NationRating();
        nationRating.initDefault();
    }

    public SeasonContext(List<Player> knights, EloRating elo, NationRating nationRating) {
        this.knights = knights;
        this.elo = elo;
        this.nationRating = nationRating;
    }

    public List<Player> getKnights() {
        return knights;
    }

    public EloRating getElo() {
        return elo;
    }

    public NationRating getNationRating() {
        return nationRating;
    }

    public MatchResult<Player> playGroupGame(Player p1, Player p2, int points) {
        return playGame(p1, p2, false, points);
    }

    public MatchResult<Player> playPlayoffGame(Player p1, Player p2, int points) {
        return playGame(p1, p2, true, points);
    }

    private MatchResult<Player> playGame(Player p1, Player p2, boolean isPlayoff, int points) {
        MatchResult<Player> res = new MatchResult<>(p1, p2);

        println(p1.getNameWithNation() + " vs " + p2.getNameWithNation());

        SimpleResult l = playGameRound(p1, p2, NORMAL_TIME_LENGTH);
        print(l + " ");
        res.addRoundResult(l, false);

        l = playGameRound(p1, p2, NORMAL_TIME_LENGTH);
        print("/ " + l + " ");
        res.addRoundResult(l, false);

        if (isPlayoff) {
            if (res.rounds.r1 == res.rounds.r2) {
                l = playGameRound(p1, p2, ADDITIONAL_TIME_LENGTH);
                print("/ e.t. " + l + " ");
                res.addRoundResult(l, true);
            }

            if (res.rounds.r1 == res.rounds.r2) {
                l = playGamePenalties(p1, p2);
                print("/ pen. " + l + " ");
                res.addRoundResult(l, true);
            }
        }

        println("( " + res.rounds + " )");

        updateElo(p1, p2, res);
        updateExp(p1, p2, res);
        updateCareerStats(p1, p2, res);
        nationRating.updateRatings(p1, p2, res.rounds, points);

        return res;
    }

    private void updateElo(Player p1, Player p2, MatchResult<Player> mr) {
        elo.updateRatings(p1, p2, mr.rounds);
    }

    private void updateExp(Player p1, Player p2, MatchResult<Player> mr) {
        if (mr.rounds.r1 > mr.rounds.r2) {
            p1.addExp(p2.getLevel());
        } else if (mr.rounds.r2 > mr.rounds.r1) {
            p2.addExp(p1.getLevel());
        }
    }

    private void updateCareerStats(Player p1, Player p2, MatchResult<Player> mr) {
        p1.getCareerStats().applyMatchResult(mr.rounds);
        p2.getCareerStats().applyMatchResult(mr.rounds.reversed());
    }

    private static SimpleResult playGameRound(Player p1, Player p2, int len) {
        int[] d1 = p1.getDeck().getShuffledItems();
        int[] d2 = p2.getDeck().getShuffledItems();

        SimpleResult r = new SimpleResult();
        for (int i = 0; i < len; i++) {
            if (d1[i] > d2[i]) {
                ++r.r1;
            } else if (d1[i] < d2[i]) {
                ++r.r2;
            }
        }
        return r;
    }

    static SimpleResult playGamePenalties(Player p1, Player p2) {
        SimpleResult r = new SimpleResult();

        int round = 0;
        while (true) {
            int[] d1 = p1.getDeck().getShuffledItems();
            int[] d2 = p2.getDeck().getShuffledItems();
            int length = Math.min(d1.length, d2.length);
            for (int i = 0; i < length; i += 2) {
                int k1 = 0;
                int k2 = 0;

                if (d1[i] > d2[i]) {
                    ++k1;
                } else {
                    ++k2;
                }
                if (d1[i + 1] < d2[i + 1]) {
                    ++k2;
                } else {
                    ++k1;
                }

                if (k1 >= k2) {
                    ++r.r1;
                }
                if (k2 >= k1) {
                    ++r.r2;
                }

                if (round >= 2 && (r.r1 > r.r2 || r.r1 < r.r2)) {
                    break;
                }
                ++round;
            }

            if (r.r1 > r.r2 || r.r1 < r.r2) {
                break;
            }
        }

        return r;
    }
}
