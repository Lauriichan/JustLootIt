package me.lauriichan.spigot.justlootit.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.lauriichan.laylib.command.util.MapUtil;

public final class ImprovedLevenshteinDistance {

    private ImprovedLevenshteinDistance() {
        throw new UnsupportedOperationException();
    }

    public static final Comparator<Entry<String, Integer>> SORTER = new DistanceSorter();

    public static final List<Entry<String, Integer>> rankByDistance(String input, String[] selection) {
        return rank(input, Arrays.stream(selection)).collect(Collectors.toList());
    }

    public static final List<Entry<String, Integer>> rankByDistance(String input, Collection<String> selection) {
        return rank(input, selection.stream()).collect(Collectors.toList());
    }

    public static final List<Entry<String, Integer>> rankByDistance(String input, int limit, String[] selection) {
        return rank(input, Arrays.stream(selection)).limit(limit).collect(Collectors.toList());
    }

    public static final List<Entry<String, Integer>> rankByDistance(String input, int limit, Collection<String> selection) {
        return rank(input, selection.stream()).limit(limit).collect(Collectors.toList());
    }

    private static final Stream<Entry<String, Integer>> rank(String input, Stream<String> stream) {
        return stream.unordered().map(string -> MapUtil.entry(string, distance(input, string))).sorted(SORTER);
    }

    public static final int distance(String var1, String var2) {
        char[] chars1 = var1.toCharArray(), chars2 = var2.toCharArray();
        int[][] matrix = new int[chars1.length + 1][chars2.length + 1];
        for (int x = 0; x <= chars1.length; x++) {
            for (int y = 0; y <= chars2.length; y++) {
                if (x == 0) {
                    matrix[x][y] = y;
                    continue;
                }
                if (y == 0) {
                    matrix[x][y] = x;
                    continue;
                }
                matrix[x][y] = Math.min(matrix[x - 1][y - 1] + costOfChar(chars1[x - 1], chars2[y - 1]),
                    Math.min(matrix[x - 1][y] + 1, matrix[x][y - 1] + 1));
            }
        }
        return matrix[chars1.length][chars2.length];
    }

    private static final int costOfChar(char var1, char var2) {
        if (var1 == var2) {
            return 0;
        }
        return Math.abs(var2 - var1);
    }

    private static final class DistanceSorter implements Comparator<Entry<String, Integer>> {

        private DistanceSorter() {}

        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
            return Integer.compare(o1.getValue(), o2.getValue());
        }

    }

}