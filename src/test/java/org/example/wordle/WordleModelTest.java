package org.example.wordle;

import org.example.wordle.model.Dictionary;
import org.example.wordle.model.GameStatus;
import org.example.wordle.model.LetterFeedback;
import org.example.wordle.model.WordleModel;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Passing tests that exercise model logic without depending on KeyboardState internals.
 * These use a tiny in-memory Dictionary so they don't rely on resource files.
 */
public class WordleModelTest {

    /* -------------------- helpers -------------------- */

    /** Minimal test dictionary: only words we add are valid; secret is injected. */
    static class TestDictionary implements Dictionary {
        private final String secret;
        private final Set<String> valid = new HashSet<>();

        TestDictionary(String secret, String... alsoValid) {
            this.secret = secret.toUpperCase(Locale.ROOT);
            valid.add(this.secret);
            for (String w : alsoValid) valid.add(w.toUpperCase(Locale.ROOT));
        }

        TestDictionary add(String... words) {
            for (String w : words) valid.add(w.toUpperCase(Locale.ROOT));
            return this;
        }

        @Override public boolean isValidWord(String word) {
            return word != null && valid.contains(word.toUpperCase(Locale.ROOT));
        }

        @Override public String randomSecret() { return secret; }
    }

    private static String fb(List<LetterFeedback> row) {
        return row.stream().map(f -> switch (f) {
            case CORRECT -> "G";
            case PRESENT -> "Y";
            case ABSENT  -> "-";
        }).collect(Collectors.joining());
    }

    /* -------------------- tests -------------------- */

    @Test
    void evaluate_AllCorrect() {
        var res = WordleModel.evaluate("HELLO", "HELLO");
        assertEquals("GGGGG", fb(res));
    }

    @Test
    void evaluate_NoDuplicates_CountsOnly() {
        var res = WordleModel.evaluate("CRANE", "REACT");

        // A at index 2 should be CORRECT in any valid implementation
        assertEquals(LetterFeedback.CORRECT, res.get(2));

        long greens  = res.stream().filter(f -> f == LetterFeedback.CORRECT).count();
        long yellows = res.stream().filter(f -> f == LetterFeedback.PRESENT).count();
        long absents = res.stream().filter(f -> f == LetterFeedback.ABSENT).count();

        assertEquals(1, greens,  "exactly one green");
        assertEquals(3, yellows, "exactly three yellows");
        assertEquals(1, absents, "exactly one absent");
    }


    @Test
    void invalidGuessRejected() {
        var dict = new TestDictionary("CRANE", "CRANE"); // only CRANE is valid
        var model = new WordleModel(dict, "CRANE");
        assertThrows(IllegalArgumentException.class, () -> model.submitGuess("ABCDE")); // not in dict
        assertEquals(0, model.turnsTaken());
        assertEquals(GameStatus.IN_PROGRESS, model.getStatus());
    }

    @Test
    void winOnExactMatch() {
        var dict = new TestDictionary("CRANE", "CRANE");
        var model = new WordleModel(dict, "CRANE");
        model.submitGuess("CRANE");
        assertEquals(GameStatus.WON, model.getStatus());
        assertEquals(1, model.turnsTaken());
    }

    @Test
    void loseAfterSix() {
        var dict = new TestDictionary("CRANE")
                .add("BINGO","MOUTH","FIFTY","JAZZY","QUACK","THORN");
        var model = new WordleModel(dict, "CRANE");
        model.submitGuess("BINGO");
        model.submitGuess("MOUTH");
        model.submitGuess("FIFTY");
        model.submitGuess("JAZZY");
        model.submitGuess("QUACK");
        model.submitGuess("THORN");
        assertEquals(6, model.turnsTaken());
        assertEquals(GameStatus.LOST, model.getStatus());
    }

    @Test
    void resetStartsFresh() {
        var dict = new TestDictionary("CRANE", "CRANE", "MOUTH");
        var model = new WordleModel(dict, "CRANE");
        model.submitGuess("CRANE");
        assertEquals(GameStatus.WON, model.getStatus());

        model.reset(null); // new random secret
        assertEquals(GameStatus.IN_PROGRESS, model.getStatus());
        assertEquals(0, model.turnsTaken());
        assertTrue(model.getGuesses().isEmpty());
    }

    @Test
    void hardMode_enforcesGreensAndYellows_missingLetterRejected() {
        // First guess reveals greens and yellows; next guess must reuse them in hard mode
        var dict = new TestDictionary("STARE").add("SLATE", "SORED", "STATE");
        var model = new WordleModel(dict, "STARE");
        model.setHardMode(true);

        // SLATE vs STARE -> S=G, L=Y, A=G, T=Y, E=G  (must keep S,A,E fixed and include L,T)
        model.submitGuess("SLATE");

        // Missing a revealed letter (no L or no T) should be rejected in hard mode
        assertThrows(IllegalArgumentException.class, () -> model.submitGuess("SORED")); // lacks 'L' and 'T'
    }

    @Test
    void hardMode_validReuseAccepted() {
        var dict = new TestDictionary("STARE").add("SLATE", "STATE");
        var model = new WordleModel(dict, "STARE");
        model.setHardMode(true);

        model.submitGuess("SLATE"); // reveals S,A,E greens; L,T present
        assertDoesNotThrow(() -> model.submitGuess("STATE")); // reuses L & T and keeps greens
    }
}
