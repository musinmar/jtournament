package com.clocktower.tournament;

import com.clocktower.tournament.domain.DefaultData;
import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.dto.SeasonDto;
import com.clocktower.tournament.simulation.MatchResult;
import com.clocktower.tournament.simulation.PlayerSeriesResult;
import com.clocktower.tournament.simulation.SimpleResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static final String FILE_NAME_SEASON_JSON = "season.json";
    private static final String FILE_NAME_KNIGHTS = "knights";
    private static final String FILE_NAME_SEASON = "season";
    private static final String FILE_NAME_ELO = "elo";
    private static final String FILE_NAME_RATING = "rating";
    private static final String FILE_NAME_RATING_CHANGE = "rating change";
    private static final String FILE_NAME_STATS = "stats";

    private static final int NORMAL_TIME_LENGTH = 9;
    private static final int ADDITIONAL_TIME_LENGTH = 7;

    private static final int NATIONAL_TEAM_MATCH_ROUNDS = 1;

    private int year;
    private final String FOLDER = "season";

    private Player[] kn;
    private EloRating elo = new EloRating();

    private NationRating nationRating = new NationRating();
    private Map<Nation, int[]> nationalCupResults = new HashMap<>();

    private int[] leagues = new int[PLAYER_COUNT];

    public static class GroupResult {
        Player player;
        int roundsWon;
        int gamesWon;
        int gamesLost;
    }

    public static class Team {
        String name;
        int[] id;
    }

    public static class PlayerPair {
        final Player p1;
        final Player p2;

        public PlayerPair(Player p1, Player p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    public Season() {
    }

    public void init(boolean newGame) {
        if (newGame) {
            initNewGame();
        } else {
            load();
        }
    }

    public void simulateSeason() {
        println();

        String seasonLogFileName = makeFilename("season", true, true);
        Logger.setCurrentFilename(seasonLogFileName);

        nationRating.printPointHistory();
        nationRating.calculateRankingsAndPrint();

        playTournaments();
        //playLeagues();

//        {if (year - 4) mod 8 = 0 then begin
//        playWorldChampionship();
//        end;}

        if (year % 4 == 0) {
            playLeagues();
        }

        if ((year - 2) % 4 == 0) {
            playWorldCup();
        }

        if ((year - 1) % 2 == 0) {
            playNationalWorldCup();
        }

        saveToFile(makeFilename(FILE_NAME_RATING_CHANGE, true, true),
                writer -> elo.print(writer, true));

        advancePlayersAge();
        if (year % 2 == 0) {
            retireRandomPlayer();
        }

        elo.advanceYear();

        playTitlePlayoffs();
        adjustPlayerSkillsAfterSeason();

        saveToFile(makeFilename(FILE_NAME_RATING, true, true),
                writer -> elo.print(writer, false));

        printStatsToFile();

        year += 1;
        nationRating.advanceYear();
        nationRating.printPointHistory();
        save();

        Logger.closeCurrentFile();
    }

    private void initNewGame() {
        kn = DefaultData.initDefaultPlayers();
        for (Player player : kn) {
            player.restartCareer(false);
        }

        elo.init(kn);

        List<Player> playersByLevel = Arrays.stream(kn)
                .sorted(comparingInt(Player::getLevel).reversed())
                .collect(toList());
        for (int i = 0; i < PLAYER_COUNT; i++) {
            leagues[i] = playersByLevel.get(i).id;
        }

        year = 1;

        nationRating.initDefault();

        save();
    }

    private void save() {
        saveAsTxt();
        saveAsJson();
    }

    private void saveAsJson() {
        SeasonDto seasonDto = new SeasonDto();
        seasonDto.setYear(year);
        seasonDto.setPlayers(Arrays.stream(kn).map(Player::toDto).collect(toList()));
        seasonDto.setLeagues(leagues);
        seasonDto.setNationRating(nationRating.toDto());
        seasonDto.setEloRating(elo.toDto());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File file = new File(makeFilename(FILE_NAME_SEASON_JSON, false, false));
        try {
            mapper.writeValue(file, seasonDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveAsTxt() {
        saveToFile(makeFilename(FILE_NAME_KNIGHTS, false, true), writer -> {
            Arrays.stream(kn).forEach(p -> p.save(writer));
        });
        saveToFile(makeFilename(FILE_NAME_SEASON, false, true), writer -> {
            writer.println(year);
            nationRating.write(writer);
            Arrays.stream(leagues).forEach(writer::println);
        });
        saveToFile(makeFilename(FILE_NAME_ELO, false, true), elo::save);
    }

    private void saveToFile(String filename, Consumer<PrintWriter> writerConsumer) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writerConsumer.accept(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void load() {
        if (Files.exists(Paths.get(makeFilename(FILE_NAME_SEASON_JSON, false, false)))) {
            readFromJson();
        } else {
            readFromTxtFile();
        }
    }

    private void readFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        SeasonDto seasonDto;
        try {
            seasonDto = mapper.readValue(new File(makeFilename(FILE_NAME_SEASON_JSON, false, false)), SeasonDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        year = seasonDto.getYear();
        kn = seasonDto.getPlayers().stream().map(Player::fromDto).toArray(Player[]::new);
        leagues = seasonDto.getLeagues();
        nationRating = NationRating.fromDto(seasonDto.getNationRating());
        elo = EloRating.fromDto(seasonDto.getEloRating(), kn);
    }

    private void readFromTxtFile() {
        readFromFile(makeFilename(FILE_NAME_KNIGHTS, false, true), sc -> {
            kn = new Player[PLAYER_COUNT];
            for (int i = 0; i < PLAYER_COUNT; i++) {
                kn[i] = new Player();
                kn[i].load(sc);
            }
        });

        readFromFile(makeFilename(FILE_NAME_SEASON, false, true), sc -> {
            year = sc.nextInt();
            nationRating.read(sc);
            for (int i = 0; i < PLAYER_COUNT; i++) {
                leagues[i] = sc.nextInt() - 1;
            }
        });

        readFromFile(makeFilename(FILE_NAME_ELO, false, true), sc -> {
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

    private String makeFilename(String s, boolean withYear, boolean txtExtension) {
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
        if (txtExtension) {
            ret += ".txt";
        }
        return folderPath.resolve(ret).toAbsolutePath().toString();
    }

    private void playTournaments() {
        for (Nation nation : Nation.values()) {
            nationalCupResults.put(nation, playNationalCup(nation));
        }

        println("Champions League - Season " + year);
        println("First qualification round");
        println();

        List<Integer> clQualifyingRound1 = new ArrayList<>(Arrays.asList(
                get_player_from(1, 4),
                get_player_from(3, 3),
                get_player_from(4, 2),
                get_player_from(4, 3),
                get_player_from(5, 1),
                get_player_from(5, 2)));

        List<Integer> clQualifyingRound2 = new ArrayList<>(Arrays.asList(
                get_player_from(1, 3),
                get_player_from(2, 2),
                get_player_from(2, 3),
                get_player_from(3, 2),
                get_player_from(4, 1)));

        List<Integer> fcQualifyingRound1 = new ArrayList<>(Collections.singletonList(
                get_player_from(1, 5)));

        List<Integer> fcQualifyingRound2 = new ArrayList<>(Arrays.asList(
                get_player_from(2, 5),
                get_player_from(3, 5),
                get_player_from(4, 4),
                get_player_from(5, 4)));

        List<Integer> clQualifyingRound1Losers = new ArrayList<>();

        printParticipants(clQualifyingRound1);
        shuffle(clQualifyingRound1, true);

        playPlayoffRound(clQualifyingRound1, clQualifyingRound2, clQualifyingRound1Losers, 2);
        fcQualifyingRound1.addAll(clQualifyingRound1Losers);

        println("Federations Cup - Season " + year);
        println("First qualification round");
        println();

        printParticipants(fcQualifyingRound1);
        shuffle(fcQualifyingRound1, true);

        playPlayoffRound(fcQualifyingRound1, fcQualifyingRound2, new ArrayList<>(), 1);


        List<Integer> clGroupRound = new ArrayList<>(Arrays.asList(
                get_player_from(1, 1),
                get_player_from(1, 2),
                get_player_from(2, 1),
                get_player_from(3, 1)));

        List<Integer> fcGroupRound = new ArrayList<>(Arrays.asList(
                get_player_from(2, 4),
                get_player_from(3, 4),
                get_player_from(5, 3)));

        List<Integer> clQualificationRound2Losers = new ArrayList<>();

        println("Champions League - Second qualification round");
        println();

        printParticipants(clQualifyingRound2);
        shuffle(clQualifyingRound2, true);
        playPlayoffRound(clQualifyingRound2, clGroupRound, clQualificationRound2Losers, 2);
        fcQualifyingRound2.addAll(clQualificationRound2Losers);

        println("Federations Cup - Second qualification round");
        println();
        printParticipants(fcQualifyingRound2);
        shuffle(fcQualifyingRound2, true);
        playPlayoffRound(fcQualifyingRound2, fcGroupRound, new ArrayList<>(), 1);

        println("Federations Cup - Group round");
        println();
        elo.sortPlayers(fcGroupRound);
        printParticipants(fcGroupRound);
        makeGroups(fcGroupRound);
        List<Integer> fcGroupRoundResult = playGroupRound(fcGroupRound, 2, 1);

        List<Integer> fc_sf = new ArrayList<>(Arrays.asList(
                fcGroupRoundResult.get(0),
                fcGroupRoundResult.get(5),
                fcGroupRoundResult.get(4),
                fcGroupRoundResult.get(1)));

        println("Champions League - Group round");
        println();
        elo.sortPlayers(clGroupRound);
        printParticipants(clGroupRound);
        makeGroups(clGroupRound);
        List<Integer> clGroupRoundResult = playGroupRound(clGroupRound, 4, 1);

        List<Integer> cl_sf = new ArrayList<>(Arrays.asList(
                clGroupRoundResult.get(0),
                clGroupRoundResult.get(5),
                clGroupRoundResult.get(4),
                clGroupRoundResult.get(1)));

        println("Federations Cup - Semifinals");
        println("Semifinals");
        println();
        List<Integer> fc_f = new ArrayList<>();
        playSeriesPlayoffRound(fc_sf, fc_f, new ArrayList<>(), 3, 1);

        println("Champions League - Semifinal Group");
        println("Semifinals");
        println();
        List<Integer> cl_f = new ArrayList<>();
        playSeriesPlayoffRound(cl_sf, cl_f, new ArrayList<>(), 3, 2);

        println("Federations Cup - FINAL");
        println("Final");
        println();
        PlayerSeriesResult r = playSeries(kn[fc_f.get(0)], kn[fc_f.get(1)], 3, 1);
        Player fcWinner = r.getWinner();
        readln();
        println("Knight " + fcWinner.getPlayerName() + " is the winner of the Federation Cup!");
        println();

        println("Champions League - FINAL");
        println("Final");
        println();
        r = playSeries(kn[cl_f.get(0)], kn[cl_f.get(1)], 3, 2);
        Player clWinner = r.getWinner();
        readln();
        println("Knight " + clWinner.getPlayerName() + " is the winner of the Champions League!");
        println(clWinner.getNation().getName() + "\'s triumph!");
        println();


        fcWinner.addTrophy("Federations Cup", year);
        clWinner.addTrophy("Champions League", year);
    }

    private int get_player_from(int rank, int pos) {
        Nation nation = nationRating.getRankedNation(rank - 1);
        return nationalCupResults.get(nation)[pos - 1];
    }

    private void makeGroups(List<Integer> a) {
        List<Integer> b = new ArrayList<>(a.subList(0, 4));
        List<Integer> c = new ArrayList<>(a.subList(4, 8));
        shuffle(b, false);
        shuffle(c, false);
        a.clear();
        a.addAll(Arrays.asList(
                b.get(0),
                b.get(1),
                c.get(0),
                c.get(1),
                b.get(2),
                b.get(3),
                c.get(2),
                c.get(3)
        ));
    }

    private int[] playNationalCup(Nation nation) {
        int[] players = Arrays.stream(kn)
                .filter(p -> p.getNation() == nation)
                .mapToInt(p -> p.id)
                .toArray();

        String name = "Cup of " + nation.getName();
        playGroup(players, name, 0, 1);
        println();

        kn[players[0]].addTrophy(name, year);
        return players;
    }

    private List<Integer> playGroupRound(List<Integer> players, int points, int rounds) {
        List<Integer> result = new ArrayList<>();
        int n = (players.size() + 1) / 4;
        int[] buf = new int[4];
        for (int i = 1; i <= n; ++i) {
            for (int j = 0; j <= 3; ++j) {
                buf[j] = players.get((i - 1) * 4 + j);
            }
            playGroup(buf, "Group " + i, points, rounds);
            for (int j = 0; j <= 3; ++j) {
                result.add(buf[j]);
            }
        }
        println();
        return result;
    }

    private void playPlayoffRound(List<Integer> players, List<Integer> winners, List<Integer> losers, int points) {
        int len = players.size();
        for (int i = 0; i < len / 2; ++i) {
            MatchResult mr = playPlayoffGame(kn[players.get(i * 2)], kn[players.get(i * 2 + 1)], points);
            winners.add(mr.getWinner().id);
            losers.add(mr.getLoser().id);
            readln();
        }
        println();
    }

    private void playSeriesPlayoffRound(List<Integer> players, List<Integer> winners, List<Integer> losers, int wins, int points) {
        int len = players.size();
        for (int i = 0; i < len / 2; ++i) {
            PlayerSeriesResult r = playSeries(kn[players.get(i * 2)], kn[players.get(i * 2 + 1)], wins, points);
            winners.add(r.getWinner().id);
            losers.add(r.getLoser().id);
            readln();
        }
        println();
    }

    private PlayerSeriesResult playSeries(Player p1, Player p2, int wins, int points) {
        PlayerSeriesResult seriesResult = new PlayerSeriesResult(p1, p2);
        SimpleResult r = seriesResult.getResult();
        while (r.getTopScore() != wins) {
            MatchResult mr = playPlayoffGame(p1, p2, points);
            readln();
            if (mr.rounds.r1 > mr.rounds.r2) {
                r.r1 += 1;
            } else {
                r.r2 += 1;
            }
        }
        return seriesResult;
    }

    private void playGroup(int[] players, String groupName, int points, int rounds) {
        println(groupName);
        println();

        int len = players.length;
        printParticipants(players);

        GroupResult[] results = new GroupResult[len];
        for (int i = 0; i < players.length; i++) {
            GroupResult result = new GroupResult();
            result.player = kn[players[i]];
            results[i] = result;
        }


        for (int k = 1; k <= rounds; ++k) {
            if (len == 4) {
                playGroupMatch(results, 0, 2, points);
                playGroupMatch(results, 1, 3, points);
                playGroupMatch(results, 3, 0, points);
                playGroupMatch(results, 2, 1, points);
                playGroupMatch(results, 0, 1, points);
                playGroupMatch(results, 3, 2, points);
            } else if (len == 6) {
                playGroupMatch(results, 0, 5, points);
                playGroupMatch(results, 2, 4, points);
                playGroupMatch(results, 1, 3, points);

                playGroupMatch(results, 5, 2, points);
                playGroupMatch(results, 0, 1, points);
                playGroupMatch(results, 4, 3, points);

                playGroupMatch(results, 4, 5, points);
                playGroupMatch(results, 3, 0, points);
                playGroupMatch(results, 1, 2, points);

                playGroupMatch(results, 5, 1, points);
                playGroupMatch(results, 0, 4, points);
                playGroupMatch(results, 2, 3, points);

                playGroupMatch(results, 3, 5, points);
                playGroupMatch(results, 0, 2, points);
                playGroupMatch(results, 4, 1, points);
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
                        sortGroupResults(bufresults);
                        println(groupName);
                        printGroupResults(bufresults);
                    }

                    for (int j = 0; j < halflen; ++j) {
                        playGroupMatch(results, buf[j], buf[j + halflen], points);
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

        sortGroupResults(results);

        println(groupName);
        printGroupResults(results);

        for (int i = 0; i < players.length; i++) {
            players[i] = results[i].player.id;
        }
    }

    private void printParticipants(int[] players) {
        Arrays.stream(players)
                .forEach(i -> println(kn[i].getPlayerName()));
        readln();
    }

    private void printParticipants(List<Integer> players) {
        players.forEach(i -> println(kn[i].getPlayerName()));
        readln();
    }

    private void sortGroupResults(GroupResult[] results) {
        Arrays.sort(results, reverseOrder(this::compareGroupResults));
    }

    private int compareGroupResults(GroupResult res1, GroupResult res2) {
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
                return elo.playerIsBetterThan(res1.player, res2.player);
            }
        }
    }

    private void printGroupResults(GroupResult[] results) {
        int len = results.length;
        int maxNameLength = Arrays.stream(results)
                .map(r -> r.player.getPlayerName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        String formatString = "%d. %-" + (maxNameLength + 1) + "s %2d:%2d  %d";
        for (int i = 0; i < len; i++) {
            println(String.format(formatString, (i + 1), results[i].player.getPlayerName(),
                    results[i].gamesWon, results[i].gamesLost, results[i].roundsWon));
        }
        readln();
    }

    private void playGroupMatch(GroupResult[] results, int id1, int id2, int points) {
        MatchResult r = playGroupGame(results[id1].player, results[id2].player, points);
        results[id1].roundsWon += r.rounds.r1;
        results[id1].gamesWon += r.games.r1;
        results[id1].gamesLost += r.games.r2;
        results[id2].roundsWon += r.rounds.r2;
        results[id2].gamesWon += r.games.r2;
        results[id2].gamesLost += r.games.r1;
        readln();
    }

    private MatchResult playGroupGame(Player p1, Player p2, int points) {
        return playGame(p1, p2, false, points);
    }

    private MatchResult playPlayoffGame(Player p1, Player p2, int points) {
        return playGame(p1, p2, true, points);
    }

    private MatchResult playGame(Player p1, Player p2, boolean isPlayoff, int points) {
        MatchResult res = new MatchResult(p1, p2);

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
        nationRating.updateRatings(p1, p2, res, points);

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

    private void shuffle(List<Integer> a, boolean checkNoSameFederationPairs) {
        boolean done = false;
        while (!done) {
            Collections.shuffle(a);
            if (!checkNoSameFederationPairs) {
                done = true;
            } else {
                done = true;
                for (int i = 0; i < a.size(); i += 2) {
                    if (kn[a.get(i)].getNation() == kn[a.get(i + 1)].getNation()) {
                        done = false;
                        break;
                    }
                }
            }
        }
    }

    private void advancePlayersAge() {
        Arrays.stream(kn).forEach(Player::advanceAge);
    }

    private void adjustPlayerSkillsAfterSeason() {
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

        Player worstLord = playersByRatingReversed.stream()
                .filter(p -> p.getTitle() == LORD)
                .findFirst().orElseThrow(RuntimeException::new);

        Player worstSir = playersByRatingReversed.stream()
                .filter(p -> p.getTitle() == SIR)
                .findFirst().orElseThrow(RuntimeException::new);

        Player bestSir = playersByRating.stream()
                .filter(p -> p.getTitle() == SIR)
                .findFirst().orElseThrow(RuntimeException::new);

        Player bestCommon = playersByRating.stream()
                .filter(p -> p.getTitle() == COMMON)
                .findFirst().orElseThrow(RuntimeException::new);


        println("Titul playoffs  - Season " + year);
        println();

        println("Sir play off");
        println();
        PlayerSeriesResult r = playSeries(worstSir, bestCommon, 3, 0);
        //playPlayoffGame(worst_sir, best_common, r1, r2, t, 0);

        if (r.getWinner() == bestCommon) {
            worstSir.setTitle(COMMON);
            bestCommon.setTitle(SIR);
            println("Knight " + bestCommon.getSimplePlayerName() + " has gained the Sir title!");
        } else {
            println("Knight " + worstSir.getSimplePlayerName() + " has defended the Sir title!");
        }
        readln();

        println();

        println("Lord play off");
        println();
        //playPlayoffGame(worst_lord, best_sir, r1, r2, t, 0);
        r = playSeries(worstLord, bestSir, 3, 0);

        if (r.getWinner() == bestSir) {
            worstLord.setTitle(SIR);
            bestSir.setTitle(LORD);
            println("Knight " + bestSir.getSimplePlayerName() + " has gained the Lord title!");
        } else {
            println("Knight " + worstLord.getSimplePlayerName() + " has defended the Lord title!");
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

    private void playWorldCup() {
        println();
        println("World Championship");

        int[] buf8 = new int[8];
        int[] buf16 = new int[16];

        List<Player> playersByRating = elo.getPlayersByRating();
        List<Integer> ro1 = playersByRating.subList(22, 30).stream().map(p -> p.id).collect(toList());
        List<Integer> ro2 = playersByRating.subList(8, 22).stream().map(p -> p.id).collect(toList());
        List<Integer> gr = playersByRating.subList(0, 8).stream().map(p -> p.id).collect(toList());

        println();
        println("First Round");
        println();

        printParticipants(ro1);

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 1; ++j) {
                int r = random(2);
                while (buf8[i + r * 4] != 0) {
                    r = random(2);
                }
                buf8[i + r * 4] = ro1.get(i * 2 + j);
            }
        }

        ro1 = Arrays.stream(buf8).boxed().collect(toList());

        println("Group Round");
        println();

        playGroupRound(ro1, 0, 2);

        List<Integer> ro1sf = new ArrayList<>(Arrays.asList(
                ro1.get(0),
                ro1.get(5),
                ro1.get(4),
                ro1.get(1)));

        println("First Round Semifinals");
        println();
        List<Integer> ro1sfWinners = new ArrayList<>();
        playSeriesPlayoffRound(ro1sf, ro1sfWinners, new ArrayList<>(), 4, 0);
        ro2.addAll(ro1sfWinners);

        println("Second Round");
        println();

        elo.sortPlayers(ro2);
        printParticipants(ro2);

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 3; ++j) {
                int r = random(4);
                while (buf16[i + r * 4] != 0) {
                    r = random(4);
                }
                buf16[i + r * 4] = ro2.get(i * 4 + j);
            }
        }

        ro2 = Arrays.stream(buf16).boxed().collect(toList());

        println("Group Round");
        println();
        List<Integer> ro2Winners = playGroupRound(ro2, 0, 2);
        gr.addAll(ro2Winners);

        println("Final Round");
        println();
        elo.sortPlayers(gr);
        printParticipants(gr);

        buf16 = new int[16];

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 3; ++j) {
                int r = random(4);
                while (buf16[i + r * 4] != 0) {
                    r = random(4);
                }
                buf16[i + r * 4] = gr.get(i * 4 + j);
            }
        }

        gr = Arrays.stream(buf16).boxed().collect(toList());

        println("Group Round");
        println();
        List<Integer> groupRoundWinners = playGroupRound(gr, 0, 2);

        List<Integer> qf = new ArrayList<>(Arrays.asList(
                groupRoundWinners.get(0),
                groupRoundWinners.get(9),
                groupRoundWinners.get(4),
                groupRoundWinners.get(13),
                groupRoundWinners.get(8),
                groupRoundWinners.get(1),
                groupRoundWinners.get(12),
                groupRoundWinners.get(5)));

        println("Quarterfinals");
        println();
        List<Integer> sf = new ArrayList<>();
        playSeriesPlayoffRound(qf, sf, new ArrayList<>(), 4, 0);

        println("Semifinals");
        println();
        List<Integer> f = new ArrayList<>();
        playSeriesPlayoffRound(sf, f, new ArrayList<>(), 4, 0);

        println("Final");
        println();

        PlayerSeriesResult r = playSeries(kn[f.get(0)], kn[f.get(1)], 4, 0);
        Player wcWinner = r.getWinner();

        readln();

        println(wcWinner.getNation().getName() + " knight " + wcWinner.getPlayerName() + " is the World Champion!!!");
        println("It is the best day in the history of " + wcWinner.getTown() + "!");
        println("Everyone from " + wcWinner.getNation().getName() + " are celebrating!");
        println("Grand Master " + wcWinner.getSimplePlayerName() + " is now in the history!");

        wcWinner.addTrophy("World Cup", year);

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
        playGroup(league, name, points, 1);
        println();

        int lastp = league.length - 1;
        if (points != 1) {
            println(name + " - relegation match");
            PlayerSeriesResult r = playSeries(kn[league[lastp - 1]], kn[league[lastp]], 3, 0);
            league[lastp - 1] = r.getWinner().id;
            league[lastp] = r.getLoser().id;
        }

        println(name + " - final match");
        PlayerSeriesResult r = playSeries(kn[league[0]], kn[league[1]], 3, 0);
        league[0] = r.getWinner().id;
        league[1] = r.getLoser().id;

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
        MatchResult mr = playTeamMatch(teams.get(nationRating.getRankedNation(3)), teams.get(nationRating.getRankedNation(4)), NATIONAL_TEAM_MATCH_ROUNDS);
        if (mr.rounds.r1 > mr.rounds.r2) {
            sf[1] = teams.get(nationRating.getRankedNation(3));
        } else {
            sf[1] = teams.get(nationRating.getRankedNation(4));
        }

        // Semifinals
        Team[] f = new Team[2];
        mr = playTeamMatch(sf[0], sf[1], NATIONAL_TEAM_MATCH_ROUNDS);
        if (mr.rounds.r1 > mr.rounds.r2) {
            f[0] = sf[0];
        } else {
            f[0] = sf[1];
        }
        mr = playTeamMatch(sf[2], sf[3], NATIONAL_TEAM_MATCH_ROUNDS);
        if (mr.rounds.r1 > mr.rounds.r2) {
            f[1] = sf[2];
        } else {
            f[1] = sf[3];
        }

        // Final
        mr = playTeamMatch(f[0], f[1], NATIONAL_TEAM_MATCH_ROUNDS);
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

    private MatchResult playTeamMatch(Team team1, Team team2, int rounds) {
        Preconditions.checkArgument(team1.id.length == team2.id.length);
        Preconditions.checkArgument(team1.id.length % 2 == 1);
        Preconditions.checkArgument(rounds % 2 == 1);
        int teamSize = team1.id.length;

        MatchResult res = new MatchResult(null, null);

        println(String.format("%s vs %s", team1.name, team2.name));
        println();

        List<PlayerPair> schedule = new ArrayList<>();
        for (int k = 0; k < rounds; k++) {
            for (int i = 0; i < teamSize; ++i) {
                for (int j = 0; j < teamSize; ++j) {
                    schedule.add(new PlayerPair(kn[team1.id[j]], kn[team2.id[(j + i) % teamSize]]));
                }
            }
        }

        int maxWinCount = teamSize * teamSize * rounds / 2 + 1;
        int counter = 0;
        for (PlayerPair pair : schedule) {
            MatchResult mres = playPlayoffGame(pair.p1, pair.p2, 0);
            res.addSubMatchResult(mres.rounds);
            readln();

            if (res.rounds.r1 == maxWinCount || res.rounds.r2 == maxWinCount) {
                break;
            }

            if (counter % teamSize == teamSize - 1) {
                println(String.format("Current score - %d:%d (%d:%d)", res.rounds.r1, res.rounds.r2, res.games.r1, res.games.r2));
                println();
            }
            ++counter;
        }

        println(String.format("%s vs %s - %d:%d (%d:%d)", team1.name, team2.name, res.rounds.r1, res.rounds.r2, res.games.r1, res.games.r2));
        println();
        readln();

        return res;
    }

    private void printStatsToFile() {
        saveToFile(makeFilename(FILE_NAME_STATS, true, true),
                writer -> {
                    writeLevels(writer);
                    writer.println();
//                    writeEffectiveLevels(writer);
//                    writer.println();
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

    private void writeEffectiveLevels(PrintWriter writer) {
        List<Player> playersByLevel = Arrays.stream(kn)
                .sorted(comparingInt(Player::getEffectiveLevel).reversed())
                .collect(toList());

        int maxNameLength = getMaxNameLength(playersByLevel);
        String formatString = "%-2d: %-" + (maxNameLength + 1) + "s %-4d";

        writer.println("Effective Levels");
        for (int i = 0; i < playersByLevel.size(); i++) {
            Player p = playersByLevel.get(i);
            int effectiveLevel = Arrays.stream(p.getShuffledDeck()).sum();
            writer.println(String.format(formatString, (i + 1), p.getPlayerName(), p.getEffectiveLevel()));
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
