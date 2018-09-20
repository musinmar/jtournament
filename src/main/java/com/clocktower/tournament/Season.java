package com.clocktower.tournament;

import com.clocktower.tournament.domain.DefaultData;
import com.clocktower.tournament.domain.Nation;
import com.clocktower.tournament.domain.Player;
import com.clocktower.tournament.domain.Title;
import com.clocktower.tournament.dto.SeasonDto;
import com.clocktower.tournament.simulation.Group;
import com.clocktower.tournament.simulation.MatchResult;
import com.clocktower.tournament.simulation.PlayerSeriesResult;
import com.clocktower.tournament.simulation.PlayoffResult;
import com.clocktower.tournament.simulation.SimpleResult;
import com.clocktower.tournament.simulation.Team;
import com.clocktower.tournament.utils.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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
import java.util.Map;
import java.util.function.Consumer;

import static com.clocktower.tournament.utils.Logger.print;
import static com.clocktower.tournament.utils.Logger.println;
import static com.clocktower.tournament.utils.Logger.readln;
import static com.clocktower.tournament.domain.Title.COMMON;
import static com.clocktower.tournament.domain.Title.LORD;
import static com.clocktower.tournament.domain.Title.SIR;
import static com.clocktower.tournament.utils.RandomUtils.random;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class Season {

    private static final int PLAYER_COUNT = 30;

    private static final String FILE_NAME_SEASON_JSON = "season.json";
    private static final String FILE_NAME_RATING = "rating";
    private static final String FILE_NAME_RATING_CHANGE = "rating change";
    private static final String FILE_NAME_STATS = "stats";

    private static final int NORMAL_TIME_LENGTH = 9;
    private static final int ADDITIONAL_TIME_LENGTH = 7;

    private static final int NATIONAL_TEAM_MATCH_ROUNDS = 1;

    private int year;
    private final String FOLDER = "season";

    private List<Player> kn;
    private EloRating elo = new EloRating();

    private NationRating nationRating = new NationRating();
    private Map<Nation, List<Player>> nationalCupResults = new HashMap<>();

    private List<Player> leagues;
    //private List<Team> teams;

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

        //playGoldenCup();

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

        leagues = kn.stream()
                .sorted(comparingInt(Player::getLevel).reversed())
                .collect(toList());

        year = 1;

        nationRating.initDefault();

        save();
    }

    private List<Team> makeRandomTeams() {
        List<Team> teams = new ArrayList<>();
        List<Player> players = newArrayList(kn);
        Collections.shuffle(players);
        List<String> names = asList("Irif Eagles", "Linagor Titans", "Alior Centaurs", "Turon Dragons",
                "Ejmoril Giants", "Reldor Griffons", "Dilion Direwolves", "Telmir Minotaurs");
        for (int i = 0; i < 8; i++) {
            teams.add(new Team(names.get(i), players.subList(i * 3, (i + 1) * 3)));
        }
        return teams;
    }

    private void save() {
        saveAsJson();
    }

    private void saveAsJson() {
        SeasonDto seasonDto = new SeasonDto();
        seasonDto.setYear(year);
        seasonDto.setPlayers(kn.stream().map(Player::toDto).collect(toList()));
        seasonDto.setLeagues(leagues.stream().map(Player::getId).collect(toList()));
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

    private void saveToFile(String filename, Consumer<PrintWriter> writerConsumer) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writerConsumer.accept(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void load() {
        readFromJson();
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
        kn = seasonDto.getPlayers().stream().map(Player::fromDto).collect(toList());
        leagues = seasonDto.getLeagues().stream().map(kn::get).collect(toList());
        nationRating = NationRating.fromDto(seasonDto.getNationRating());
        elo = EloRating.fromDto(seasonDto.getEloRating(), kn);
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

        List<Player> clQualifyingRound1 = new ArrayList<>(asList(
                get_player_from(1, 4),
                get_player_from(3, 3),
                get_player_from(4, 2),
                get_player_from(4, 3),
                get_player_from(5, 1),
                get_player_from(5, 2)));

        List<Player> clQualifyingRound2 = new ArrayList<>(asList(
                get_player_from(1, 3),
                get_player_from(2, 2),
                get_player_from(2, 3),
                get_player_from(3, 2),
                get_player_from(4, 1)));

        List<Player> fcQualifyingRound1 = new ArrayList<>(Collections.singletonList(
                get_player_from(1, 5)));

        List<Player> fcQualifyingRound2 = new ArrayList<>(asList(
                get_player_from(2, 5),
                get_player_from(3, 5),
                get_player_from(4, 4),
                get_player_from(5, 4)));

        printParticipants(clQualifyingRound1);
        shuffle(clQualifyingRound1, true);

        PlayoffResult<Player> clQr1Result = playPlayoffRound(clQualifyingRound1, 2);
        clQualifyingRound2.addAll(clQr1Result.winners);
        fcQualifyingRound1.addAll(clQr1Result.losers);

        println("Federations Cup - Season " + year);
        println("First qualification round");
        println();

        printParticipants(fcQualifyingRound1);
        shuffle(fcQualifyingRound1, true);

        PlayoffResult<Player> fcQr1Result = playPlayoffRound(fcQualifyingRound1, 1);
        fcQualifyingRound2.addAll(fcQr1Result.winners);

        List<Player> clGroupRound = new ArrayList<>(asList(
                get_player_from(1, 1),
                get_player_from(1, 2),
                get_player_from(2, 1),
                get_player_from(3, 1)));

        List<Player> fcGroupRound = new ArrayList<>(asList(
                get_player_from(2, 4),
                get_player_from(3, 4),
                get_player_from(5, 3)));

        println("Champions League - Second qualification round");
        println();

        printParticipants(clQualifyingRound2);
        shuffle(clQualifyingRound2, true);
        PlayoffResult<Player> clQr2Result = playPlayoffRound(clQualifyingRound2, 2);
        clGroupRound.addAll(clQr2Result.winners);
        fcQualifyingRound2.addAll(clQr2Result.losers);

        println("Federations Cup - Second qualification round");
        println();
        printParticipants(fcQualifyingRound2);
        shuffle(fcQualifyingRound2, true);
        PlayoffResult<Player> fcQr2Result = playPlayoffRound(fcQualifyingRound2, 1);
        fcGroupRound.addAll(fcQr2Result.winners);

        println("Federations Cup - Group round");
        println();
        elo.sortPlayersByRating(fcGroupRound);
        printParticipants(fcGroupRound);
        makeGroups(fcGroupRound);
        List<Player> fcGroupRoundResult = playGroupRound(fcGroupRound, 2, 1);

        List<Player> fc_sf = new ArrayList<>(asList(
                fcGroupRoundResult.get(0),
                fcGroupRoundResult.get(5),
                fcGroupRoundResult.get(4),
                fcGroupRoundResult.get(1)));

        println("Champions League - Group round");
        println();
        elo.sortPlayersByRating(clGroupRound);
        printParticipants(clGroupRound);
        makeGroups(clGroupRound);
        List<Player> clGroupRoundResult = playGroupRound(clGroupRound, 4, 1);

        List<Player> cl_sf = new ArrayList<>(asList(
                clGroupRoundResult.get(0),
                clGroupRoundResult.get(5),
                clGroupRoundResult.get(4),
                clGroupRoundResult.get(1)));

        println("Federations Cup - Semifinals");
        println("Semifinals");
        println();
        List<Player> fc_f = playSeriesPlayoffRound(fc_sf, 3, 2).winners;

        println("Champions League - Semifinal Group");
        println("Semifinals");
        println();
        List<Player> cl_f = playSeriesPlayoffRound(cl_sf, 3, 4).winners;

        println("Federations Cup - FINAL");
        println("Final");
        println();
        PlayerSeriesResult r = playSeries(fc_f.get(0), fc_f.get(1), 3, 2);
        Player fcWinner = r.getWinner();
        readln();
        println("Knight " + fcWinner.getPlayerName() + " is the winner of the Federation Cup!");
        println();

        println("Champions League - FINAL");
        println("Final");
        println();
        r = playSeries(cl_f.get(0), cl_f.get(1), 3, 4);
        Player clWinner = r.getWinner();
        readln();
        println("Knight " + clWinner.getPlayerName() + " is the winner of the Champions League!");
        println(clWinner.getNation().getName() + "\'s triumph!");
        println();


        fcWinner.addTrophy("Federations Cup", year);
        clWinner.addTrophy("Champions League", year);
    }

    private Player get_player_from(int rank, int pos) {
        Nation nation = nationRating.getRankedNation(rank - 1);
        return nationalCupResults.get(nation).get(pos - 1);
    }

    private void makeGroups(List<Player> a) {
        List<Player> b = new ArrayList<>(a.subList(0, 4));
        List<Player> c = new ArrayList<>(a.subList(4, 8));
        shuffle(b, false);
        shuffle(c, false);
        a.clear();
        a.addAll(asList(
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

    private List<Player> playNationalCup(Nation nation) {
        List<Player> players = kn.stream()
                .filter(p -> p.getNation() == nation)
                .collect(toList());

        String name = "Cup of " + nation.getName();
        players = playGroup(players, name, 0, 1);
        println();

        players.get(0).addTrophy(name, year);
        return players;
    }

    private List<Player> playGroupRound(List<Player> players, int points, int rounds) {
        List<Player> result = new ArrayList<>();
        int n = (players.size() + 1) / 4;
        for (int i = 0; i < n; ++i) {
            List<Player> group = new ArrayList<>(players.subList(i * 4, (i + 1) * 4));
            List<Player> groupResult = playGroup(group, "Group " + (i + 1), points, rounds);
            result.addAll(groupResult);
        }
        println();
        return result;
    }

    private PlayoffResult<Player> playPlayoffRound(List<Player> players, int points) {
        PlayoffResult<Player> pr = new PlayoffResult<>();
        int len = players.size();
        for (int i = 0; i < len / 2; ++i) {
            MatchResult<Player> mr = playPlayoffGame(players.get(i * 2), players.get(i * 2 + 1), points);
            pr.winners.add(mr.getWinner());
            pr.losers.add(mr.getLoser());
            readln();
        }
        println();
        return pr;
    }

    private PlayoffResult<Player> playSeriesPlayoffRound(List<Player> players, int wins, int points) {
        PlayoffResult<Player> pr = new PlayoffResult<>();
        int len = players.size();
        for (int i = 0; i < len / 2; ++i) {
            PlayerSeriesResult r = playSeries(players.get(i * 2), players.get(i * 2 + 1), wins, points);
            pr.winners.add(r.getWinner());
            pr.losers.add(r.getLoser());
            readln();
        }
        println();
        return pr;
    }

    private PlayerSeriesResult playSeries(Player p1, Player p2, int wins, int points) {
        PlayerSeriesResult seriesResult = new PlayerSeriesResult(p1, p2);
        SimpleResult r = seriesResult.getResult();
        while (r.getTopScore() != wins) {
            MatchResult<Player> mr = playPlayoffGame(p1, p2, 0);
            readln();
            if (mr.rounds.r1 > mr.rounds.r2) {
                r.r1 += 1;
            } else {
                r.r2 += 1;
            }
        }
        nationRating.updateRatings(p1, p2, r, points);
        return seriesResult;
    }

    private List<Player> playGroup(List<Player> players, String groupName, int points, int rounds) {
        println(groupName);
        println();
        printParticipants(players);

        Group group = new Group(groupName, players, elo);

        for (int k = 1; k <= rounds; ++k) {
            int len = players.size();
            if (len == 4) {
                playGroupMatch(group, 0, 2, points);
                playGroupMatch(group, 1, 3, points);
                playGroupMatch(group, 3, 0, points);
                playGroupMatch(group, 2, 1, points);
                playGroupMatch(group, 0, 1, points);
                playGroupMatch(group, 3, 2, points);
            } else if (len == 6) {
                playGroupMatch(group, 0, 5, points);
                playGroupMatch(group, 2, 4, points);
                playGroupMatch(group, 1, 3, points);

                playGroupMatch(group, 5, 2, points);
                playGroupMatch(group, 0, 1, points);
                playGroupMatch(group, 4, 3, points);

                playGroupMatch(group, 4, 5, points);
                playGroupMatch(group, 3, 0, points);
                playGroupMatch(group, 1, 2, points);

                playGroupMatch(group, 5, 1, points);
                playGroupMatch(group, 0, 4, points);
                playGroupMatch(group, 2, 3, points);

                playGroupMatch(group, 3, 5, points);
                playGroupMatch(group, 0, 2, points);
                playGroupMatch(group, 4, 1, points);
            } else {
                int[] buf = new int[len];
                int[] buf2 = new int[len];
                for (int i = 0; i < len; ++i) {
                    buf[i] = i;
                }
                int halflen = len / 2;
                for (int i = 1; i < len; ++i) {
                    if (i % 2 == 1 && i > 1) {
                        group.printGroupResults();
                    }

                    for (int j = 0; j < halflen; ++j) {
                        playGroupMatch(group, buf[j], buf[j + halflen], points);
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
                }
            }
        }

        group.printGroupResults();
        return group.getResult();
    }

    private void printParticipants(List<Player> players) {
        players.forEach(p -> println(p.getPlayerName()));
        readln();
    }

    private void playGroupMatch(Group group, int id1, int id2, int points) {
        MatchResult<Player> matchResult = playGroupGame(group.getGroupResult(id1).player, group.getGroupResult(id2).player, points);
        group.applyMatchResult(id1, id2, matchResult);
        readln();
    }

    private MatchResult<Player> playGroupGame(Player p1, Player p2, int points) {
        return playGame(p1, p2, false, points);
    }

    private MatchResult<Player> playPlayoffGame(Player p1, Player p2, int points) {
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

    private void shuffle(List<Player> a, boolean checkNoSameFederationPairs) {
        boolean done = false;
        while (!done) {
            Collections.shuffle(a);
            if (!checkNoSameFederationPairs) {
                done = true;
            } else {
                done = true;
                for (int i = 0; i < a.size(); i += 2) {
                    if (a.get(i).getNation() == a.get(i + 1).getNation()) {
                        done = false;
                        break;
                    }
                }
            }
        }
    }

    private void advancePlayersAge() {
        kn.forEach(Player::advanceAge);
    }

    private void adjustPlayerSkillsAfterSeason() {
        kn.forEach(Player::applyRandomDeckChanges);
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
        println("Titul playoffs  - Season " + year);
        println();
        playMatchForTitle(LORD, SIR, 4);
        playMatchForTitle(SIR, COMMON, 6);
    }

    private void playMatchForTitle(Title contestedTitle, Title contenderTitle, int allowedHolderCount) {
        List<Player> participants;

        long holderCount = kn.stream().filter(p -> p.getTitle() == contestedTitle).count();
        if (holderCount >= allowedHolderCount) {
            List<Player> playersByRating = elo.getPlayersByRating();
            List<Player> playersByRatingReversed = Lists.reverse(playersByRating);

            Player holder = playersByRatingReversed.stream()
                    .filter(p -> p.getTitle() == contestedTitle)
                    .findFirst().orElseThrow(RuntimeException::new);

            Player contender = playersByRating.stream()
                    .filter(p -> p.getTitle() == contenderTitle)
                    .findFirst().orElseThrow(RuntimeException::new);

            participants = asList(holder, contender);
        } else {
            List<Player> playersByRating = elo.getPlayersByRating();
            participants = playersByRating.stream()
                    .filter(p -> p.getTitle() == contenderTitle)
                    .limit(2)
                    .collect(toList());
        }

        println("Match for the %s title", contestedTitle.getName());
        println();
        PlayerSeriesResult r = playSeries(participants.get(0), participants.get(1), 3, 0);

        if (r.getWinner().getTitle() != contestedTitle) {
            r.getWinner().setTitle(contestedTitle);
            r.getLoser().setTitle(contenderTitle);
            println("Knight %s has gained the %s title!", r.getWinner().getSimplePlayerName(), contestedTitle.getName());
        } else {
            println("Knight %s has defended the %s title!", r.getWinner().getSimplePlayerName(), contestedTitle.getName());
        }

        readln();
        println();
    }

    private Player selectPlayerToRetire() {
        final double EXPONENTIAL_FACTOR = 12.0;

        double[] weights = kn.stream().mapToDouble(p -> Math.exp(p.getAge() / EXPONENTIAL_FACTOR)).toArray();
        double totalWeight = stream(weights).sum();

//        for (int i = 0; i < kn.size(); ++i) {
//            println("Weight %d: %5.2f", i, weights[i]);
//        }
//        println("Total weight: %5.2f", totalWeight);

        double r = random() * totalWeight;
//        println("Random number: %5.2f", r);

        for (int i = kn.size() - 1; i >= 0; --i) {
            if (totalWeight - weights[i] <= r) {
//                println("Found interval for weight %d", i);
                return kn.get(i);
            } else {
                totalWeight -= weights[i];
//                println("Not found interval, next weight: %5.2f", totalWeight);
            }
        }
        throw new RuntimeException("Failed to select player to retire");
    }

    private void retireRandomPlayer() {
        Player retiredPlayer = selectPlayerToRetire();
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
        retiredPlayer.setTitle(COMMON);
        elo.resetRating(retiredPlayer);

        println();
        readln();
    }

    private void playWorldCup() {
        println();
        println("World Championship");

        Player[] buf8 = new Player[8];
        Player[] buf16 = new Player[16];

        List<Player> playersByRating = elo.getPlayersByRating();
        List<Player> ro1 = newArrayList(playersByRating.subList(22, 30));
        List<Player> ro2 = newArrayList(playersByRating.subList(8, 22));
        List<Player> gr = newArrayList(playersByRating.subList(0, 8));

        println();
        println("First Round");
        println();

        printParticipants(ro1);

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 1; ++j) {
                int r = random(2);
                while (buf8[i + r * 4] != null) {
                    r = random(2);
                }
                buf8[i + r * 4] = ro1.get(i * 2 + j);
            }
        }

        ro1 = Arrays.stream(buf8).collect(toList());

        println("Group Round");
        println();

        ro1 = playGroupRound(ro1, 0, 2);

        List<Player> ro1sf = new ArrayList<>(asList(
                ro1.get(0),
                ro1.get(5),
                ro1.get(4),
                ro1.get(1)));

        println("First Round Semifinals");
        println();
        PlayoffResult<Player> ro1Result = playSeriesPlayoffRound(ro1sf, 4, 0);
        ro2.addAll(ro1Result.winners);

        println("Second Round");
        println();

        elo.sortPlayersByRating(ro2);
        printParticipants(ro2);

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 3; ++j) {
                int r = random(4);
                while (buf16[i + r * 4] != null) {
                    r = random(4);
                }
                buf16[i + r * 4] = ro2.get(i * 4 + j);
            }
        }

        ro2 = Arrays.stream(buf16).collect(toList());

        println("Group Round");
        println();
        List<Player> ro2Result = playGroupRound(ro2, 0, 2);
        for (int i = 0; i < 4; ++i) {
            gr.add(ro2Result.get(i * 4));
            gr.add(ro2Result.get(i * 4 + 1));
        }

        println("Final Round");
        println();
        elo.sortPlayersByRating(gr);
        printParticipants(gr);

        buf16 = new Player[16];

        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 3; ++j) {
                int r = random(4);
                while (buf16[i + r * 4] != null) {
                    r = random(4);
                }
                buf16[i + r * 4] = gr.get(i * 4 + j);
            }
        }

        gr = Arrays.stream(buf16).collect(toList());

        println("Group Round");
        println();
        List<Player> groupRoundWinners = playGroupRound(gr, 0, 2);

        List<Player> qf = new ArrayList<>(asList(
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
        List<Player> sf = playSeriesPlayoffRound(qf, 4, 0).winners;

        println("Semifinals");
        println();
        List<Player> f = playSeriesPlayoffRound(sf, 4, 0).winners;

        println("Final");
        println();

        PlayerSeriesResult r = playSeries(f.get(0), f.get(1), 4, 0);
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

        Collections.swap(leagues, 7, 8);
        Collections.swap(leagues, 15, 16);
        Collections.swap(leagues, 23, 24);
    }

    private void playLeague(int first, int last, String name, int points) {
        List<Player> league = new ArrayList<>(leagues.subList(first - 1, last));

        league = playGroup(league, name, points, 2);
        println();

        int lastp = league.size() - 1;
        if (points != 1) {
            println(name + " - relegation match");
            PlayerSeriesResult r = playSeries(league.get(lastp - 1), league.get(lastp), 4, 0);
            league.set(lastp - 1, r.getWinner());
            league.set(lastp, r.getLoser());
        }

        println(name + " - final match");
        PlayerSeriesResult r = playSeries(league.get(0), league.get(1), 4, 0);
        league.set(0, r.getWinner());
        league.set(1, r.getLoser());

        for (int i = first; i <= last; ++i) {
            leagues.set(i - 1, league.get(i - first));
        }

        println();
        println("Knight " + league.get(0).getPlayerName() + " is the winner of the " + name + "!");
        if (points != 1) {
            println("Knight " + league.get(lastp).getPlayerName() + " have been relegated.");
        }

        println();

        league.get(0).addTrophy(name, year);
    }

    private Team makeNationalTeam(Nation nation, boolean mainTeam) {
        String name = nation.getName() + (mainTeam ? "" : " B");
        List<Player> playersByRating = elo.getPlayersByRating();
        List<Player> teamMembers = playersByRating.stream()
                .filter(p -> p.getNation() == nation)
                .skip(mainTeam ? 0 : 3)
                .limit(3)
                .collect(toList());
        return new Team(name, teamMembers);
    }

    private void playNationalWorldCup() {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < Nation.COUNT; ++i) {
            Nation nation = nationRating.getRankedNation(i);
            teams.add(makeNationalTeam(nation, true));
        }
        for (int i = 0; i < Nation.COUNT; ++i) {
            Nation nation = nationRating.getRankedNation(i);
            teams.add(makeNationalTeam(nation, false));
        }

        println(String.format("National World Cup - Season %d", year));
        println();

        println("Participants");
        println();
        teams.forEach(this::printTeam);

        // First round
        List<Team> firstRound = new ArrayList<>(asList(
                teams.get(6),
                teams.get(7),
                teams.get(8),
                teams.get(9)
        ));
        println("First round");
        println();
        PlayoffResult<Team> firstRoundResult = playTeamPlayoffRound(firstRound, NATIONAL_TEAM_MATCH_ROUNDS);

        // Quaterfinals
        List<Team> qf = new ArrayList<>(asList(
                teams.get(0),
                firstRoundResult.winners.get(0),
                teams.get(3),
                teams.get(4),
                teams.get(1),
                firstRoundResult.winners.get(1),
                teams.get(2),
                teams.get(5)
        ));
        println("Quarterfinals");
        println();
        PlayoffResult<Team> qfResult = playTeamPlayoffRound(qf, NATIONAL_TEAM_MATCH_ROUNDS);

        println("Semifinals");
        println();
        PlayoffResult<Team> sfResult = playTeamPlayoffRound(qfResult.winners, NATIONAL_TEAM_MATCH_ROUNDS);

        println("Final");
        println();
        PlayoffResult<Team> fResult = playTeamPlayoffRound(sfResult.winners, NATIONAL_TEAM_MATCH_ROUNDS);

        Team winner = fResult.winners.get(0);
        println(String.format("%s is the winner of the National World Cup %d", winner.getName(), year));
        println();
        readln();

        winner.getPlayers().forEach(p -> p.addTrophy("National World Cup", year));
    }

    private void printTeam(Team team) {
        println(team.getName());
        for (int j = 0; j < team.getPlayers().size(); j++) {
            print(String.format("%d: %s", j + 1, team.getPlayers().get(j).getPlayerName()));
            if (j == 0) {
                println(" - Captain");
            } else {
                println();
            }
        }
        println();
    }

    private PlayoffResult<Team> playTeamPlayoffRound(List<Team> teams, int rounds) {
        PlayoffResult<Team> pr = new PlayoffResult<>();
        int len = teams.size() / 2;
        for (int i = 0; i < len; i++) {
            MatchResult<Team> mr = playTeamMatch(teams.get(i * 2), teams.get(i * 2 + 1), rounds);
            pr.winners.add(mr.getWinner());
            pr.losers.add(mr.getLoser());
        }
        return pr;
    }

    private MatchResult<Team> playTeamMatch(Team team1, Team team2, int rounds) {
        Preconditions.checkArgument(team1.getPlayers().size() == team2.getPlayers().size());
        Preconditions.checkArgument(team1.getPlayers().size() % 2 == 1);
        Preconditions.checkArgument(rounds % 2 == 1);
        int teamSize = team1.getPlayers().size();

        MatchResult<Team> res = new MatchResult<>(team1, team2);

        println(String.format("%s vs %s", team1.getName(), team2.getName()));
        println();

        List<PlayerPair> schedule = new ArrayList<>();
        for (int k = 0; k < rounds; k++) {
            for (int i = 0; i < teamSize; ++i) {
                for (int j = 0; j < teamSize; ++j) {
                    schedule.add(new PlayerPair(team1.getPlayers().get(j), team2.getPlayers().get((j + i) % teamSize)));
                }
            }
        }

        int maxWinCount = teamSize * teamSize * rounds / 2 + 1;
        int counter = 0;
        for (PlayerPair pair : schedule) {
            MatchResult<Player> mres = playPlayoffGame(pair.p1, pair.p2, 0);
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

        println(String.format("%s vs %s - %d:%d (%d:%d)", team1.getName(), team2.getName(), res.rounds.r1, res.rounds.r2, res.games.r1, res.games.r2));
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
        List<Player> playersByLevel = kn.stream()
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
        List<Player> playersByLevel = kn.stream()
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
        List<Player> playersByAge = kn.stream()
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

    private void playGoldenCup(List<Team> teams) {
        println(String.format("Golden Cup - Season %d", year));
        println();

        List<Team> allTeams = new ArrayList<>(teams);

        println("Participants");
        println();
        allTeams.forEach(this::printTeam);

        Collections.shuffle(allTeams);

        println("Quarterfinals");
        println();
        PlayoffResult<Team> qfResult = playTeamPlayoffRound(allTeams, NATIONAL_TEAM_MATCH_ROUNDS);

        println("Semifinals");
        println();
        PlayoffResult<Team> sfResult = playTeamPlayoffRound(qfResult.winners, NATIONAL_TEAM_MATCH_ROUNDS);

        println("Final");
        println();
        PlayoffResult<Team> fResult = playTeamPlayoffRound(sfResult.winners, NATIONAL_TEAM_MATCH_ROUNDS);

        Team winner = fResult.winners.get(0);
        println(String.format("%s is the winner of the Golden Cup %d", winner.getName(), year));
        println();
        readln();

        winner.getPlayers().forEach(p -> p.addTrophy("Golden Cup", year));
    }
}
