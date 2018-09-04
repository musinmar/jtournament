package com.clocktower.tournament;

public class IntToRomanConverter {

    private static final int NVALS = 13;
    private static final int[] VALS = new int[]{1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000};
    private static final String[] ROMS = new String[]{"I", "IV", "V", "IX", "X", "XL", "L", "XC", "C",
            "CD", "D", "CM", "M"};

    public static String convert(int num) {
        String result = "";
        int b = NVALS - 1;
        while (num > 0) {
            while (VALS[b] > num) {
                --b;
            }
            num -= VALS[b];
            result += ROMS[b];
        }
        return result;
    }
}
