package org.example.wordle.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart hint engine: filters candidates based on prior feedback and
 * scores words by letter-frequency (approx. information gain).
 */
public class HintEngine {
    private final List<String> corpus; // uppercase 5-letter words

    /** Loads words from /wordlist.txt on the classpath. */
    public HintEngine() {
        this(loadWordList());
    }

    public HintEngine(List<String> wordsUppercase) {
        this.corpus = new ArrayList<>(wordsUppercase);
    }

    public List<String> suggest(List<String> guesses, List<List<LetterFeedback>> fb, int k) {
        List<String> cand = filterCandidates(guesses, fb);
        if (cand.isEmpty()) return List.of();
// build frequency over remaining candidates (unique letters per word)
        int[] freq = new int[26];
        for (String w : cand) {
            boolean[] seen = new boolean[26];
            for (int i = 0; i < w.length(); i++) {
                int idx = w.charAt(i) - 'A';
                if (!seen[idx]) { freq[idx]++; seen[idx] = true; }
            }
        }
// score by sum of unique-letter frequencies; penalize repeats slightly
        record Scored(String w, int s) {}
        List<Scored> scored = new ArrayList<>();
        for (String w : cand) {
            boolean[] seen = new boolean[26];
            int score = 0;
            for (int i = 0; i < w.length(); i++) {
                int idx = w.charAt(i) - 'A';
                if (!seen[idx]) { score += freq[idx]; seen[idx] = true; }
                else score -= 1; // mild penalty for duplicates
            }
            scored.add(new Scored(w, score));
        }
        scored.sort((a,b) -> Integer.compare(b.s, a.s));
        return scored.stream()
                .map(s -> s.w)
                .filter(w -> !guesses.contains(w)) // don't suggest what you already tried
                .limit(k)
                .collect(Collectors.toList());
    }

    private List<String> filterCandidates(List<String> guesses, List<List<LetterFeedback>> fb) {
        if (guesses.isEmpty()) return new ArrayList<>(corpus);
        Constraints C = Constraints.from(guesses, fb);
        List<String> out = new ArrayList<>();
        outer: for (String w : corpus) {
// fixed positions
            for (int i = 0; i < 5; i++) {
                if (C.must[i] != 0 && w.charAt(i) != C.must[i]) continue outer;
                if (C.cannot[i].contains(w.charAt(i))) continue outer;
            }
// letter count bounds
            int[] count = new int[26];
            for (int i = 0; i < 5; i++) count[w.charAt(i) - 'A']++;
            for (int L = 0; L < 26; L++) {
                if (count[L] < C.min[L]) continue outer;
                if (count[L] > C.max[L]) continue outer;
            }
            out.add(w);
        }
        return out;
    }

    // --- Constraints builder (handles duplicates reasonably well across rows) ---
    static class Constraints {
        final char[] must = new char[5];
        final List<Character>[] cannot = new List[5];
        final int[] min = new int[26];
        final int[] max = new int[26];
        Constraints() {
            for (int i = 0; i < 5; i++) cannot[i] = new ArrayList<>();
            Arrays.fill(max, 5);
        }
        static Constraints from(List<String> guesses, List<List<LetterFeedback>> fb) {
            Constraints C = new Constraints();
            for (int r = 0; r < guesses.size(); r++) {
                String g = guesses.get(r);
                List<LetterFeedback> row = fb.get(r);
                int[] greenYellow = new int[26];
                int[] total = new int[26];
// first pass: positions and per-row counts
                for (int i = 0; i < 5; i++) {
                    char ch = g.charAt(i);
                    int idx = ch - 'A';
                    total[idx]++;
                    switch (row.get(i)) {
                        case CORRECT -> { C.must[i] = ch; greenYellow[idx]++; }
                        case PRESENT -> { C.cannot[i].add(ch); greenYellow[idx]++; }
                        case ABSENT -> { /* handled via counts after */ }
                    }
                }
// update global min/max from this row
                for (int L = 0; L < 26; L++) {
                    C.min[L] = Math.max(C.min[L], greenYellow[L]);
                    int absents = total[L] - greenYellow[L];
                    if (absents > 0) {
// if this row had any ABSENT of L, then the true count cannot exceed greenYellow for that row
                        C.max[L] = Math.min(C.max[L], greenYellow[L]);
                    }
                }
            }
// letters never seen as green/yellow across all rows have max 0 if ever guessed and marked absent
            boolean[] seenGY = new boolean[26];
            for (int r = 0; r < guesses.size(); r++) {
                String g = guesses.get(r);
                List<LetterFeedback> row = fb.get(r);
                for (int i = 0; i < 5; i++) {
                    char ch = g.charAt(i); int idx = ch - 'A';
                    if (row.get(i) == LetterFeedback.CORRECT || row.get(i) == LetterFeedback.PRESENT) seenGY[idx] = true;
                }
            }
            for (int r = 0; r < guesses.size(); r++) {
                String g = guesses.get(r);
                List<LetterFeedback> row = fb.get(r);
                for (int i = 0; i < 5; i++) {
                    char ch = g.charAt(i); int idx = ch - 'A';
                    if (row.get(i) == LetterFeedback.ABSENT && !seenGY[idx]) {
                        C.max[idx] = 0;
                        C.cannot[i].add(ch);
                    }
                }
            }
            return C;
        }
    }

    private static List<String> loadWordList() {
        List<String> words = new ArrayList<>();
        try (InputStream in = HintEngine.class.getResourceAsStream("/wordlist.txt")) {
            if (in == null) return words; // empty list
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line; while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 5 && line.matches("[a-zA-Z]{5}")) words.add(line.toUpperCase());
                }
            }
        } catch (IOException ignored) {}
        return words;
    }
}
