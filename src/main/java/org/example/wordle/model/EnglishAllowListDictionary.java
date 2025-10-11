package org.example.wordle.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates guesses against a large allow-list of real English 5-letter words.
 * Secrets are still chosen from another dictionary (e.g., SimpleDictionary).
 *
 * Put a file like /english-words-5.txt in resources (one word per line).
 * If that resource isn't found, it will try the system dictionary at /usr/share/dict/words.
 */
public class EnglishAllowListDictionary implements Dictionary {
    private final Dictionary secretSource;
    private final Set<String> valid = new HashSet<>();

    public EnglishAllowListDictionary(Dictionary secretSource, String resourcePath) {
        this.secretSource = secretSource;
        boolean loaded = loadFromResource(resourcePath);
        if (!loaded) {
            // Fallback to system dictionary on macOS/Linux (optional)
            trySystemDictionary(Path.of("/usr/share/dict/words"));
        }
        if (valid.isEmpty()) {
            throw new IllegalStateException(
                    "No allow-list found. Add /english-words-5.txt to resources or provide a valid path.");
        }
    }

    @Override
    public boolean isValidWord(String word) {
        if (word == null || word.length() != 5) return false;
        String up = word.toUpperCase();
        // Only pure letters; reject digits/accents/punct
        if (!up.matches("[A-Z]{5}")) return false;
        return valid.contains(up);
    }

    @Override
    public String randomSecret() {
        return secretSource.randomSecret(); // keep your curated secret list
    }

    // ---- loaders ----
    private boolean loadFromResource(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) return false;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 5 && line.matches("[A-Za-z]{5}")) {
                        valid.add(line.toUpperCase());
                    }
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void trySystemDictionary(Path path) {
        try {
            if (!Files.exists(path)) return;
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String w = line.trim();
                if (w.length() == 5 && w.matches("[A-Za-z]{5}")) {
                    valid.add(w.toUpperCase());
                }
            }
        } catch (IOException ignored) { }
    }
}
