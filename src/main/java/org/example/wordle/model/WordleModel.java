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


    public WordleModel(Dictionary dictionary, String fixedSecretOrNull) {
        this.dictionary = dictionary;
        this.secret = (fixedSecretOrNull != null) ? fixedSecretOrNull.toUpperCase() : dictionary.randomSecret();
        if (secret.length() != WORD_LENGTH) throw new IllegalArgumentException("Secret must be 5 letters");
        if (!secret.matches("[A-Z]{5}")) throw new IllegalArgumentException("Secret must be letters only");
    }


    public WordleModel(Dictionary dictionary) { this(dictionary, null); }


    // ----- Getters for View -----
    public int turnsTaken() { return guesses.size(); }
    public GameStatus getStatus() { return status; }
    public List<String> getGuesses() { return new ArrayList<>(guesses); }
    public List<List<LetterFeedback>> getFeedback() { return new ArrayList<>(feedback); }
    public KeyboardState getKeyboard() { return keyboard; }
    public String getSecretDebug() { return secret; } // for testing/demo only


    // ----- Game API -----
    public List<LetterFeedback> submitGuess(String guess) {
        if (status != GameStatus.IN_PROGRESS) throw new IllegalStateException("Game over");
        if (guess == null || guess.length() != WORD_LENGTH) throw new IllegalArgumentException("Guess must be 5 letters");
        guess = guess.toUpperCase();
        if (!guess.matches("[A-Z]{5}")) throw new IllegalArgumentException("Guess must be A-Z only");
        if (!dictionary.isValidWord(guess)) throw new IllegalArgumentException("Not in word list");


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


    public static List<LetterFeedback> evaluate(String guess, String secret) {
        guess = guess.toUpperCase();
        secret = secret.toUpperCase();
        LetterFeedback[] fb = new LetterFeedback[WORD_LENGTH];
        int[] remain = new int[26];


        for (int i = 0; i < WORD_LENGTH; i++) {
            char g = guess.charAt(i), s = secret.charAt(i);
            if (g == s) fb[i] = LetterFeedback.CORRECT; else remain[s - 'A']++;
        }
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (fb[i] != null) continue;
            char g = guess.charAt(i);
            int idx = g - 'A';
            if (idx >= 0 && idx < 26 && remain[idx] > 0) { fb[i] = LetterFeedback.PRESENT; remain[idx]--; }
            else fb[i] = LetterFeedback.ABSENT;
        }
        return Arrays.asList(fb);
    }
}