package org.example.wordle.model;

/**
 * Permissive dictionary: accepts any 5-letter A–Z word as a valid guess,
 * but still chooses the secret from another dictionary (e.g., SimpleDictionary).
 */
public class OpenDictionary implements Dictionary {
    private final Dictionary secretSource;

    public OpenDictionary(Dictionary secretSource) {
        this.secretSource = secretSource;
    }

    @Override
    public boolean isValidWord(String word) {
        return word != null && word.matches("[A-Za-z]{5}");
    }

    @Override
    public String randomSecret() {
        return secretSource.randomSecret();
    }
}
