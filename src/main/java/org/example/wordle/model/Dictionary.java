package org.example.wordle.model;

public interface Dictionary {
    boolean isValidWord(String word);
    String randomSecret();
}