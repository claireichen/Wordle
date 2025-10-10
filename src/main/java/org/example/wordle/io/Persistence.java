package org.example.wordle.io;


import org.example.wordle.model.GameStatus;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/** Simple, Gson-free persistence.
 * Format (UTF-8 text):
 * line 1: SECRET
 * line 2: STATUS (IN_PROGRESS/WON/LOST) — informational
 * line 3: N (number of guesses)
 * next N lines: each guess (5 letters)
 */
public class Persistence {
    public static void save(Path file, String secret, List<String> guesses, GameStatus status) throws IOException {
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        List<String> out = new ArrayList<>();
        out.add(secret);
        out.add(status.name());
        out.add(Integer.toString(guesses.size()));
        out.addAll(guesses);
        Files.write(file, out, StandardCharsets.UTF_8);
    }


    public static Loaded load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.size() < 3) throw new IOException("Corrupt save file: too few lines");
        String secret = lines.get(0).trim().toUpperCase();
// String status = lines.get(1).trim(); // not strictly needed; model will recompute
        int n;
        try { n = Integer.parseInt(lines.get(2).trim()); }
        catch (NumberFormatException e) { throw new IOException("Corrupt save file: bad guess count", e); }
        if (lines.size() < 3 + n) throw new IOException("Corrupt save file: missing guesses");
        List<String> guesses = new ArrayList<>();
        for (int i = 0; i < n; i++) guesses.add(lines.get(3 + i).trim().toUpperCase());
        return new Loaded(secret, guesses);
    }


    public static class Loaded {
        public final String secret;
        public final List<String> guesses;
        public Loaded(String secret, List<String> guesses) { this.secret = secret; this.guesses = guesses; }
    }
}