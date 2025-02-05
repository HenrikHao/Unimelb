package src.utility;
import src.AbstractTetris;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

public class GameStat {
    private List<Integer> scores = new ArrayList<Integer>();
    private List<Map<Character, Integer>> blocks = new ArrayList<Map<Character, Integer>>();
    private String difficulty;
    public GameStat(String tetrisName) {
        switch (tetrisName) {
            case "TetrisEasy" -> difficulty = "Easy";
            case "TetrisMedian" -> difficulty = "Medium";
            case "TetrisHard" -> difficulty = "Madness";
        }
    }
    public void addScore(int round, int score) {
        // if the first score in a round
        if (scores.size() == round) {
            scores.add(score);
        } else {
            scores.set(round, score);
        }
    }
    public void addTetrisToStat(int round, String symbol) {
        Map<Character, Integer> stats = null;
        if (blocks.size() == round) {
            stats = new HashMap<>() {
            };
            stats.put(symbol.charAt(0), 1);
            blocks.add(round, stats);
        } else {
            stats = blocks.get(round);
            stats.put(symbol.charAt(0), stats.getOrDefault(symbol.charAt(0), 0) + 1);
            blocks.set(round, stats);
        }
    }

    public void output() throws IOException {

        List<String> stats = new ArrayList<>();
        BufferedWriter writer = new BufferedWriter(new FileWriter("Statistics.txt"));


        // calculate average
        double average = scores.stream().mapToDouble(d -> d).sum() / scores.size();

        writer.append("Difficulty: ").append(difficulty);
        writer.append("\nAverage score per round: ").append(String.valueOf(average));

        for (Map<Character, Integer> map : blocks) {
            writer.append("\n------------------------------------------\n");
            writer.append("Round #" + (blocks.indexOf(map) + 1) + "\n");
            for (Map.Entry<Character, Integer> entry : map.entrySet()) {
                writer.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
            }
        }
        writer.close();


    }

}
