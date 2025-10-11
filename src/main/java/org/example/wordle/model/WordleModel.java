package org.example.wordle.model;

import org.example.wordle.util.ObservableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WordleModel extends ObservableModel {
    public static final int WORD_LENGTH = 5;
    public static final int MAX_TURNS = 6;


    private final Dictionary dictionary;
    private String secret;
    private final List<String> guesses = new ArrayList<>();
    private final List<List<LetterFeedback>> feedback = new ArrayList<>();
    private final KeyboardState keyboard = new KeyboardState();
    private GameStatus status = GameStatus.IN_PROGRESS;
    private boolean hardMode = false; // Hard Mode: reuse revealed letters


    public WordleModel(Dictionary dictionary, String fixedSecretOrNull) {
        this.dictionary = dictionary;
        this.secret = (fixedSecretOrNull != null) ? fixedSecretOrNull.toUpperCase() : dictionary.randomSecret();
        if (secret.length() != WORD_LENGTH) throw new IllegalArgumentException("Secret must be 5 letters");
        if (!secret.matches("[A-Z]{5}")) throw new IllegalArgumentException("Secret must be letters only");
    }


    public WordleModel(Dictionary dictionary) {
        this(dictionary, null);
    }


    // ----- Getters for View -----
    public int turnsTaken() {
        return guesses.size();
    }

    public GameStatus getStatus() {
        return status;
    }

    public List<String> getGuesses() {
        return new ArrayList<>(guesses);
    }

    public List<List<LetterFeedback>> getFeedback() {
        return new ArrayList<>(feedback);
    }

    public KeyboardState getKeyboard() {
        return keyboard;
    }

    public String getSecretDebug() {
        return secret;
    } // for testing/demo only


    // ----- Modes -----
    public void setHardMode(boolean enabled) {
        this.hardMode = enabled;
    }

    public boolean isHardMode() {
        return hardMode;
    }


    // ----- Game API -----
    public List<LetterFeedback> submitGuess(String guess) {
        if (status != GameStatus.IN_PROGRESS) throw new IllegalStateException("Game over");
        if (guess == null || guess.length() != WORD_LENGTH)
            throw new IllegalArgumentException("Guess must be 5 letters");
        guess = guess.toUpperCase();
        if (!guess.matches("[A-Z]{5}")) throw new IllegalArgumentException("Guess must be A-Z only");
        if (!dictionary.isValidWord(guess)) throw new IllegalArgumentException("Not in word list");
        if (hardMode) enforceHardMode(guess);


        List<LetterFeedback> row = evaluate(guess, secret);
        guesses.add(guess);
        feedback.add(row);
        for (int i = 0; i < WORD_LENGTH; i++) keyboard.upgrade(guess.charAt(i), row.get(i));


        if (guess.equals(secret)) status = GameStatus.WON;
        else if (guesses.size() >= MAX_TURNS) status = GameStatus.LOST;


        notifyListeners();
        return row;
    }

    public void reset(String fixedSecretOrNull) {
        guesses.clear();
        feedback.clear();
        keyboard.clear();
        status = GameStatus.IN_PROGRESS;
        secret = (fixedSecretOrNull != null) ? fixedSecretOrNull.toUpperCase() : dictionary.randomSecret();
        notifyListeners();
    }


    private void enforceHardMode(String guess) {
// Build constraints from previous feedback (greens fixed; min counts for green+yellow letters)
        char[] mustAt = new char[WORD_LENGTH];
        int[] minCount = new int[26];
        for (int r = 0; r < guesses.size(); r++) {
            String g = guesses.get(r);
            List<LetterFeedback> row = feedback.get(r);
            int[] gyRow = new int[26];
            for (int i = 0; i < WORD_LENGTH; i++) {
                char ch = g.charAt(i);
                int idx = ch - 'A';
                switch (row.get(i)) {
                    case CORRECT -> {
                        mustAt[i] = ch;
                        gyRow[idx]++;
                    }
                    case PRESENT -> {
                        gyRow[idx]++;
                    }
                    default -> {
                    }
                }
            }
            for (int L = 0; L < 26; L++) if (gyRow[L] > minCount[L]) minCount[L] = gyRow[L];
        }
// positional greens must be reused
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (mustAt[i] != 0 && guess.charAt(i) != mustAt[i])
                throw new IllegalArgumentException("Hard mode: position " + (i + 1) + " must be '" + mustAt[i] + "'");
        }
// include at least the known count of each discovered letter
        int[] cnt = new int[26];
        for (int i = 0; i < WORD_LENGTH; i++) cnt[guess.charAt(i) - 'A']++;
        for (int L = 0; L < 26; L++) {
            if (cnt[L] < minCount[L]) {
                char ch = (char) ('A' + L);
                int need = minCount[L];
                if (need == 1) throw new IllegalArgumentException("Hard mode: must include '" + ch + "'");
                else throw new IllegalArgumentException("Hard mode: must include " + need + " '" + ch + "' letters");
            }
        }
    }


    public static List<LetterFeedback> evaluate(String guess, String secret) {
        guess = guess.toUpperCase();
        secret = secret.toUpperCase();
        LetterFeedback[] fb = new LetterFeedback[WORD_LENGTH];
        int[] remain = new int[26];


        for (int i = 0; i < WORD_LENGTH; i++) {
            char g = guess.charAt(i), s = secret.charAt(i);
            if (g == s) fb[i] = LetterFeedback.CORRECT;
            else remain[s - 'A']++;
        }
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (fb[i] != null) continue;
            char g = guess.charAt(i);
            int idx = g - 'A';
            if (idx >= 0 && idx < 26 && remain[idx] > 0) {
                fb[i] = LetterFeedback.PRESENT;
                remain[idx]--;
            } else fb[i] = LetterFeedback.ABSENT;
        }
        return Arrays.asList(fb);
    }
}