package it.unicam.cs.mpgc.rpg130017.ui;

import it.unicam.cs.mpgc.rpg130017.launcher.MainApp;
import it.unicam.cs.mpgc.rpg130017.model.Racer;
import it.unicam.cs.mpgc.rpg130017.model.TournamentRaceMode;
import it.unicam.cs.mpgc.rpg130017.service.RaceService;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TournamentRaceView {

    private final BorderPane root;
    private final MainApp app;
    private final TournamentRaceMode race;

    private static final double GAUGE_W = 550.0;
    private static final double GAUGE_H = 36.0;

    // Player HUD
    private Label feedbackLabel;
    private Label countdownLabel;
    private Label speedLabel;
    private Label gearLabel;
    private ProgressBar playerBar;
    private Rectangle rpmCursor;
    private Label rpmLabel;

    // Leaderboard rows: one per participant (player + opponents)
    private final List<Label>       lbNames  = new ArrayList<>();
    private final List<Label>       lbPos    = new ArrayList<>();
    private final List<ProgressBar> lbBars   = new ArrayList<>();

    private boolean countdownActive = true;
    private long lastTime = 0;
    private AnimationTimer gameLoop;

    public TournamentRaceView(MainApp app, TournamentRaceMode race) {
        this.app  = app;
        this.race = race;
        this.root = new BorderPane();
        this.root.getStyleClass().add("root");
        this.root.setPadding(new Insets(18));

        root.setCenter(buildCenter());
        root.setRight(buildLeaderboard());
        BorderPane.setMargin(root.getRight(), new Insets(0, 0, 0, 18));

        setupInputHandlers();
    }

    // ── Center: player HUD + rpm gauge ─────────────────────────────────────
    private VBox buildCenter() {
        VBox center = new VBox(18);
        center.setAlignment(Pos.CENTER);

        // Header
        Label title = new Label("TOURNAMENT RACE");
        title.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 22px; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(255,215,0,0.5), 8, 0, 0, 0);");

        Label driverLabel = new Label(app.getPlayer().getName().toUpperCase());
        driverLabel.setStyle("-fx-text-fill: #00f3ff; -fx-font-size: 15px; -fx-font-weight: bold;");

        HBox topRow = new HBox(30, title, driverLabel);
        topRow.setAlignment(Pos.CENTER);
        topRow.getStyleClass().add("panel-dark");
        topRow.setPadding(new Insets(12));

        // Player progress bar
        VBox pTrack = new VBox(6);
        pTrack.getStyleClass().add("panel-dark");
        pTrack.setPadding(new Insets(12));

        HBox pLabels = new HBox(10);
        pLabels.setAlignment(Pos.CENTER_LEFT);
        Label pTitle = new Label("YOUR PROGRESS");
        pTitle.setStyle("-fx-text-fill: #00f3ff; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        gearLabel  = new Label("GEAR 1");
        gearLabel.setStyle("-fx-text-fill: #00f3ff; -fx-font-weight: bold;");
        speedLabel = new Label("0 km/h");
        speedLabel.setStyle("-fx-text-fill: #fff; -fx-font-weight: bold;");
        pLabels.getChildren().addAll(pTitle, sp, gearLabel, speedLabel);

        playerBar = new ProgressBar(0);
        playerBar.setPrefWidth(700); playerBar.setPrefHeight(14);

        pTrack.getChildren().addAll(pLabels, playerBar);

        // RPM Gauge
        VBox gaugeBox = buildRpmGauge();

        // Feedback / countdown
        countdownLabel = new Label("GET READY!");
        countdownLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 28px; -fx-font-weight: bold;");
        feedbackLabel = new Label("");
        feedbackLabel.setStyle("-fx-text-fill: #00f3ff; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label hint = new Label("SPACEBAR → SHIFT GEAR");
        hint.setStyle("-fx-text-fill: #555566; -fx-font-size: 11px;");

        center.getChildren().addAll(topRow, pTrack, gaugeBox, countdownLabel, feedbackLabel, hint);
        return center;
    }

    private VBox buildRpmGauge() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);

        Pane pane = new Pane();
        pane.setPrefSize(GAUGE_W, GAUGE_H);
        pane.setStyle("-fx-background-color: linear-gradient(to right, #121216 0%, #1a1a2e 60%, #3a0000 85%, #ff0055 100%); " +
                "-fx-border-color: #00f3ff; -fx-border-width: 2px; -fx-border-radius: 4px; -fx-background-radius: 4px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,243,255,0.4), 10, 0, 0, 0);");

        // Good zone
        double gs = race.getGoodZoneStart(), ge = race.getGoodZoneEnd();
        Rectangle good = new Rectangle(GAUGE_W * (ge - gs), GAUGE_H - 4);
        good.setX(GAUGE_W * gs); good.setY(2);
        good.setFill(Color.web("#ffd700", 0.3));

        // Perfect zone
        double ps = race.getPerfectZoneStart(), pe = race.getPerfectZoneEnd();
        Rectangle perfect = new Rectangle(GAUGE_W * (pe - ps), GAUGE_H - 4);
        perfect.setX(GAUGE_W * ps); perfect.setY(2);
        perfect.setFill(Color.web("#00ff66", 0.7));

        rpmCursor = new Rectangle(4, GAUGE_H - 4);
        rpmCursor.setX(0); rpmCursor.setY(2);
        rpmCursor.setFill(Color.web("#00f3ff"));

        pane.getChildren().addAll(good, perfect, rpmCursor);

        rpmLabel = new Label("RPM: 0%");
        rpmLabel.setStyle("-fx-text-fill: #e2e2e8; -fx-font-weight: bold;");

        box.getChildren().addAll(pane, rpmLabel);
        return box;
    }

    // ── Right: live leaderboard ─────────────────────────────────────────────
    private VBox buildLeaderboard() {
        VBox board = new VBox(10);
        board.getStyleClass().add("panel-glowing");
        board.setPrefWidth(220);
        board.setPadding(new Insets(14));

        Label header = new Label("LIVE STANDINGS");
        header.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 14px; -fx-font-weight: bold;");
        board.getChildren().add(header);

        // Player row
        board.getChildren().add(buildLbRow(0, app.getPlayer().getName(), true));

        // AI rows
        List<Racer> opps = race.getOpponents();
        for (int i = 0; i < opps.size(); i++) {
            board.getChildren().add(buildLbRow(i + 1, opps.get(i).getName(), false));
        }

        return board;
    }

    /** Builds one leaderboard row (position label + name + mini progress bar). */
    private VBox buildLbRow(int idx, String name, boolean isPlayer) {
        VBox row = new VBox(3);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.04); " +
                "-fx-padding: 6px; -fx-background-radius: 5px;");

        Label pos = new Label("P?");
        pos.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold; -fx-font-size: 13px;");
        lbPos.add(pos);

        Label nm = new Label((isPlayer ? "★ " : "") + name);
        nm.setStyle("-fx-text-fill: " + (isPlayer ? "#00f3ff" : "#e2e2e8") +
                "; -fx-font-size: 12px; -fx-font-weight: bold;");
        lbNames.add(nm);

        HBox top = new HBox(6, pos, nm);
        top.setAlignment(Pos.CENTER_LEFT);

        ProgressBar bar = new ProgressBar(0);
        bar.setPrefWidth(192); bar.setPrefHeight(6);
        if (!isPlayer) bar.getStyleClass().add("opponent-bar");
        lbBars.add(bar);

        row.getChildren().addAll(top, bar);
        return row;
    }

    // ── Input ──────────────────────────────────────────────────────────────
    private void setupInputHandlers() {
        root.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                newS.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
                    if (ev.getCode() == javafx.scene.input.KeyCode.SPACE) {
                        if (!countdownActive && !race.isFinished()) {
                            race.shiftPlayerGear();
                            ev.consume();
                        }
                    }
                });
            }
        });
        root.setFocusTraversable(true);
    }

    // ── Countdown + game loop ──────────────────────────────────────────────
    public void startRaceCountdown() {
        countdownActive = true;
        countdownLabel.setText("3...");
        Timeline tl = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> countdownLabel.setText("2...")),
                new KeyFrame(Duration.seconds(2), e -> countdownLabel.setText("1...")),
                new KeyFrame(Duration.seconds(3), e -> {
                    countdownLabel.setText("GO!");
                    countdownActive = false;
                    lastTime = System.nanoTime();
                    startLoop();
                }),
                new KeyFrame(Duration.seconds(4), e -> countdownLabel.setText(""))
        );
        tl.play();
    }

    private void startLoop() {
        gameLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = Math.min(0.1, (now - lastTime) / 1_000_000_000.0);
                lastTime = now;

                race.update(dt);
                updateHud();

                if (race.isFinished()) { stop(); onFinished(); }
            }
        };
        gameLoop.start();
    }

    private void updateHud() {
        // Player HUD
        playerBar.setProgress(race.getPlayerProgress());
        speedLabel.setText(String.format("%.0f km/h", race.getPlayerSpeed()));
        if (race.getPlayerGear() == race.getMaxGears()) {
            gearLabel.setText("GEAR " + race.getPlayerGear() + " (MAX)");
            gearLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");
        } else {
            gearLabel.setText("GEAR " + race.getPlayerGear());
            gearLabel.setStyle("-fx-text-fill: #00f3ff; -fx-font-weight: bold;");
        }
        feedbackLabel.setText(race.getPlayerFeedback());

        double rpm = race.getPlayerRpm();
        rpmCursor.setX(GAUGE_W * rpm);
        rpmLabel.setText(String.format("RPM: %.0f%%", rpm * 100));
        if      (rpm >= 0.95)                               rpmCursor.setFill(Color.web("#ff0055"));
        else if (rpm >= race.getPerfectZoneStart() &&
                 rpm <= race.getPerfectZoneEnd())            rpmCursor.setFill(Color.web("#00ff66"));
        else                                                 rpmCursor.setFill(Color.web("#00f3ff"));

        // Leaderboard standings
        List<double[]> standings = race.getLiveStandings();
        for (int rank = 0; rank < standings.size(); rank++) {
            double[] entry  = standings.get(rank);
            int participantIdx = (int) entry[0]; // 0 = player, 1..N = AI index
            double dist     = entry[1];
            double progress = Math.min(1.0, dist / 402.33);
            String posLabel = "P" + (rank + 1);

            lbPos.get(participantIdx).setText(posLabel);
            lbBars.get(participantIdx).setProgress(progress);
        }
    }

    private void onFinished() {
        int pos = race.getPlayerFinishPos();
        RaceService.RaceRewards rewards = app.getRaceService().processTournamentResult(app.getPlayer(), pos);
        // Tournament wins don't unlock the boss (separate track)
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(2.5),
                e -> app.showTournamentResults(pos, rewards.getMoney(), rewards.getReputation())));
        delay.play();
    }

    public BorderPane getView() {
        root.requestFocus();
        return root;
    }
}
