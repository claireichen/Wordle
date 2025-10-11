package org.example.wordle;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.example.wordle.control.GameControllerFX;
import org.example.wordle.io.Persistence;
import org.example.wordle.io.StatsIO;
import org.example.wordle.model.Dictionary;
import org.example.wordle.model.OpenDictionary;
import org.example.wordle.model.SimpleDictionary;
import org.example.wordle.model.Stats;
import org.example.wordle.model.WordleModel;
import org.example.wordle.model.HintEngine;
import org.example.wordle.viewfx.BoardViewFX;
import org.example.wordle.viewfx.KeyboardViewFX;

import java.nio.file.Path;

public class App extends Application {
    private WordleModel model;
    private BoardViewFX board;
    private KeyboardViewFX keyboard;
    private GameControllerFX controller;
    private HintEngine hints;
    private Stats stats;
    private Stage primary;

    private static final Path SAVE_PATH  =
            Path.of(System.getProperty("user.home"), ".wordle", "save.txt");
    private static final Path STATS_PATH =
            Path.of(System.getProperty("user.home"), ".wordle", "stats.txt");

    @Override
    public void start(Stage stage) {
        // Dictionary: accept any A–Z guess, but choose secret from wordlist.txt
        Dictionary dict = new OpenDictionary(new SimpleDictionary());

        this.model = new WordleModel(dict);
        this.board = new BoardViewFX(model);
        this.keyboard = new KeyboardViewFX();
        this.hints = new HintEngine();

        // Load stats (or start fresh)
        try { this.stats = StatsIO.load(STATS_PATH); }
        catch (Exception e) { this.stats = new Stats(); }

        // Controller with "game finished" callback to update stats + persist
        this.controller = new GameControllerFX(model, board, keyboard, (status, turns) -> {
            stats.recordGame(status, turns);
            try { StatsIO.save(STATS_PATH, stats); } catch (Exception ignore) {}
        });

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setCenter(board);
        root.setBottom(new HBox(keyboard));

        // Menu bar
        MenuBar mb = new MenuBar();
        Menu game = new Menu("Game");
        MenuItem miReset = new MenuItem("Reset");
        MenuItem miSave  = new MenuItem("Save");
        MenuItem miLoad  = new MenuItem("Load");
        MenuItem miHint  = new MenuItem("Smart Hint…");
        MenuItem miStats = new MenuItem("Statistics…");
        game.getItems().addAll(miReset, miSave, miLoad, miHint, miStats);
        mb.getMenus().add(game);
        root.setTop(mb);

        miReset.setOnAction(e -> model.reset(null));
        miSave.setOnAction(e -> save());
        miLoad.setOnAction(e -> load());
        miHint.setOnAction(e -> showHint());
        miStats.setOnAction(e -> showStats());

        Scene scene = new Scene(root, 480, 640);
        controller.attachToScene(scene);
        stage.setTitle("Wordle — JavaFX MVC Starter");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        this.primary = stage;
    }

    private void save() {
        try {
            Persistence.save(SAVE_PATH, model.getSecretDebug(), model.getGuesses(), model.getStatus());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void load() {
        try {
            var loaded = Persistence.load(SAVE_PATH);

            // Keep OpenDictionary behavior on load as well
            Dictionary dict = new OpenDictionary(new SimpleDictionary());
            WordleModel newModel = new WordleModel(dict, loaded.secret);
            for (String g : loaded.guesses) newModel.submitGuess(g);

            // Swap into UI
            var scene = primary.getScene();
            BorderPane root = (BorderPane) scene.getRoot();
            BoardViewFX newBoard = new BoardViewFX(newModel);
            KeyboardViewFX newKeyboard = new KeyboardViewFX();
            GameControllerFX newController = new GameControllerFX(
                    newModel, newBoard, newKeyboard,
                    (status, turns) -> {
                        stats.recordGame(status, turns);
                        try { StatsIO.save(STATS_PATH, stats); } catch (Exception ignore) {}
                    });

            root.setCenter(newBoard);
            root.setBottom(new HBox(newKeyboard));
            newController.attachToScene(scene);

            this.model = newModel;
            this.board = newBoard;
            this.keyboard = newKeyboard;
            this.controller = newController;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showHint() {
        var suggestions = hints.suggest(model.getGuesses(), model.getFeedback(), 5);
        String body = suggestions.isEmpty()
                ? "No suggestions (constraints too tight)."
                : String.join(", ", suggestions);
        var a = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION, body);
        a.setHeaderText("Smart Hint (top candidates)");
        a.setTitle("Hint");
        a.showAndWait();
    }

    private void showStats() {
        var box = new javafx.scene.layout.VBox(8);
        box.setPadding(new Insets(12));

        int games = stats.getGames();
        int wins = stats.getWins();
        int losses = stats.getLosses();
        double winPct = stats.getWinPercentage();
        int[] dist = stats.getGuessDistribution();

        var header = new javafx.scene.control.Label("Statistics");
        header.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        var line1 = new javafx.scene.control.Label("Games Played: " + games);
        var line2 = new javafx.scene.control.Label(String.format("Win %%: %.1f%%", winPct));
        var line3 = new javafx.scene.control.Label("Wins: " + wins + "    Losses: " + losses);
        box.getChildren().addAll(header, line1, line2, line3, new javafx.scene.control.Separator());

        int max = 1; for (int v : dist) max = Math.max(max, v);
        int barMaxWidth = 280;
        for (int i = 0; i < 6; i++) {
            int v = dist[i];
            double w = (max == 0) ? 0 : (barMaxWidth * (v / (double) max));
            var row = new javafx.scene.layout.HBox(8);
            var lab = new javafx.scene.control.Label((i + 1) + ":");
            lab.setPrefWidth(24);
            var bar = new javafx.scene.layout.Region();
            bar.setPrefWidth(Math.max(4, w));
            bar.setMinHeight(18); bar.setMaxHeight(18);
            bar.setStyle("-fx-background-color: #6AAA64; -fx-background-radius: 4;");
            var count = new javafx.scene.control.Label(" " + v);
            row.getChildren().addAll(lab, bar, count);
            box.getChildren().add(row);
        }

        var sc = new javafx.scene.Scene(box);
        var st = new javafx.stage.Stage();
        st.setTitle("Wordle — Statistics");
        st.initOwner(primary);
        st.setScene(sc);
        st.setResizable(false);
        st.show();
    }

    private Stage getPrimaryStage() { return primary; }

    public static void main(String[] args) { launch(args); }
}
