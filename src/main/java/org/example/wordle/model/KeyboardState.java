package org.example.wordle.model;


import java.util.HashMap;
import java.util.Map;


public class KeyboardState {
    private final Map<Character, LetterFeedback> status = new HashMap<>();


    public Map<Character, LetterFeedback> snapshot() { return new HashMap<>(status); }


    public void upgrade(char c, LetterFeedback fb) {
        c = Character.toUpperCase(c);
        LetterFeedback current = status.get(c);
        if (current == null || precedence(fb) > precedence(current)) status.put(c, fb);
    }


    public void clear() { status.clear(); }


    private int precedence(LetterFeedback fb) {
        switch (fb) {
            case ABSENT: return 1;
            case PRESENT: return 2;
            case CORRECT: return 3;
        }
        return 0;
    }
}