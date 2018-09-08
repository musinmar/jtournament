package com.clocktower.tournament;

import com.clocktower.tournament.domain.DefaultData;
import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.match.MatchResult;
import com.clocktower.tournament.match.SimpleResult;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.clocktower.tournament.Logger.print;
import static com.clocktower.tournament.Logger.println;
import static com.clocktower.tournament.Logger.readln;
import static com.clocktower.tournament.domain.Title.COMMON;
import static com.clocktower.tournament.domain.Title.LORD;
import static com.clocktower.tournament.domain.Title.SIR;
import static com.clocktower.tournament.utils.RandomUtils.random;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class Season {

    private static final int PLAYER_COUNT = 30;

    private static final int NATION_SIZE = 6;

    private static final String FILE_NAME_KNIGHTS = "knights";
    private static final String FILE_NAME_SEASON = "season";
    private static final String FILE_NAME_ELO = "elo";
    private static final String FILE_NAME_RATING = "rating";
    private static final String FILE_NAME_RATING_CHANGE = "rating change";
    private static final String FILE_NAME_STATS = "stats";

    private static final int NORMAL_TIME_LENGTH = 9;
    private static final int ADDITIONAL_TIME_LENGTH = 7;

    private int year;
    private final String FOLDER = "season";

    private Player[] kn = new Player[PLAYER_COUNT];
    private EloRating elo = new EloRating();

    private NationRating nationRating = new NationRating();
    private Map<Nation, int[]> nationalCupResults = new HashMap<>();

    private int[] leagues = new int[PLAYER_COUNT];

    public static class GroupResult {
        int playerId;
        int roundsWon;
        int gamesWon;
        int gamesLost;
    }

    public static class Team {
        String name;
        int[] id;
    }

    public Season() {
    }

    public void init(boolean newGame) {
        Arrays.setAll(kn, i -> new Player());

        if (newGame) {
            initNewGame();
        } else {
            load();
        }
    }

    public void startSimulation() {
        String seasonLogFileName = filename("season", true);
        Logger.setCurrentFilename(seasonLogFileName);

        nationRating.printNationTable();
        nationRating.calculateNationRankingsAndPrint();
        nationRating.advanceYear();

        playTournaments();
        //playLeagues();

//        {if (year - 4) mod 8 = 0 then begin
//        playWorldChampionship();
//        end;}

        if (year % 4 == 0) {
            playLeagues();
        }

        int wc = 0;
        if ((year - 2) % 4 == 0) {
            world_cup();
        }

        if ((year - 1) % 2 == 0) {
            playNationalWorldCup();
        }

        saveToFile(filename(FILE_NAME_RATING_CHANGE, true),
                writer -> elo.print(writer, true));

        advancePlayersAge();
        if (year % 2 == 0) {
            retireRandomPlayer();
        }

        elo.advanceYear();

        playTitlePlayoffs();
        performEndOfSeasonAdjustments();

        saveToFile(filename(FILE_NAME_RATING, true),
                writer -> elo.print(writer, false));

        printStatsToFile();

        year += 1;
        nationRating.printNationTable();
        save();

        Logger.closeCurrentFile();
    }

    private void initNewGame() {
        kn = DefaultData.initDefaultPlayers();

        elo.init(kn);

        List<Player> playersByLevel = Arrays.stream(kn)
                .sorted(comparingInt(Player::getLevel).reversed())
                .collect(toList());
        for (int i = 0; i < PLAYER_COUNT; i++) {
            leagues[i] = playersByLevel.get(i).id;
        }

        year = 1;

        nationRating.initDefault();

        for (Player player : kn) {
            player.restartCareer(false);
        }

        save();
    }

    private void save() {
        saveToFile(filename(FILE_NAME_KNIGHTS, false), writer -> {
            Arrays.stream(kn).forEach(p -> p.save(writer));
        });
        saveToFile(filename(FILE_NAME_SEASON, false), writer -> {
            writer.println(year);
            nationRating.write(writer);
            Arrays.stream(leagues).forEach(writer::println);
        });
        saveToFile(filename(FILE_NAME_ELO, false), elo::save);
    }

    private void saveToFile(String filename, Consumer<PrintWriter> writerConsumer) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writerConsumer.accept(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void load() {
        readFromFile(filename(FILE_NAME_KNIGHTS, false), sc -> {
            for (int i = 0; i < 30; i++) {
                kn[i].load(sc);
            }
        });

        readFromFile(filename(FILE_NAME_SEASON, false), sc -> {
            year = sc.nextInt();
            nationRating.read(sc);
            for (int i = 0; i < 30; i++) {
                leagues[i] = sc.nextInt();
            }
        });

        readFromFile(filename(FILE_NAME_ELO, false), sc -> {
            elo.load(sc, kn);
        });
    }


    private void readFromFile(String filename, Consumer<Scanner> readingConsumer) {
        try (Scanner sc = new Scanner(Paths.get(filename))) {
            sc.useLocale(Locale.US);
            readingConsumer.accept(sc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String filename(String s, boolean withYear) {
        Path folderPath = Paths.get(FOLDER);
        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectory(folderPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String ret = s;
        if (withYear) {
            ret += " " + year;
        }
        return folderPath.resolve(ret + ".txt").toAbsolutePath().toString();
    }

    private void playTournaments() {
        for (Nation nation : Nation.values()) {
            nationalCupResults.put(nation, playNationalCup(nation));
        }

        println("Champions League - Season " + year);
        println("First qualification round");
        println();

        int[] cl_qr1 = new int[6];
        int[] cl_qr2 = new int[8];
        int[] fc_qr1 = new int[4];
        int[] fc_qr2 = new int[10];
        int[] dummy = new int[10];

        cl_qr1[0] = get_player_from(1, 4);
        cl_qr1[1] = get_player_from(3, 3);
        cl_qr1[2] = get_player_from(4, 2);
        cl_qr1[3] = get_player_from(4, 3);
        cl_qr1[4] = get_player_from(5, 1);
        cl_qr1[5] = get_player_from(5, 2);

        cl_qr2[3] = get_player_from(1, 3);
        cl_qr2[4] = get_player_from(2, 2);
        cl_qr2[5] = get_player_from(2, 3);
        cl_qr2[6] = get_player_from(3, 2);
        cl_qr2[7] = get_player_from(4, 1);

        fc_qr1[3] = get_player_from(1, 5);

        fc_qr2[2] = get_player_from(2, 5);
        fc_qr2[3] = get_player_from(3, 5);
        fc_qr2[4] = get_player_from(4, 4);
        fc_qr2[5] = get_player_from(5, 4);

        print_players(cl_qr1);
        shuffle(cl_qr1, true);

        play_playoff_round(cl_qr1, cl_qr2, fc_qr1, 2);

        println("Federations Cup - Season " + year);
        println("First qualification round");
        println();

        print_players(fc_qr1);
        shuffle(fc_qr1, true);

        play_playoff_round(fc_qr1, fc_qr2, dummy, 1);

        int[] cl_gr = new int[8];
        int[] fc_gr = new int[8];

        cl_gr[4] = get_player_from(1, 1);
        cl_gr[5] = get_player_from(1, 2);
        cl_gr[6] = get_player_from(2, 1);
        cl_gr[7] = get_player_from(3, 1);

        fc_gr[5] = get_player_from(2, 4);
        fc_gr[6] = get_player_from(3, 4);
        fc_gr[7] = get_player_from(5, 3);


        println("Champions League - Second qualification round");
        println();

        print_players(cl_qr2);
        shuffle(cl_qr2, true);
        play_playoff_round(cl_qr2, cl_gr, dummy, 2);
        for (int i = 0; i <= 3; ++i) {
            fc_qr2[i + 6] = dummy[i];
        }

        println("Federations Cup - Second qualification round");
        println();
        print_players(fc_qr2);
        shuffle(fc_qr2, true);
        play_playoff_round(fc_qr2, fc_gr, dummy, 1);

        println("Federations Cup - Group round");
        println();
        elo.sortPlayers(fc_gr);
        print_players(fc_gr);
        make_groups(fc_gr);
        play_group_round(fc_gr, 2, 1);

        int[] fc_sf = new int[4];
        int[] cl_sf = new int[4];
        int[] fc_f = new int[2];
        int[] cl_f = new int[2];

        fc_sf[0] = fc_gr[0];
        fc_sf[1] = fc_gr[5];
        fc_sf[2] = fc_gr[4];
        fc_sf[3] = fc_gr[1];

        println("Champions League - Group round");
        println();
        elo.sortPlayers(cl_gr);
        print_players(cl_gr);
        make_groups(cl_gr);
        play_group_round(cl_gr, 4, 1);

        cl_sf[0] = cl_gr[0];
        cl_sf[1] = cl_gr[5];
        cl_sf[2] = cl_gr[4];
        cl_sf[3] = cl_gr[1];

        println("Federations Cup - Semifinals");
        println("Semifinals");
        println();
        //play_playoff_round(t_fc, fc_sf, fc_f, dummy, 2);
        play_series_playoff_round(fc_sf, fc_f, dummy, 3, 1);

        println("Champions League - Semifinal Group");
        println("Semifinals");
        println();

        play_series_playoff_round(cl_sf, cl_f, dummy, 3, 2);

        println("Federations Cup - FINAL");
        println("Final");
        println();
        SimpleResult r = play_series(fc_f[0], fc_f[1], 3, 1);
        int fc_winner;
        if (r.r1 > r.r2) {
            fc_winner = fc_f[0];
        } else {
            fc_winner = fc_f[1];
        }
        readln();
        println();
        println("Knight " + kn[fc_winner].getPlayerName() + " is the winner of the Federation Cup!");
        println();

        println("Champions League - FINAL");
        println("Final");
        println();
        r = play_series(cl_f[0], cl_f[1], 3, 2);
        int cl_winner;
        if (r.r1 > r.r2) {
            cl_winner = cl_f[0];
        } else {
            cl_winner = cl_f[1];
        }
        readln();
        println();
        println("Knight " + kn[cl_winner].getPlayerName() + " is the winner of the Champions League!");
        println(kn[cl_winner].getNation().getName() + "\'s triumph!");
        println();


        kn[fc_winner].addTrophy("Federations Cup", year);
        kn[cl_winner].addTrophy("Champions League", year);
    }

    private int get_player_from(int rank, int pos) {
        Nation nation = nationRating.getRankedNation(rank - 1);
        return nationalCupResults.get(nation)[pos - 1];
    }

    private void make_groups(int[] a) {
        int[] b = new int[4];
        int[] c = new int[4];
        for (int i = 0; i <= 3; ++i) {
            b[i] = a[i];
        }
        for (int i = 0; i <= 3; ++i) {
            c[i] = a[i + 4];
        }

        shuffle(b, false);
        shuffle(c, false);

        a[0] = b[0];
        a[1] = b[1];
        a[2] = c[0];
        a[3] = c[1];
        a[4] = b[2];
        a[5] = b[3];
        a[6] = c[2];
        a[7] = c[3];
        //shuffle(a, true);
    }

    private int[] playNationalCup(Nation nation) {
        int[] players = Arrays.stream(kn)
                .filter(p -> p.getNation() == nation)
                .mapToInt(p -> p.id)
                .toArray();

        String name = "Cup of " + nation.getName();
        play_group(players, name, 0, 1);
        println();

        kn[players[0]].addTrophy(name, year);
        return players;
    }

    private void play_group_round(int[] players, int points, int rounds) {
        int n = (players.length + 1) / 4;
        int[] buf = new int[4];
        for (int i = 1; i <= n; ++i) {
            for (int j = 0; j <= 3; ++j) {
                buf[j] = players[(i - 1) * 4 + j];
            }
            play_group(buf, "Group " + i, points, rounds);
            for (int j = 0; j <= 3; ++j) {
                players[(i - 1) * 4 + j] = buf[j];
            }
        }
        println();
    }

    private void play_playoff_round(int[] players, int[] winners, int[] loosers, int points) {
        int len = players.length;

        for (int i = 0; i < len / 2; ++i) {
            MatchResult mr = playPlayoffGame(kn[players[i * 2]], kn[players[i * 2 + 1]], points);
            if (mr.rounds.r1 > mr.rounds.r2) {
                winners[i] = players[i * 2];
                loosers[i] = players[i * 2 + 1];
            } else {
                winners[i] = players[i * 2 + 1];
                loosers[i] = players[i * 2];
            }
            readln();
        }
        println();
    }

    private void play_series_playoff_round(int[] players, int[] winners, int[] loosers, int wins, int points) {
        int len = players.length;
        for (int i = 0; i < len / 2; ++i) {
            SimpleResult r = play_series(players[i * 2], players[i * 2 + 1], wins, points);
            if (r.r1 > r.r2) {
                winners[i] = players[i * 2];
                loosers[i] = players[i * 2 + 1];
            } else {
                winners[i] = players[i * 2 + 1];
                loosers[i] = players[i * 2];
            }
            readln();
        }
        println();
    }

    private SimpleResult play_series(int id1, int id2, int wins, int points) {
        SimpleResult r = new SimpleResult();
        while (r.r1 != wins && r.r2 != wins) {
            MatchResult mr = playPlayoffGame(kn[id1], kn[id2], points);
            readln();
            if (mr.rounds.r1 > mr.rounds.r2) {
                r.r1 += 1;
            } else {
                r.r2 += 1;
            }
        }
        return r;
    }

    private void play_group(int[] players, String groupName, int points, int rounds) {
        println(groupName);
        println();

        int len = players.length;
        print_players(players);

        GroupResult[] results = new GroupResult[len];
        for (int i = 0; i < players.length; i++) {
            GroupResult result = new GroupResult();
            result.playerId = players[i];
            results[i] = result;
        }


        for (int k = 1; k <= rounds; ++k) {
            if (len == 4) {
                play_group_match(results, 0, 2, points);
                play_group_match(results, 1, 3, points);
                play_group_match(results, 3, 0, points);
                play_group_match(results, 2, 1, points);
                play_group_match(results, 0, 1, points);
                play_group_match(results, 3, 2, points);
            } else if (len == 6) {
                play_group_match(results, 0, 5, points);
                play_group_match(results, 2, 4, points);
                play_group_match(results, 1, 3, points);

                play_group_match(results, 5, 2, points);
                play_group_match(results, 0, 1, points);
                play_group_match(results, 4, 3, points);

                play_group_match(results, 4, 5, points);
                play_group_match(results, 3, 0, points);
                play_group_match(results, 1, 2, points);

                play_group_match(results, 5, 1, points);
                play_group_match(results, 0, 4, points);
                play_group_match(results, 2, 3, points);

                play_group_match(results, 3, 5, points);
                play_group_match(results, 0, 2, points);
                play_group_match(results, 4, 1, points);
            } else {
                int[] buf = new int[len];
                int[] buf2 = new int[len];
                for (int i = 0; i < len; ++i) {
                    buf[i] = i;
                }

                int halflen = len / 2;
                int i = 1;

                while (i < len) {
                    if (i % 2 == 1 && i > 1) {
                        GroupResult[] bufresults = new GroupResult[len];
                        for (int j = 0; j < len; ++j) {
                            bufresults[j] = results[j];
                        }
                        sort_group_results(bufresults);
                        println(groupName);
                        print_group_results(bufresults);
                    }

                    for (int j = 0; j < halflen; ++j) {
                        play_group_match(results, buf[j], buf[j + halflen], points);
                    }

                    buf2[0] = buf[0];
                    buf2[1] = buf[halflen];
                    for (int j = 2; j < halflen; ++j) {
                        buf2[j] = buf[j - 1];
                    }
                    for (int j = halflen; j < len - 1; j++) {
                        buf2[j] = buf[j + 1];
                    }
                    buf2[len - 1] = buf[halflen - 1];
                    for (int j = 0; j < len; ++j) {
                        buf[j] = buf2[j];
                    }

                    i = i + 1;
                }
            }
        }

        sort_group_results(results);

        println(groupName);
        print_group_results(results);

        for (int i = 0; i < players.length; i++) {
            players[i] = results[i].playerId;
        }
    }

    private void print_players(int[] players) {
        Arrays.stream(players)
                .forEach(i -> println(kn[i].getPlayerName()));
        readln();
    }

    private void sort_group_results(GroupResult[] results) {
        Arrays.sort(results, reverseOrder(this::group_result_more));
    }

    private int group_result_more(GroupResult res1, GroupResult res2) {
        if (res1.roundsWon > res2.roundsWon) {
            return 1;
        } else if (res1.roundsWon < res2.roundsWon) {
            return -1;
        } else {
            int dif1 = res1.gamesWon - res1.gamesLost;
            int dif2 = res2.gamesWon - res2.gamesLost;
            if (dif1 > dif2) {
                return 1;
            } else if (dif1 < dif2) {
                return -1;
            } else {
                return elo.playerIsBetterThan(res1.playerId, res2.playerId);
            }
        }
    }

    private void print_group_results(GroupResult[] results) {
        int len = results.length;
        int maxNameLength = Arrays.stream(results)
                .map(r -> kn[r.playerId].getPlayerName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%d. %-" + (maxNameLength + 1) + "s %2d:%2d  %d";
        for (int i = 0; i < len; i++) {
            println(String.format(formatString, (i + 1), kn[results[i].playerId].getPlayerName(),
                    results[i].gamesWon, results[i].gamesLost, results[i].roundsWon));
        }
        readln();
    }

    private void play_group_match(GroupResult[] results, int id1, int id2, int points) {
        MatchResult mres = playGroupGame(kn[results[id1].playerId], kn[results[id2].playerId], points);
        results[id1].roundsWon += mres.rounds.r1;
        results[id1].gamesWon += mres.games.r1;
        results[id1].gamesLost += mres.games.r2;
        results[id2].roundsWon += mres.rounds.r2;
        results[id2].gamesWon += mres.games.r2;
        results[id2].gamesLost += mres.games.r1;
        readln();
    }

    private MatchResult playGroupGame(Player p1, Player p2, int points) {
        return playGame(p1, p2, false, points);
    }

    private MatchResult playPlayoffGame(Player p1, Player p2, int points) {
        return playGame(p1, p2, true, points);
    }

    private MatchResult playGame(Player p1, Player p2, boolean isPlayoff, int points) {
        MatchResult res = new MatchResult(p1.id, p2.id);

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
        nationRating.updateNationRatings(p1, p2, res, points);

        return res;
    }

    private void updateElo(Player p1, Player p2, MatchResult mr) {
        double s = mr.rounds.r1 + mr.rounds.r2;
        elo.update(p1.id, p2.id, mr.rounds.r1 / s, mr.rounds.r2 / s);
    }

    private void updateExp(Player p1, Player p2, MatchResult mr) {
        if (mr.rounds.r1 > mr.rounds.r2) {
            p1.addExp(p2.getLevel());
        } else if (mr.rounds.r2 > mr.rounds.r1) {
            p2.addExp(p1.getLevel());
        }
    }

    private static SimpleResult playGameRound(Player p1, Player p2, int len) {
        int[] d1 = p1.getShuffledDeck();
        int[] d2 = p2.getShuffledDeck();

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
            int[] d1 = p1.getShuffledDeck();
            int[] d2 = p2.getShuffledDeck();
            for (int i = 0; i < 20; i += 2) {
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

    private void shuffle(int[] a, boolean fed_check) {
        boolean done = false;
        int len = a.length;
        int[] temp = new int[len];
        while (!done) {
            for (int i = 0; i < len - 1; ++i) {
                int r = random(len - i);
                temp[i] = a[r];
                int k = a[len - i - 1];
                a[len - i - 1] = a[r];
                a[r] = k;
            }

            temp[len - 1] = a[0];

            if (!fed_check) {
                done = true;
            } else {
                done = true;
                for (int i = 0; i < len; ++i) {
                    if (i % 2 == 0) {
                        if (kn[temp[i]].getNation() == kn[temp[i + 1]].getNation()) {
                            done = false;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void advancePlayersAge() {
        Arrays.stream(kn).forEach(Player::advanceAge);
    }

    private void performEndOfSeasonAdjustments() {
        nationRating.normalizeCurrentYearRating();
        Arrays.stream(kn).forEach(Player::applyRandomDeckChanges);

        if (year % 2 == 0) {
            decreaseBestPlayerSkills();
        }
    }

    private void decreaseBestPlayerSkills() {
        Player bestPlayer = elo.getPlayersByRating().get(0);
        bestPlayer.decreaseDeck();
        bestPlayer.decreaseDeck();
        println(bestPlayer.getPlayerName() + " has decreased his skill.");
        println();
        readln();
    }

    private void playTitlePlayoffs() {
        List<Player> playersByRating = elo.getPlayersByRating();
        List<Player> playersByRatingReversed = Lists.reverse(playersByRating);

        int worstLord = playersByRatingReversed.stream()
                .filter(p -> p.getTitle() == LORD)
                .map(p -> p.id)
                .findFirst().orElseThrow(RuntimeException::new);

        int worstSir = playersByRatingReversed.stream()
                .filter(p -> p.getTitle() == SIR)
                .map(p -> p.id)
                .findFirst().orElseThrow(RuntimeException::new);

        int bestSir = playersByRating.stream()
                .filter(p -> p.getTitle() == SIR)
                .map(p -> p.id)
                .findFirst().orElseThrow(RuntimeException::new);

        int bestCommon = playersByRating.stream()
                .filter(p -> p.getTitle() == COMMON)
                .map(p -> p.id)
                .findFirst().orElseThrow(RuntimeException::new);


        println("Titul playoffs  - Season " + year);
        println();

        println("Sir play off");
        println();
        SimpleResult r = play_series(worstSir, bestCommon, 3, 0);
        //playPlayoffGame(worst_sir, best_common, r1, r2, t, 0);

        if (r.r2 > r.r1) {
            kn[worstSir].setTitle(COMMON);
            kn[bestCommon].setTitle(SIR);
            println("Knight " + kn[bestCommon].getSimplePlayerName() + " has gained the Sir title!");
        } else {
            println("Knight " + kn[worstSir].getSimplePlayerName() + " has defended the Sir title!");
        }
        readln();

        println();

        println("Lord play off");
        println();
        //playPlayoffGame(worst_lord, best_sir, r1, r2, t, 0);
        r = play_series(worstLord, bestSir, 3, 0);

        if (r.r2 > r.r1) {
            kn[worstLord].setTitle(SIR);
            kn[bestSir].setTitle(LORD);
            println("Knight " + kn[bestSir].getSimplePlayerName() + " has gained the Lord title!");
        } else {
            println("Knight " + kn[worstLord].getSimplePlayerName() + " has defended the Lord title!");
        }
        readln();
        println();
    }

    private int selectPlayerToRetire() {
        double totalWeight = 0;
        double[] weights = new double[PLAYER_COUNT];
        for (int i = 0; i < 30; ++i) {
            weights[i] = Math.exp(kn[i].getAge() / 20.0);
            //print("Weight " + i + 1 + ": ");
            //println(weights[i]:5:2);
            totalWeight = totalWeight + weights[i];
        }
        //write("Total weight: ");
        //println(totalWeight:5:2);

        double r = random() * totalWeight;
        //write("Random number: ");
        //println(r:5:2);
        int id = -1;
        for (int i = 30 - 1; i >= 0; --i) {
            if (totalWeight - weights[i] <= r) {
                //println("Found interval for weight " + inttostr(i));
                id = i;
                break;
            } else {
                totalWeight = totalWeight - weights[i];
                //write("Not found interval, next weight: ");
                //println(totalWeight:5:2);
            }
        }
        return id;
    }

    private void retireRandomPlayer() {
        int id = selectPlayerToRetire();

        Player retiredPlayer = kn[id];
        println("Knight %s has retired at the age of %d", retiredPlayer.getPlayerName(), retiredPlayer.getAge());
        Map<String, Long> trophies = retiredPlayer.getTrophiesByType();
        if (!trophies.isEmpty()) {
            println("Knight's achievements are:");
            for (Map.Entry<String, Long> entry : trophies.entrySet()) {
                println(entry.getKey() + ": " + entry.getValue());
            }
            println();
        }

        retiredPlayer.restartCareer(true);
        elo.resetPlayer(retiredPlayer);

        if (retiredPlayer.getTitle() == LORD) {
            List<Player> playersByRating = elo.getPlayersByRating();
            Player bestSir = playersByRating.stream()
                    .filter(p -> p.getTitle() == SIR)
                    .findFirst().orElseThrow(RuntimeException::new);
            bestSir.setTitle(LORD);
            retiredPlayer.setTitle(SIR);
        }

        if (retiredPlayer.getTitle() == SIR) {
            List<Player> playersByRating = elo.getPlayersByRating();
            Player bestCommon = playersByRating.stream()
                    .filter(p -> p.getTitle() == COMMON)
                    .findFirst().orElseThrow(RuntimeException::new);
            bestCommon.setTitle(SIR);
            retiredPlayer.setTitle(COMMON);
        }

        println();
        readln();
    }

    private void world_cup() {
        println();
        println("World Championship");

        int[] dummy = new int[20];
        int[] ro1 = new int[8];
        int[] ro1sf = new int[4];
        int[] ro2 = new int[16];
        int[] gr = new int[16];
        int[] qf = new int[8];
        int[] sf = new int[4];
        int[] f = new int[2];

        int[] buf8 = new int[8];
        int[] buf16 = new int[16];

        List<Player> playersByRating = elo.getPlayersByRating();
        for (int i = 22; i < 30; ++i) {
            ro1[i - 22] = playersByRating.get(i).id;
        }
        for (int i = 8; i < 22; ++i) {
            ro2[i - 6] = playersByRating.get(i).id;
        }
        for (int i = 0; i < 8; ++i) {
            gr[i + 8] = playersByRating.get(i).id;
        }

        println();
        println("First Round");
        println();

        print_players(ro1);

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 1; ++j) {
                int r = random(2);
                while (buf8[i + r * 4] != 0) {
                    r = random(2);
                }
                buf8[i + r * 4] = ro1[i * 2 + j];
            }
        }

        for (int i = 0; i <= 7; ++i) {
            ro1[i] = buf8[i];
        }

        println("Group Round");
        println();

        play_group_round(ro1, 0, 2);

        ro1sf[0] = ro1[0];
        ro1sf[1] = ro1[5];
        ro1sf[2] = ro1[4];
        ro1sf[3] = ro1[1];

        println("First Round Semifinals");
        println();
        play_playoff_round(ro1sf, ro2, dummy, 0);

        println("Second Round");
        println();

        elo.sortPlayers(ro2);
        print_players(ro2);

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 3; ++j) {
                int r = random(4);
                while (buf16[i + r * 4] != 0) {
                    r = random(4);
                }
                buf16[i + r * 4] = ro2[i * 4 + j];
            }
        }

        for (int i = 0; i <= 15; ++i) {
            ro2[i] = buf16[i];
        }

        println("Group Round");
        println();
        play_group_round(ro2, 0, 2);

        //readlnt(t);

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 1; ++j) {
                gr[i * 2 + j] = ro2[i * 4 + j];
            }
        }

        println("Final Round");
        println();
        elo.sortPlayers(gr);
        print_players(gr);

        buf16 = new int[16];

        for (int i = 0; i <= 3; ++i) {

            for (int j = 0; j <= 3; ++j) {

                int r = random(4);
                while (buf16[i + r * 4] != 0) {
                    r = random(4);
                }
                buf16[i + r * 4] = gr[i * 4 + j];
            }
        }

        for (int i = 0; i <= 15; ++i) {
            gr[i] = buf16[i];
        }

        println("Group Round");
        println();
        play_group_round(gr, 0, 2);

        qf[0] = gr[0];
        qf[1] = gr[9];
        qf[2] = gr[4];
        qf[3] = gr[13];
        qf[4] = gr[8];
        qf[5] = gr[1];
        qf[6] = gr[12];
        qf[7] = gr[5];

        println("Quarterfinals");
        println();
        play_series_playoff_round(qf, sf, dummy, 4, 0);

        println("Semifinals");
        println();
        play_series_playoff_round(sf, f, dummy, 4, 0);

        println("Final");
        println();

        SimpleResult r = play_series(f[0], f[1], 4, 0);
        int wc;
        if (r.r1 > r.r2) {
            wc = f[0];
        } else {
            wc = f[1];
        }
        //for i = 1 to 30 do if sl[i].id=Wc then sl[i].os=sl[i].os+20;

        readln();

        println(kn[wc].getNation().getName() + " knight " + kn[wc].getPlayerName() + " is the World Champion!!!");
        println("It is the best day in the history of " + kn[wc].getTown() + "!");
        println("Everyone from " + kn[wc].getNation().getName() + " are celebrating!");
        println("Grand Master " + kn[wc].getSimplePlayerName() + " is now in the history!");

        kn[wc].addTrophy("World Cup", year);

        readln();
    }

    private void playLeagues() {
        playLeague(25, 30, "Division D", 0);
        playLeague(17, 24, "Division C", 0);
        playLeague(9, 16, "Division B", 0);
        playLeague(1, 8, "Division A", 0);

        //playCup();

        ArrayUtils.swap(leagues, 7, 8);
        ArrayUtils.swap(leagues, 15, 16);
        ArrayUtils.swap(leagues, 23, 24);
    }

    private void playLeague(int first, int last, String name, int points) {
        int[] league = new int[last - first + 1];
        for (int i = first; i <= last; ++i) {
            league[i - first] = leagues[i - 1];
        }
        play_group(league, name, points, 1);
        println();

        int lastp = league.length - 1;
        if (points != 1) {
            println(name + " - relegation match");
            SimpleResult r = play_series(league[lastp - 1], league[lastp], 3, 0);
            if (r.r2 > r.r1) {
                int j = league[lastp];
                league[lastp] = league[lastp - 1];
                league[lastp - 1] = j;
            }
        }

        println(name + " - final match");
        SimpleResult r = play_series(league[0], league[1], 3, 0);
        if (r.r2 > r.r1) {
            int j = league[0];
            league[0] = league[1];
            league[1] = j;
        }

        for (int i = first; i <= last; ++i) {
            leagues[i - 1] = league[i - first];
        }

        println();
        println("Knight " + kn[league[0]].getPlayerName() + " is the winner of the " + name + "!");
        if (points != 1) {
            println("Knight " + kn[league[lastp]].getPlayerName() + " have been relegated.");
        }

        println();

        kn[league[0]].addTrophy(name, year);
    }

    private Team makeNationalTeam(Nation nation) {
        Team res = new Team();
        res.name = nation.getName();

        List<Player> playersByRating = elo.getPlayersByRating();
        res.id = playersByRating.stream()
                .filter(p -> p.getNation() == nation)
                .limit(3)
                .mapToInt(p -> p.id)
                .toArray();

        return res;
    }

    private void playNationalWorldCup() {
        Map<Nation, Team> teams = new HashMap<>();
        for (Nation nation : Nation.values()) {
            teams.put(nation, makeNationalTeam(nation));
        }

        println(String.format("National World Cup - Season %d", year));
        println();

        println("Participants");
        println();
        for (int i = 0; i < Nation.COUNT; i++) {
            Team team = teams.get(nationRating.getRankedNation(i));
            println(team.name);
            for (int j = 0; j < team.id.length; j++) {
                print(String.format("%d: %s", j + 1, kn[team.id[j]].getPlayerName()));
                if (j == 0) {
                    println(" - Captain");
                } else {
                    println();
                }
            }
            println();
        }

        Team[] sf = new Team[4];
        sf[0] = teams.get(nationRating.getRankedNation(0));
        sf[2] = teams.get(nationRating.getRankedNation(1));
        sf[3] = teams.get(nationRating.getRankedNation(2));

        // Quaterfinal
        MatchResult mr = playTeamMatch(teams.get(nationRating.getRankedNation(3)), teams.get(nationRating.getRankedNation(4)));
        if (mr.rounds.r1 > mr.rounds.r2) {
            sf[1] = teams.get(nationRating.getRankedNation(3));
        } else {
            sf[1] = teams.get(nationRating.getRankedNation(4));
        }

        // Semifinals
        Team[] f = new Team[2];
        mr = playTeamMatch(sf[0], sf[1]);
        if (mr.rounds.r1 > mr.rounds.r2) {
            f[0] = sf[0];
        } else {
            f[0] = sf[1];
        }
        mr = playTeamMatch(sf[2], sf[3]);
        if (mr.rounds.r1 > mr.rounds.r2) {
            f[1] = sf[2];
        } else {
            f[1] = sf[3];
        }

        // Final
        mr = playTeamMatch(f[0], f[1]);
        Team winner;
        if (mr.rounds.r1 > mr.rounds.r2) {
            winner = f[0];
        } else {
            winner = f[1];
        }

        println(String.format("%s is the winner of the National World Cup %d", winner.name, year));
        println();
        readln();

        for (int i : winner.id) {
            kn[i].addTrophy("National World Cup", year);
        }
    }

    private MatchResult playTeamMatch(Team team1, Team team2) {
        MatchResult res = new MatchResult(-1, -1);

        int[] buf1 = new int[3];
        int[] buf2 = new int[3];
        copyArray(team1.id, buf1, 3, 0, 0);
        copyArray(team2.id, buf2, 3, 0, 0);

        println(String.format("%s vs %s", team1.name, team2.name));
        println();

        for (int j = 0; j <= 2; ++j) {
            for (int i = 0; i <= 2; ++i) {
                MatchResult mres = playPlayoffGame(kn[buf1[i]], kn[buf2[i]], 0);
                res.addSubMatchResult(mres.rounds);
                readln();

                if (res.rounds.r1 == 5 || res.rounds.r2 == 5) {
                    break;
                }
            }

            if (res.rounds.r1 == 5 || res.rounds.r2 == 5) {
                break;
            }

            int b = buf2[0];
            for (int i = 0; i <= 1; ++i) {
                buf2[i] = buf2[i + 1];
            }
            buf2[2] = b;
        }

        println(String.format("%s vs %s - %d:%d (%d:%d)", team1.name, team2.name, res.rounds.r1, res.rounds.r2, res.games.r1, res.games.r2));
        println();
        readln();

        return res;
    }

    private void copyArray(int[] src, int[] dest, int count, int fromSrc, int fromDest) {
        int[] dummy = new int[count];
        for (int i = 0; i < count; ++i) {
            dummy[i] = src[fromSrc + i];
        }
        shuffle(dummy, false);
        for (int i = 0; i < count; ++i) {
            dest[fromDest + i] = dummy[i];
        }
    }

    private void printStatsToFile() {
        saveToFile(filename(FILE_NAME_STATS, true),
                writer -> {
                    writeLevels(writer);
                    writer.println();
                    writeAges(writer);
                    writer.println();
                    writeTrophies(writer);
                });
    }

    private static int getMaxNameLength(List<Player> players) {
        return players.stream()
                .map(Player::getPlayerName)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    private void writeLevels(PrintWriter writer) {
        List<Player> playersByLevel = Arrays.stream(kn)
                .sorted(comparingInt(Player::getLevel).reversed())
                .collect(toList());

        int maxNameLength = getMaxNameLength(playersByLevel);
        String formatString = "%-2d: %-" + (maxNameLength + 1) + "s %-3d";

        writer.println("Levels");
        for (int i = 0; i < playersByLevel.size(); i++) {
            Player p = playersByLevel.get(i);
            writer.println(String.format(formatString, (i + 1), p.getPlayerName(), p.getLevel()));
        }
    }

    private void writeAges(PrintWriter writer) {
        List<Player> playersByAge = Arrays.stream(kn)
                .sorted(comparingInt(Player::getAge).reversed())
                .collect(toList());

        int maxNameLength = getMaxNameLength(playersByAge);
        String formatString = "%-2d: %-" + (maxNameLength + 1) + "s %-3d";

        writer.println("Ages");
        for (int i = 0; i < playersByAge.size(); i++) {
            Player p = playersByAge.get(i);
            writer.println(String.format(formatString, (i + 1), p.getPlayerName(), p.getAge()));
        }
    }

    private void writeTrophies(PrintWriter writer) {
        writer.println("Trophies");

        for (Player player : kn) {
            Map<String, Long> trophies = player.getTrophiesByType();
            if (trophies.isEmpty()) {
                continue;
            }
            writer.println();
            writer.println(player.getPlayerName());
            for (Map.Entry<String, Long> entry : trophies.entrySet()) {
                writer.println(entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}
