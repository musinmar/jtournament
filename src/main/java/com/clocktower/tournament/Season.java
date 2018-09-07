package com.clocktower.tournament;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.clocktower.tournament.Logger.*;
import static com.clocktower.tournament.Player.LEVEL_UP_COEFFICIENT;
import static com.clocktower.tournament.RandomUtils.random;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class Season {

    private static final int PLAYER_COUNT = 30;
    private static final int NATION_COUNT = 5;
    private static final int SEASON_COUNT = 4;
    private static final int NATION_SIZE = 6;

    private static final String FILE_NAME_KNIGHTS = "knights";
    private static final String FILE_NAME_SEASON = "season";
    private static final String FILE_NAME_ELO = "elo";
    private static final String FILE_NAME_RATING = "rating";
    private static final String FILE_NAME_RATING_CHANGE = "rating change";

    private int year;
    private final String FOLDER = "season";

    private Player[] kn = new Player[PLAYER_COUNT];
    private EloRating elo = new EloRating();
    private NationRatingItem[] nation_rating = new NationRatingItem[NATION_COUNT];
    private int[][] nations = new int[NATION_COUNT][NATION_SIZE];
    private int[] nation_pos = new int[NATION_COUNT];

    private int[] leagues = new int[PLAYER_COUNT];

    public static class NationRatingItem {
        public double[] seasons = new double[SEASON_COUNT];
    }

    public static class TajmResult {
        int r1;
        int r2;
    }

    public static class MatchResult {
        int id1;
        int id2;
        int rw1;
        int rw2;
        int gw1;
        int gw2;
    }

    public static class GroupResult {
        int id;
        int rounds_won;
        int games_won;
        int games_lost;
    }

    public static class Team {
        String name;
        int[] id;
    }

    public Season() {
        Arrays.setAll(nation_rating, i -> new NationRatingItem());
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

        writeFederationsTable();
        makeSeasonNationPos();
        eraseSeasonNationsData();

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

        if (year % 2 == 0) {
            retireRandomPlayer();
        }

        elo.advanceYear();

        play_titul_playoffs();
        end_of_season_adjust();

        saveToFile(filename(FILE_NAME_RATING, true),
                writer -> elo.print(writer, false));

        year += 1;
        writeFederationsTable();
        save();

// assign(t, filename(FILE_NAME_LEVELS, true));
// rewrite(t);
// for i:=1 to 30 do
//  begin
//   str(kn[i].level,s1);
//   writeln(t,kn[i].titul,kn[i].name,' ',kn[i].surname,' ',s1);
//  end;
// close(t);
//
// assign(t, filename(FILE_NAME_DOST, true));
// rewrite(t);
// for i:=1 to 30 do
//  begin
//   writeln(t,kn[i].titul,kn[i].name,' ',kn[i].surname);
//   for j:=1 to kn[i].dost.Count do writeln(t, kn[i].dost[j - 1]);
//  end;
// close(t);

        Logger.closeCurrentFile();
    }

    private void initNewGame() {
        DefaultData.initDefaultPlayers(kn);
        DefaultData.initDecks(kn);

        elo.init(kn);

        List<Player> playersByLevel = Arrays.stream(kn)
                .sorted(comparingInt((Player p) -> p.level).reversed())
                .collect(toList());
        for (int i = 0; i < PLAYER_COUNT; i++) {
            leagues[i] = playersByLevel.get(i).id;
        }

        year = 1;

        for (int i = 0; i < SEASON_COUNT; i++) {
            nation_rating[0].seasons[i] = 3.5;
            nation_rating[1].seasons[i] = 3;
            nation_rating[2].seasons[i] = 2.5;
            nation_rating[3].seasons[i] = 4.5;
            nation_rating[4].seasons[i] = 4;
        }

        for (Player player : kn) {
            player.persistentLevel = player.level;
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
            Arrays.stream(nation_rating)
                    .flatMapToDouble(nationRatingItem -> Arrays.stream(nationRatingItem.seasons))
                    .forEach(writer::println);
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
            for (int i = 0; i < NATION_COUNT; i++) {
                for (int j = 0; j < SEASON_COUNT; j++) {
                    nation_rating[i].seasons[j] = sc.nextDouble();
                }
            }
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

    private void writeFederationsTable() {
        println("Federations table");
        int index = 0;
        for (NationRatingItem item : nation_rating) {
            print(String.format("%d) %-11s", index + 1, Nation.fromId(index).getName()));
            Arrays.stream(item.seasons).forEach(d -> print(String.format("%7.2f", d)));
            println();
            ++index;
        }
        println();
    }

    private void makeSeasonNationPos() {
        double[] sums = Arrays.stream(nation_rating)
                .mapToDouble(item -> {
                    return Arrays.stream(item.seasons).sum();
                })
                .toArray();

        Arrays.setAll(nation_pos, i -> i);
        nation_pos = Arrays.stream(nation_pos)
                .boxed()
                .sorted(comparingDouble((Integer i) -> sums[i]).reversed())
                .mapToInt(i -> i)
                .toArray();

        println("Start of season federations ranking:");
        println();
        for (int i = 0; i < nation_pos.length; ++i) {
            int nationId = nation_pos[i];
            println(String.format("%d) %-11s %7.2f", i + 1, Nation.fromId(nationId).getName(), sums[nationId]));
        }
        readln();
    }

    private void eraseSeasonNationsData() {
        // TODO: make more incapsulated
        for (int i = 0; i < NATION_COUNT; ++i) {
            for (int j = SEASON_COUNT - 1; j >= 1; --j) {
                nation_rating[i].seasons[j] = nation_rating[i].seasons[j - 1];
            }
            nation_rating[i].seasons[0] = 0;
        }
    }

    private void playTournaments() {
        for (int i = 0; i < NATION_COUNT; i++) {
            playNational(i);
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
        TajmResult r = play_series(fc_f[0], fc_f[1], 3, 1);
        int fc_winner;
        if (r.r1 > r.r2) {
            fc_winner = fc_f[0];
        } else {
            fc_winner = fc_f[1];
        }
        readln();
        println();
        println("Knight " + kn[fc_winner].name + " " + kn[fc_winner].surname + " is the winner of the Federation Cup!");
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
        println("Knight " + kn[cl_winner].name + " " + kn[cl_winner].surname + " is the winner of the Champions League!");
        println(kn[cl_winner].getNation().getName() + "\'s triumph!");
        println();


        kn[fc_winner].addTrophy("FC", year);
        kn[cl_winner].addTrophy("CL", year);
    }

    private int get_player_from(int rank, int pos) {
        return nations[nation_pos[rank - 1]][pos - 1];
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

    private void playNational(int id) {
        Nation nation = Nation.fromId(id);

        int[] gr = IntStream.range(0, NATION_SIZE)
                .map(i -> id * NATION_SIZE + i)
                .toArray();

        play_group(gr, "Cup of " + nation.getName(), 0, 1);
        println();

        for (int i = 0; i < 6; i++) {
            nations[id][i] = gr[i];
        }
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
            MatchResult mr = boj_t(players[i * 2], players[i * 2 + 1], points);
            if (mr.rw1 > mr.rw2) {
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
            TajmResult r = play_series(players[i * 2], players[i * 2 + 1], wins, points);
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

    private TajmResult play_series(int id1, int id2, int wins, int points) {
        TajmResult r = new TajmResult();
        while (r.r1 != wins && r.r2 != wins) {
            MatchResult mr = boj_t(id1, id2, points);
            readln();
            if (mr.rw1 > mr.rw2) {
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
            result.id = players[i];
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
            players[i] = results[i].id;
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
        if (res1.rounds_won > res2.rounds_won) {
            return 1;
        } else if (res1.rounds_won < res2.rounds_won) {
            return -1;
        } else {
            int dif1 = res1.games_won - res1.games_lost;
            int dif2 = res2.games_won - res2.games_lost;
            if (dif1 > dif2) {
                return 1;
            } else if (dif1 < dif2) {
                return -1;
            } else {
                return elo.playerIsBetterThan(res1.id, res2.id);
            }
        }
    }

    private void print_group_results(GroupResult[] results) {
        int len = results.length;
        int maxNameLength = Arrays.stream(results)
                .map(r -> kn[r.id].getPlayerName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%d. %-" + (maxNameLength + 1) + "s %2d:%2d  %d";
        for (int i = 0; i < len; i++) {
            println(String.format(formatString, (i + 1), kn[results[i].id].getPlayerName(),
                    results[i].games_won, results[i].games_lost, results[i].rounds_won));
        }
        readln();
    }

    private void play_group_match(GroupResult[] results, int id1, int id2, int points) {
        MatchResult mres = bojgr_t(results[id1].id, results[id2].id, points);

//        if (mres.rw1 == 3 || mres.rw1 == 4) {
//            mres.rw1 = 3;
//        } else if (mres.rw1 == 2) {
//            mres.rw1 = 1;
//        } else {
//            mres.rw1 = 0;
//        }
//        if (mres.rw2 == 3 || mres.rw2 == 4) {
//            mres.rw2 = 3;
//        } else if (mres.rw2 == 2) {
//            mres.rw2 = 1;
//        } else {
//            mres.rw2 = 0;
//        }

        results[id1].rounds_won += mres.rw1;
        results[id1].games_won += mres.gw1;
        results[id1].games_lost += mres.gw2;
        results[id2].rounds_won += mres.rw2;
        results[id2].games_won += mres.gw2;
        results[id2].games_lost += mres.gw1;
        readln();
    }

    private MatchResult bojgr_t(int id1, int id2, int points) {
        MatchResult res = new MatchResult();
        res.rw1 = 0;
        res.rw2 = 0;
        res.id1 = id1;
        res.id2 = id2;
        res.gw1 = 0;
        res.gw2 = 0;

        println(kn[id1].getNameWithNation() + " vs " + kn[id2].getNameWithNation());

        // TODO: incapsulate
        TajmResult l = tajm(id1, id2, 9);
        res.gw1 += l.r1;
        res.gw2 += l.r2;
        print(l.r1 + ":" + l.r2 + " ");
        if (l.r1 > l.r2) {
            res.rw1 += +2;
        } else if (l.r2 > l.r1) {
            res.rw2 += +2;
        } else {
            res.rw1 += 1;
            res.rw2 += 1;
        }

        l = tajm(id1, id2, 9);
        res.gw1 += l.r1;
        res.gw2 += l.r2;
        print("/ " + l.r1 + ":" + l.r2 + " ");
        if (l.r1 > l.r2) {
            res.rw1 += +2;
        } else if (l.r2 > l.r1) {
            res.rw2 += +2;
        } else {
            res.rw1 += 1;
            res.rw2 += 1;
        }

        println("( " + res.rw1 + ":" + res.rw2 + " )");

        if (res.rw1 > res.rw2) {
            kn[id1].exp = kn[id1].exp + kn[id2].level;
            if (kn[id1].exp >= kn[id1].level * kn[id1].level * LEVEL_UP_COEFFICIENT) {
                kn[id1].levelup();
            }
            elo.update(id1, id2, 1, 0);
            nation_rating[kn[id1].getNation().getId()].seasons[0] = nation_rating[kn[id1].getNation().getId()].seasons[0] + points;
        } else if (res.rw2 > res.rw1) {
            kn[id2].exp = kn[id2].exp + kn[id1].level;
            if (kn[id2].exp >= kn[id2].level * kn[id2].level * LEVEL_UP_COEFFICIENT) {
                kn[id2].levelup();
            }
            elo.update(id1, id2, 0, 1);
            nation_rating[kn[id2].getNation().getId()].seasons[0] = nation_rating[kn[id2].getNation().getId()].seasons[0] + points;
        } else {
            elo.update(id1, id2, 0.5, 0.5);
            nation_rating[kn[id1].getNation().getId()].seasons[0] = nation_rating[kn[id1].getNation().getId()].seasons[0] + points / 2;
            nation_rating[kn[id2].getNation().getId()].seasons[0] = nation_rating[kn[id2].getNation().getId()].seasons[0] + points / 2;
        }

        return res;
    }

    private MatchResult boj_t(int id1, int id2, int points) {
        MatchResult res = new MatchResult();
        res.rw1 = 0;
        res.rw2 = 0;
        res.id1 = id1;
        res.id2 = id2;
        res.gw1 = 0;
        res.gw2 = 0;

        println(kn[id1].getNameWithNation() + " vs " + kn[id2].getNameWithNation());

        // TODO: incapsulate
        TajmResult l = tajm(id1, id2, 9);
        res.gw1 += l.r1;
        res.gw2 += l.r2;
        print(l.r1 + ":" + l.r2 + " ");
        if (l.r1 > l.r2) {
            res.rw1 += +2;
        } else if (l.r2 > l.r1) {
            res.rw2 += +2;
        } else {
            res.rw1 += 1;
            res.rw2 += 1;
        }

        l = tajm(id1, id2, 9);
        res.gw1 += l.r1;
        res.gw2 += l.r2;
        print("/ " + l.r1 + ":" + l.r2 + " ");
        if (l.r1 > l.r2) {
            res.rw1 += +2;
        } else if (l.r2 > l.r1) {
            res.rw2 += +2;
        } else {
            res.rw1 += 1;
            res.rw2 += 1;
        }


        if (res.rw1 == res.rw2) {
            l = tajm(id1, id2, 7);
            print("/ e.t. " + l.r1 + ":" + l.r2 + " ");

            res.gw1 += l.r1;
            res.gw2 += l.r2;
            if (l.r1 > l.r2) {
                res.rw1 += 1;
            } else if (l.r2 > l.r1) {
                res.rw2 += 1;
            } else {
                l = penalty(id1, id2);
                print("/ pen. " + l.r1 + ":" + l.r2 + " ");
                if (l.r1 > l.r2) {
                    res.rw1 += 1;
                } else {
                    res.rw2 += 1;
                }
            }
        }

        println("( " + res.rw1 + ":" + res.rw2 + " )");

        if (res.rw1 > res.rw2) {
            kn[id1].exp = kn[id1].exp + kn[id2].level;
            if (kn[id1].exp >= kn[id1].level * kn[id1].level * LEVEL_UP_COEFFICIENT) {
                kn[id1].levelup();
            }
            nation_rating[kn[id1].getNation().getId()].seasons[0] = nation_rating[kn[id1].getNation().getId()].seasons[0] + points;
        } else if (res.rw2 > res.rw1) {
            kn[id2].exp = kn[id2].exp + kn[id1].level;
            if (kn[id2].exp >= kn[id2].level * kn[id2].level * LEVEL_UP_COEFFICIENT) {
                kn[id2].levelup();
            }
            nation_rating[kn[id2].getNation().getId()].seasons[0] = nation_rating[kn[id2].getNation().getId()].seasons[0] + points;
        }

        if (res.rw1 > res.rw2 && res.rw1 - res.rw2 >= 2) {
            elo.update(id1, id2, 1, 0);
        } else if (res.rw2 > res.rw1 && res.rw2 - res.rw1 >= 2) {
            elo.update(id1, id2, 0, 1);
        } else if (res.rw1 > res.rw2) {
            elo.update(id1, id2, 0.6, 0.4);
        } else if (res.rw2 > res.rw1) {
            elo.update(id1, id2, 0.4, 0.6);
        } else {
            elo.update(id1, id2, 0.5, 0.5);
        }

        return res;
    }

    private TajmResult tajm(int id1, int id2, int len) {
        // TODO: refactor to something more reasonable

        int[] t1 = new int[20];
        int[] t2 = new int[20];
        for (int i = 0; i < 20; i++) {
            t1[i] = i;
            t2[i] = i;
        }

        TajmResult r = new TajmResult();

        for (int i = 0; i < len; i++) {
            int l1 = random(20 - i);
            int l2 = random(20 - i);

            if (kn[id1].deck[t1[l1]] > kn[id2].deck[t2[l2]]) {
                r.r1 += 1;
            } else if (kn[id1].deck[t1[l1]] < kn[id2].deck[t2[l2]]) {
                r.r2 += 1;
            }

            for (int j = l1; j < 20 - i - 1; ++j) {
                t1[j] = t1[j + 1];
            }
            for (int j = l2; j < 20 - i - 1; ++j) {
                t2[j] = t2[j + 1];
            }
        }

        return r;
    }

    private TajmResult penalty(int id1, int id2) {
        int[] t1 = new int[20];
        int[] t2 = new int[20];
        for (int i = 0; i < 20; i++) {
            t1[i] = i;
            t2[i] = i;
        }

        TajmResult r = new TajmResult();

        for (int i = 1; i <= 3; ++i) {
            int k1 = 0;
            int k2 = 0;

            int l1 = random(21 - i * 2 + 1);
            int l2 = random(21 - i * 2 + 1);
            if (kn[id1].deck[t1[l1]] > kn[id2].deck[t2[l2]]) {
                k1 += 1;
            } else {
                k2 += 1;
            }

            for (int j = l1; j < 20 - 1; ++j) {
                t1[j] = t1[j + 1];
            }
            for (int j = l2; j < 20 - 1; ++j) {
                t2[j] = t2[j + 1];
            }

            l1 = random(21 - i * 2);
            l2 = random(21 - i * 2);
            if (kn[id2].deck[t2[l2]] > kn[id1].deck[t1[l1]]) {
                k2 += 1;
            } else {
                k1 += 1;
            }

            for (int j = l1; j < 20 - 1; ++j) {
                t1[j] = t1[j + 1];
            }
            for (int j = l2; j < 20 - 1; ++j) {
                t2[j] = t2[j + 1];
            }

            if (k1 == 2) {
                r.r1 += 1;
            }
            if (k2 == 2) {
                r.r2 += 1;
            }
            if (k1 == k2) {
                r.r1 += 1;
                r.r2 += 1;
            }
        }

        int i = 3;
        while (r.r1 == r.r2) {
            i += 1;
            if (i % 10 == 1) {
                for (int j = 0; j < 20; j++) {
                    t1[j] = j;
                    t2[j] = j;
                }
            }

            int k1 = 0;
            int k2 = 0;

            int t = i % 10;
            if (t == 0) {
                t = 10;
            }

            int l1 = random(21 - t * 2 + 1);
            int l2 = random(21 - t * 2 + 1);
            if (kn[id1].deck[t1[l1]] > kn[id2].deck[t2[l2]]) {
                k1 += 1;
            } else {
                k2 += 1;
            }

            for (int j = l1; j < 20 - 1; ++j) {
                t1[j] = t1[j + 1];
            }
            for (int j = l2; j < 20 - 1; ++j) {
                t2[j] = t2[j + 1];
            }

            l1 = random(21 - t * 2);
            l2 = random(21 - t * 2);
            if (kn[id2].deck[t2[l2]] > kn[id1].deck[t1[l1]]) {
                k2 += 1;
            } else {
                k1 += 1;
            }

            for (int j = l1; j < 20 - 1; ++j) {
                t1[j] = t1[j + 1];
            }
            for (int j = l2; j < 20 - 1; ++j) {
                t2[j] = t2[j + 1];
            }

            if (k1 == 2) {
                r.r1 += 1;
            }
            if (k2 == 2) {
                r.r2 += 1;
            }
            if (k1 == k2) {
                r.r1 += 1;
                r.r2 += 1;
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

    private void end_of_season_adjust() {
        nation_rating[nation_pos[0]].seasons[0] = nation_rating[nation_pos[0]].seasons[0] / 5;
        nation_rating[nation_pos[1]].seasons[0] = nation_rating[nation_pos[1]].seasons[0] / 5;
        nation_rating[nation_pos[2]].seasons[0] = nation_rating[nation_pos[2]].seasons[0] / 5;
        nation_rating[nation_pos[3]].seasons[0] = nation_rating[nation_pos[3]].seasons[0] / 4;
        nation_rating[nation_pos[4]].seasons[0] = nation_rating[nation_pos[4]].seasons[0] / 4;

        for (int i = 0; i < 30; ++i) {
            kn[i].applyRandomDeckChanges();
        }

        for (int i = 0; i < 30; ++i) {
            kn[i].age += 1;
        }

        if (year % 2 == 0) {
            Player bestPlayer = elo.getPlayersByRating().get(0);
            bestPlayer.decDeck();
            bestPlayer.decDeck();
            println(bestPlayer.getPlayerName() + " has decreased his skill.");
            println();
            readln();
        }
    }

    private void play_titul_playoffs() {
        List<Player> playersByRating = elo.getPlayersByRating();
        List<Player> playersByRatingReversed = Lists.reverse(playersByRating);

        int worstLord = playersByRatingReversed.stream()
                .filter(p -> p.titul.equals("Lord "))
                .map(p -> p.id)
                .findFirst().orElseThrow(RuntimeException::new);

        int worstSir = playersByRatingReversed.stream()
                .filter(p -> p.titul.equals("Sir "))
                .map(p -> p.id)
                .findFirst().orElseThrow(RuntimeException::new);

        int bestSir = playersByRating.stream()
                .filter(p -> p.titul.equals("Sir "))
                .map(p -> p.id)
                .findFirst().orElseThrow(RuntimeException::new);

        int bestCommon = playersByRating.stream()
                .filter(p -> p.titul.equals(""))
                .map(p -> p.id)
                .findFirst().orElseThrow(RuntimeException::new);


        println("Titul playoffs  - Season " + year);
        println();

        println("Sir play off");
        println();
        TajmResult r = play_series(worstSir, bestCommon, 3, 0);
        //boj_t(worst_sir, best_common, r1, r2, t, 0);

        if (r.r2 > r.r1) {
            kn[worstSir].titul = "";
            kn[bestCommon].titul = "Sir ";
            println("Knight " + kn[bestCommon].name + " " + kn[bestCommon].surname + " has gained the Sir title!");
        } else {
            println("Knight " + kn[worstSir].name + " " + kn[worstSir].surname + " has defended his Sir title!");
        }
        readln();

        println();

        println("Lord play off");
        println();
        //boj_t(worst_lord, best_sir, r1, r2, t, 0);
        r = play_series(worstLord, bestSir, 3, 0);

        if (r.r2 > r.r1) {
            kn[worstLord].titul = "Sir ";
            kn[bestSir].titul = "Lord ";
            println("Knight " + kn[bestSir].name + " " + kn[bestSir].surname + " has gained the Lord title!");
        } else {
            println("Knight " + kn[worstLord].name + " " + kn[worstLord].surname + " has defended his Lord title!");
        }
        readln();
        println();
    }

    private int selectPlayerToRetire() {
        double totalWeight = 0;
        double[] weights = new double[PLAYER_COUNT];
        for (int i = 0; i < 30; ++i) {
            weights[i] = Math.exp(kn[i].age / 20.0);
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

        println("Knight " + kn[id].getPlayerName() + " has retired at the age of " + kn[id].age);
        kn[id].restartCareer(true);
        kn[id].age = 0;
        elo.resetPlayer(kn[id]);

        if (kn[id].titul.equals("Lord ")) {
            List<Player> playersByRating = elo.getPlayersByRating();
            Player bestSir = playersByRating.stream()
                    .filter(p -> p.titul.equals("Sir "))
                    .findFirst().orElseThrow(RuntimeException::new);
            bestSir.titul = "Lord ";
            kn[id].titul = "Sir ";
        }

        if (kn[id].titul.equals("Sir ")) {
            List<Player> playersByRating = elo.getPlayersByRating();
            Player bestCommon = playersByRating.stream()
                    .filter(p -> p.titul.equals(""))
                    .findFirst().orElseThrow(RuntimeException::new);
            bestCommon.titul = "Sir ";
            kn[id].titul = "";
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

        TajmResult r = play_series(f[0], f[1], 4, 0);
        int wc;
        if (r.r1 > r.r2) {
            wc = f[0];
        } else {
            wc = f[1];
        }
        //for i = 1 to 30 do if sl[i].id=Wc then sl[i].os=sl[i].os+20;

        readln();

        println(kn[wc].getNation().getName() + " knight " + kn[wc].titul + kn[wc].name + " " + kn[wc].surname + " is the World Champion!!!");
        println("It is the best day in the history of " + kn[wc].town + "!");
        println("Everyone from " + kn[wc].getNation().getName() + " are celebrating!");
        println("Grand Master " + kn[wc].name + " " + kn[wc].surname + " is now in the history!");

        kn[wc].addTrophy("WC", year);

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
            TajmResult r = play_series(league[lastp - 1], league[lastp], 2, 0);
            if (r.r2 > r.r1) {
                int j = league[lastp];
                league[lastp] = league[lastp - 1];
                league[lastp - 1] = j;
            }
        }

        println(name + " - final match");
        TajmResult r = play_series(league[0], league[1], 3, 0);
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

        kn[league[0]].addTrophy(Character.toString(name.charAt(name.length() - 1)), year);
    }

    private Team makeNationalTeam(int nationId) {
        Team res = new Team();
        res.name = Nation.fromId(nationId).getName();

        List<Player> playersByRating = elo.getPlayersByRating();
        res.id = playersByRating.stream()
                .filter(p -> p.getNation().getId() == nationId)
                .limit(3)
                .mapToInt(p -> p.id)
                .toArray();

        return res;
    }

    private void playNationalWorldCup() {
        Team[] teams = new Team[5];
        for (int i = 0; i < 5; ++i) {
            teams[i] = makeNationalTeam(i);
        }

        println(String.format("National World Cup - Season %d", year));
        println();

        println("Participants");
        println();
        for (int i = 0; i < teams.length; i++) {
            Team team = teams[nation_pos[i]];
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

        int[] sf = new int[4];
        sf[0] = nation_pos[0];
        sf[2] = nation_pos[1];
        sf[3] = nation_pos[2];

        // Quaterfinal
        MatchResult mr = playTeamMatch(teams[nation_pos[3]], teams[nation_pos[4]]);
        if (mr.rw1 > mr.rw2) {
            sf[1] = nation_pos[3];
        } else {
            sf[1] = nation_pos[4];
        }

        // Semifinals
        int[] f = new int[2];
        mr = playTeamMatch(teams[sf[0]], teams[sf[1]]);
        if (mr.rw1 > mr.rw2) {
            f[0] = sf[0];
        } else {
            f[0] = sf[1];
        }
        mr = playTeamMatch(teams[sf[2]], teams[sf[3]]);
        if (mr.rw1 > mr.rw2) {
            f[1] = sf[2];
        } else {
            f[1] = sf[3];
        }

        // Final
        mr = playTeamMatch(teams[f[0]], teams[f[1]]);
        int winner;
        if (mr.rw1 > mr.rw2) {
            winner = f[0];
        } else {
            winner = f[1];
        }

        println(String.format("%s is the winner of the National World Cup %d", teams[winner].name, year));
        println();
        readln();
    }

    private MatchResult playTeamMatch(Team team1, Team team2) {
        MatchResult res = new MatchResult();
        res.rw1 = 0;
        res.rw2 = 0;
        res.gw1 = 0;
        res.gw2 = 0;

        int[] buf1 = new int[3];
        int[] buf2 = new int[3];
        copyArray(team1.id, buf1, 3, 0, 0);
        copyArray(team2.id, buf2, 3, 0, 0);

        println(String.format("%s vs %s", team1.name, team2.name));
        println();

        for (int j = 0; j <= 2; ++j) {
            for (int i = 0; i <= 2; ++i) {
                MatchResult mres = boj_t(buf1[i], buf2[i], 0);
                if (mres.rw1 > mres.rw2) {
                    res.rw1 += 1;
                } else {
                    res.rw2 += 1;
                }
                res.gw1 = res.gw1 + mres.rw1;
                res.gw2 = res.gw2 + mres.rw2;
                readln();

                if (res.rw1 == 5 || res.rw2 == 5) {
                    break;
                }
            }

            if (res.rw1 == 5 || res.rw2 == 5) {
                break;
            }

            int b = buf2[0];
            for (int i = 0; i <= 1; ++i) {
                buf2[i] = buf2[i + 1];
            }
            buf2[2] = b;
        }

        println(String.format("%s vs %s - %d:%d (%d:%d)", team1.name, team2.name, res.rw1, res.rw2, res.gw1, res.gw2));
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
}
