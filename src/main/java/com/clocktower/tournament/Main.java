package com.clocktower.tournament;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Season season = new Season();
        System.out.println("Type \"start\" to start new game?");
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        boolean newGameRequested = s.equals("start");
        season.init(newGameRequested);
        season.startSimulation();
        scanner.nextLine();
    }
}
