package org.example.wordle.control;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.example.wordle.model.GameStatus;
import org.example.wordle.model.WordleModel;
import org.example.wordle.viewfx.BoardViewFX;
import org.example.wordle.viewfx.KeyboardViewFX;

import java.util.Optional;
import java.util.function.BiConsumer;

public class GameControllerFX {
    private final WordleModel model;
    private final BoardViewFX board;
    private final KeyboardViewFX keyboard;
    private final StringBuilder current = new StringBuilder();

    // Notifies when a game ends: (status, turnsTaken)
    private final BiConsumer<GameStatus, Integer> onGameFinished;

    public GameControllerFX(WordleModel model, BoardViewFX board, KeyboardViewFX keyboard) {
        this(model, board, keyboard, null);
    }

    public GameControllerFX(WordleModel model,
                            BoardViewFX board,
                            KeyboardViewFX keyboard,
                            BiConsumer<GameStatus, Integer> onGameFinished) {
        this.model = model;
        this.board = board;
        this.keyboard = keyboard;
        this.onGameFinished = onGameFinished;
        keyboard.setHandler(this::handleButton);
    }

    public void attachToScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
    }

    private void handleButton(String label) {
        switch (label) {
            case "⌫" -> backspace();
            case "⏎" -> enter();
            default -> {
                if (label.matches("[A-Z]")) type(label.charAt(0));
            }
        }
    }

    private void handleKey(KeyEvent e) {
        if (e.getCode().isLetterKey()) {
            type(e.getText().toUpperCase().charAt(0));
        } else if (e.getCode() == KeyCode.BACK_SPACE) {
            backspace();
        } else if (e.getCode() == KeyCode.ENTER) {
            enter();
        }
    }

    private void type(char c) {
        if (model.getStatus() != GameStatus.IN_PROGRESS) return;
        if (current.length() < WordleModel.WORD_LENGTH) {
            current.append(Character.toUpperCase(c));
            board.setPreview(current.toString());
        }
    }

    private void backspace() {
        if (current.length() > 0) {
            current.deleteCharAt(current.length() - 1);
            board.setPreview(current.toString());
        }
    }

    private void enter() {
        if (model.getStatus() != GameStatus.IN_PROGRESS) return;
        if (current.length() != WordleModel.WORD_LENGTH) {
            toast("Not enough letters");
            return;
        }
        try {
            model.submitGuess(current.toString());
            keyboard.updateColors(model.getKeyboard());
            current.setLength(0);
            board.setPreview("");

            if (model.getStatus() == GameStatus.WON || model.getStatus() == GameStatus.LOST) {
                if (onGameFinished != null) onGameFinished.accept(model.getStatus(), model.turnsTaken());
                endOfGamePrompt();
            }
        } catch (IllegalArgumentException ex) {
            toast(ex.getMessage());
        }
    }

    private void endOfGamePrompt() {
        String msg = (model.getStatus() == GameStatus.WON)
                ? "You won! The word was: " + model.getSecretDebug()
                : "You lost! The word was: " + model.getSecretDebug();

        ButtonType playAgain = new ButtonType("Play Again");
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, playAgain, close);
        a.setHeaderText(null);
        a.setTitle("Wordle");

        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == playAgain) {
            model.reset(null);                 // picks a NEW random secret via dictionary
            keyboard.updateColors(model.getKeyboard());
            board.setPreview("");
        }
    }

    private void toast(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.setTitle("Wordle");
        a.showAndWait();
    }
}
