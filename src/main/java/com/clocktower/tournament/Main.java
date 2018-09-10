package com.clocktower.tournament;

import java.util.Scanner;

import static com.clocktower.tournament.Logger.readln;

public class Main {
    public static void main(String[] args) {
        System.out.println("Type \"start\" to start new game?");
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        boolean newGameRequested = s.equals("start");
        Season season = new Season();
        season.init(newGameRequested);
        season.simulateSeason();
        for (int i = 0; i < 100; i++) {
            season = new Season();
            season.init(false);
            season.simulateSeason();
        }
        readln();
    }
}
