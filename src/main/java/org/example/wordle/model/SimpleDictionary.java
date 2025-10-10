package org.example.wordle.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleDictionary implements Dictionary {
    private final List<String> words = new ArrayList<>();
    private final Random rng = new Random();

    public SimpleDictionary() {
        try (InputStream in = getClass().getResourceAsStream("/wordlist.txt")) {
            if (in == null) throw new IllegalStateException("wordlist.txt not found on classpath");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 5 && line.matches("[a-zA-Z]{5}")) words.add(line.toUpperCase());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dictionary", e);
        }
        if (words.isEmpty()) throw new IllegalStateException("Dictionary is empty");
    }

    @Override public boolean isValidWord(String word) {
        if (word == null || word.length() != 5) return false;
        return words.contains(word.toUpperCase());
    }

    @Override public String randomSecret() { return words.get(rng.nextInt(words.size())); }
}