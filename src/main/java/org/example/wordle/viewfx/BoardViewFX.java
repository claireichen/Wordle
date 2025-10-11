package org.example.wordle.viewfx;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import org.example.wordle.model.LetterFeedback;
import org.example.wordle.model.WordleModel;
import org.example.wordle.util.ModelListener;

import java.util.ArrayList;
import java.util.List;

public class BoardViewFX extends GridPane implements ModelListener {
    private static final int TILE = 56;
    private static final int GAP = 8;

    private final WordleModel model;
    private final List<Tile> tiles = new ArrayList<>();
    private String preview = ""; // current unsubmitted guess

    public BoardViewFX(WordleModel model) {
        this.model = model;
        this.model.addListener(this);
        setHgap(GAP);
        setVgap(GAP);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: white;");

// create grid
        for (int r = 0; r < WordleModel.MAX_TURNS; r++) {
            for (int c = 0; c < WordleModel.WORD_LENGTH; c++) {
                Tile t = new Tile();
                tiles.add(t);
                add(t, c, r);
            }
        }
        render();
    }

    public void setPreview(String text) {
        this.preview = text == null ? "" : text.toUpperCase();
        render();
    }

    @Override public void onModelChanged() { render(); }

    private void render() {
// clear
        for (Tile t : tiles) t.setNeutral();

// filled rows
        var guesses = model.getGuesses();
        var feedback = model.getFeedback();
        for (int r = 0; r < guesses.size(); r++) {
            String g = guesses.get(r);
            for (int c = 0; c < WordleModel.WORD_LENGTH; c++) {
                LetterFeedback lf = feedback.get(r).get(c);
                tiles.get(r * WordleModel.WORD_LENGTH + c).setResult(g.charAt(c), lf);
            }
        }
// preview row
        int row = guesses.size();
        if (row < WordleModel.MAX_TURNS && preview != null) {
            for (int c = 0; c < preview.length() && c < WordleModel.WORD_LENGTH; c++) {
                tiles.get(row * WordleModel.WORD_LENGTH + c).setPreview(preview.charAt(c));
            }
        }
    }

    private static class Tile extends StackPane {
        private final Rectangle rect = new Rectangle(TILE, TILE);
        private final Text letter = new Text("");

        Tile() {
            rect.setArcWidth(8); rect.setArcHeight(8);
            rect.setStroke(Color.GRAY);
            rect.setFill(Color.web("#ECEFF3"));
            letter.setFont(Font.font("System", FontWeight.BOLD, 24));
            getChildren().addAll(rect, letter);
            setAlignment(Pos.CENTER);
        }

        void setNeutral() {
            rect.setStroke(Color.web("#AAB0B6"));
            rect.setFill(Color.web("#ECEFF3"));
            letter.setFill(Color.BLACK);
            letter.setText("");
        }

        void setPreview(char ch) {
            rect.setStroke(Color.web("#5A6B7A"));
            rect.setFill(Color.WHITE);
            letter.setFill(Color.BLACK);
            letter.setText(String.valueOf(Character.toUpperCase(ch)));
        }

        void setResult(char ch, LetterFeedback fb) {
            switch (fb) {
                case CORRECT: rect.setFill(Color.web("#6AAA64")); break;
                case PRESENT: rect.setFill(Color.web("#C9B458")); break;
                case ABSENT: rect.setFill(Color.web("#787C7E")); break;
            }
            rect.setStroke(Color.TRANSPARENT);
            letter.setFill(Color.WHITE);
            letter.setText(String.valueOf(Character.toUpperCase(ch)));
        }
    }
}
