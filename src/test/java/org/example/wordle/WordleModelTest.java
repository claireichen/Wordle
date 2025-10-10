package org.example.wordle;

import org.junit.jupiter.api.Test;
import org.example.wordle.model.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WordleModelTest {

    private static class StubDict implements Dictionary {
        @Override public boolean isValidWord(String word) { return word != null && word.matches("[A-Z]{5}"); }
        @Override public String randomSecret() { return "HELLO"; }
    }

    @Test public void evaluate_AllCorrect() {
        List<LetterFeedback> fb = WordleModel.evaluate("HELLO", "HELLO");
        assertEquals(Arrays.asList(LetterFeedback.CORRECT, LetterFeedback.CORRECT, LetterFeedback.CORRECT,
                LetterFeedback.CORRECT, LetterFeedback.CORRECT), fb);
    }

    @Test public void evaluate_DuplicatesHandled() {
        List<LetterFeedback> fb = WordleModel.evaluate("LEVEL", "HELLO");
        assertEquals(LetterFeedback.PRESENT, fb.get(0));
        assertEquals(LetterFeedback.CORRECT, fb.get(1));
        assertEquals(LetterFeedback.PRESENT, fb.get(2));
    }

    @Test public void submitGuess_Win() {
        WordleModel m = new WordleModel(new StubDict(), "HELLO");
        m.submitGuess("HELLO");
        assertEquals(GameStatus.WON, m.getStatus());
        assertEquals(1, m.turnsTaken());
    }

    @Test public void submitGuess_RejectsInvalid() {
        WordleModel m = new WordleModel(new StubDict(), "HELLO");
        assertThrows(IllegalArgumentException.class, () -> m.submitGuess("12345"));
    }

    @Test public void loseAfterSix() {
        WordleModel m = new WordleModel(new StubDict(), "HELLO");
        for (int i = 0; i < 6; i++) m.submitGuess("WORLD");
        assertEquals(GameStatus.LOST, m.getStatus());
    }

    @Test public void keyboardUpgradesPrecedence() {
        WordleModel m = new WordleModel(new StubDict(), "HELLO");
        m.submitGuess("AAAAA");
        m.submitGuess("ALARM");
        m.submitGuess("ABACK");
        assertEquals(LetterFeedback.CORRECT, m.getKeyboard().snapshot().get('A'));
    }

    @Test public void cannotGuessAfterGameOver() {
        WordleModel m = new WordleModel(new StubDict(), "HELLO");
        m.submitGuess("HELLO");
        assertThrows(IllegalStateException.class, () -> m.submitGuess("WORLD"));
    }

    @Test public void resetStartsFresh() {
        WordleModel m = new WordleModel(new StubDict(), "HELLO");
        m.submitGuess("HOLLY");
        m.reset("HELLO");
        assertEquals(0, m.turnsTaken());
        assertEquals(GameStatus.IN_PROGRESS, m.getStatus());
    }
}