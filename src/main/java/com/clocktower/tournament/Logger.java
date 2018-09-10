package com.clocktower.tournament;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Logger {

    private static final boolean NO_PROMPT = true;

    private static PrintWriter currentWriter;
    private static Scanner scanner = new Scanner(System.in);

    public static void println() {
        System.out.println();
        if (currentWriter != null) {
            currentWriter.println();
        }
    }

    public static void println(String formatString, Object... args) {
        String s = String.format(formatString, args);
        println(s);
    }

    public static void println(String x) {
        System.out.println(x);
        if (currentWriter != null) {
            currentWriter.println(x);
        }
    }

    public static void print(String x) {
        System.out.print(x);
        if (currentWriter != null) {
            currentWriter.print(x);
        }
    }

    public static String readln() {
        String ret = null;
        if (NO_PROMPT) {
            println();
        } else {
            ret = scanner.nextLine();
        }
        if (currentWriter != null) {
            currentWriter.println();
        }
        return ret;
    }

    public static void setCurrentFilename(String filename) {
        if (currentWriter != null) {
            currentWriter.close();
        }
        try {
            currentWriter = new PrintWriter(filename);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeCurrentFile() {
        currentWriter.close();
        currentWriter = null;
    }
}
