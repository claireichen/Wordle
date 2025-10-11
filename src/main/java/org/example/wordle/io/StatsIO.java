package org.example.wordle.io;

import org.example.wordle.model.Stats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class StatsIO {

    /** Text format:
     *  line1: games
     *  line2: wins
     *  line3: six integers for distribution (wins in 1..6 guesses), space-separated
     */
    public static void save(Path file, Stats s) throws IOException {
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        int[] d = s.getGuessDistribution();
        String dist = String.format("%d %d %d %d %d %d", d[0], d[1], d[2], d[3], d[4], d[5]);
        String content = s.getGames() + "\n" + s.getWins() + "\n" + dist + "\n";
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    public static Stats load(Path file) throws IOException {
        if (!Files.exists(file)) return new Stats();
        var lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int games = lines.size() > 0 ? parseIntSafe(lines.get(0)) : 0;
        int wins  = lines.size() > 1 ? parseIntSafe(lines.get(1)) : 0;
        int[] dist = new int[6];
        if (lines.size() > 2) {
            String[] parts = lines.get(2).trim().split("\\s+");
            for (int i = 0; i < Math.min(6, parts.length); i++) dist[i] = parseIntSafe(parts[i]);
        }
        return new Stats(games, wins, dist);
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}
