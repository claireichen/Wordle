package org.example.wordle.viewfx;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.wordle.model.KeyboardState;
import org.example.wordle.model.LetterFeedback;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class KeyboardViewFX extends VBox {
    private static final String[] ROWS = { "QWERTYUIOP", "ASDFGHJKL", "⌫ZXCVBNM⏎" };
    private final Map<Character, Button> buttons = new HashMap<>();
    private Consumer<String> handler = s -> {};

    public KeyboardViewFX() {
        setSpacing(6);
        setPadding(new Insets(8));
        setAlignment(Pos.CENTER);

        for (String row : ROWS) {
            HBox h = new HBox(6);
            h.setAlignment(Pos.CENTER);
            for (char ch : row.toCharArray()) {
                String label = String.valueOf(ch);
                Button b = new Button(label);
                b.setPrefHeight(40);
                b.setMinWidth((ch == '⏎' || ch == '⌫') ? 60 : 36);
                b.setOnAction(e -> handler.accept(label));
                h.getChildren().add(b);
                buttons.put(ch, b);
            }
            getChildren().add(h);
        }
    }

    public void setHandler(Consumer<String> h) { this.handler = h; }

    public void updateColors(KeyboardState state) {
        Map<Character, LetterFeedback> map = state.snapshot();
        for (var entry : buttons.entrySet()) {
            char ch = entry.getKey();
            Button b = entry.getValue();
            LetterFeedback fb = map.get(Character.toUpperCase(ch));
            if (fb == null) {
                b.setStyle("");
            } else {
                String color = switch (fb) {
                    case CORRECT -> "#6AAA64";
                    case PRESENT -> "#C9B458";
                    case ABSENT -> "#787C7E";
                };
                b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
            }
        }
    }
}