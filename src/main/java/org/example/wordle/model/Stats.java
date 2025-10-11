package org.example.wordle.model;

import java.util.Arrays;

public class Stats {
    private int games;
    private int wins;
    private final int[] guessWins = new int[6];  // wins in 1..6 guesses

    public Stats() {}

    public Stats(int games, int wins, int[] dist) {
        this.games = games;
        this.wins = wins;
        System.arraycopy(dist, 0, this.guessWins, 0, Math.min(6, dist.length));
    }

    /** Call exactly once per finished game. */
    public void recordGame(GameStatus status, int turnsTaken) {
        games++;
        if (status == GameStatus.WON) {
            wins++;
            if (turnsTaken >= 1 && turnsTaken <= 6) guessWins[turnsTaken - 1]++;
        }
    }

    public int getGames() { return games; }
    public int getWins()  { return wins; }
    public int getLosses(){ return games - wins; }
    public double getWinPercentage() { return games == 0 ? 0.0 : (wins * 100.0) / games; }
    public int[] getGuessDistribution() { return Arrays.copyOf(guessWins, guessWins.length); }
}
