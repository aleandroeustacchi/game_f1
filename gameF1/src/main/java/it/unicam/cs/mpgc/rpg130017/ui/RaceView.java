package it.unicam.cs.mpgc.rpg130017.ui;

import it.unicam.cs.mpgc.rpg130017.launcher.MainApp;
import it.unicam.cs.mpgc.rpg130017.model.RaceMode;
import it.unicam.cs.mpgc.rpg130017.model.Racer;
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

public class RaceView {
    private final VBox root;
    private final MainApp app;
    private final Racer opponent;
    private final boolean isBoss;
    
    private RaceMode race;
    private AnimationTimer gameLoop;
    
    // UI Elements
    private Label countdownLabel;
    private Label feedbackLabel;
    
    private ProgressBar playerProgress;
    private Label playerSpeedLabel;
    private Label playerGearLabel;
    
    private ProgressBar opponentProgress;
    private Label opponentSpeedLabel;
    private Label opponentGearLabel;
    
    // Custom RPM Gauge
    private final double GAUGE_WIDTH = 600.0;
    private final double GAUGE_HEIGHT = 40.0;
    private StackPane rpmGaugeContainer;
    private Rectangle rpmCursor;
    private Label rpmTextLabel;
    
    private boolean countdownActive = true;
    private long lastTime = 0;

    public RaceView(MainApp app, Racer opponent, boolean isBoss) {
        this.app = app;
        this.opponent = opponent;
        this.isBoss = isBoss;
        
        this.root = new VBox(25);
        this.root.setAlignment(Pos.CENTER);
        this.root.getStyleClass().add("root");
        this.root.setPadding(new Insets(30));
        
        // Grab logic instance
        this.race = app.getRaceService().createDragRace(app.getPlayer(), opponent);

        setupHeader();
        setupTracks();
        setupRpmGauge();
        setupFeedbackArea();
        setupInputHandlers();
    }

    private void setupHeader() {
        HBox header = new HBox(50);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("panel-dark");
        header.setPadding(new Insets(15));

        VBox pInfo = new VBox(5);
        pInfo.setAlignment(Pos.CENTER);
        Label pName = new Label(app.getPlayer().getName().toUpperCase());
        pName.setStyle("-fx-text-fill: #00f3ff; -fx-font-weight: bold; -fx-font-size: 16px;");
        Label pCar = new Label("Level " + app.getPlayer().getCar().getEngineLevel() + " Engine");
        pCar.setStyle("-fx-text-fill: #8a8a99; -fx-font-size: 12px;");
        pInfo.getChildren().addAll(pName, pCar);

        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-text-fill: #ff0055; -fx-font-size: 24px; -fx-font-weight: bold;");

        VBox oInfo = new VBox(5);
        oInfo.setAlignment(Pos.CENTER);
        Label oName = new Label(opponent.getName().toUpperCase());
        oName.setStyle("-fx-text-fill: #ff0055; -fx-font-weight: bold; -fx-font-size: 16px;");
        Label oCar = new Label("Level " + opponent.getCar().getEngineLevel() + " Engine");
        oCar.setStyle("-fx-text-fill: #8a8a99; -fx-font-size: 12px;");
        oInfo.getChildren().addAll(oName, oCar);

        header.getChildren().addAll(pInfo, vsLabel, oInfo);
        root.getChildren().add(header);
    }

    private void setupTracks() {
        VBox tracksContainer = new VBox(20);
        tracksContainer.getStyleClass().add("panel-dark");
        tracksContainer.setPadding(new Insets(20));

        // Player Track
        VBox pTrack = new VBox(6);
        HBox pLabels = new HBox(10);
        pLabels.setAlignment(Pos.CENTER_LEFT);
        Label pTitle = new Label("YOU");
        pTitle.setStyle("-fx-text-fill: #00f3ff; -fx-font-weight: bold;");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        playerGearLabel = new Label("GEAR 1");
        playerGearLabel.setStyle("-fx-text-fill: #00f3ff; -fx-font-weight: bold;");
        playerSpeedLabel = new Label("0 km/h");
        playerSpeedLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        pLabels.getChildren().addAll(pTitle, spacer1, playerGearLabel, playerSpeedLabel);

        playerProgress = new ProgressBar(0.0);
        playerProgress.setPrefWidth(900);
        playerProgress.setPrefHeight(15);
        pTrack.getChildren().addAll(pLabels, playerProgress);

        // Opponent Track
        VBox oTrack = new VBox(6);
        HBox oLabels = new HBox(10);
        oLabels.setAlignment(Pos.CENTER_LEFT);
        Label oTitle = new Label(opponent.getName().toUpperCase());
        oTitle.setStyle("-fx-text-fill: #ff0055; -fx-font-weight: bold;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        opponentGearLabel = new Label("GEAR 1");
        opponentGearLabel.setStyle("-fx-text-fill: #ff0055; -fx-font-weight: bold;");
        opponentSpeedLabel = new Label("0 km/h");
        opponentSpeedLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        oLabels.getChildren().addAll(oTitle, spacer2, opponentGearLabel, opponentSpeedLabel);

        opponentProgress = new ProgressBar(0.0);
        opponentProgress.getStyleClass().add("opponent-bar");
        opponentProgress.setPrefWidth(900);
        opponentProgress.setPrefHeight(15);
        oTrack.getChildren().addAll(oLabels, opponentProgress);

        tracksContainer.getChildren().addAll(pTrack, oTrack);
        root.getChildren().add(tracksContainer);
    }

    private void setupRpmGauge() {
        VBox gaugeWrapper = new VBox(8);
        gaugeWrapper.setAlignment(Pos.CENTER);

        rpmGaugeContainer = new StackPane();
        rpmGaugeContainer.setMaxWidth(GAUGE_WIDTH);
        rpmGaugeContainer.setPrefWidth(GAUGE_WIDTH);
        rpmGaugeContainer.setPrefHeight(GAUGE_HEIGHT);
        rpmGaugeContainer.setStyle("-fx-background-color: #121216; -fx-border-color: #32323e; -fx-border-width: 2px; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        // 1. Draw Good Zone Rect
        double goodStartVal = race.getGoodZoneStart();
        double goodEndVal = race.getGoodZoneEnd();
        double goodWidth = GAUGE_WIDTH * (goodEndVal - goodStartVal);
        Rectangle goodZoneRect = new Rectangle(goodWidth, GAUGE_HEIGHT - 4);
        goodZoneRect.setFill(Color.web("#ffd700", 0.35)); // Translucent Gold
        
        // 2. Draw Perfect Zone Rect
        double perfectStartVal = race.getPerfectZoneStart();
        double perfectEndVal = race.getPerfectZoneEnd();
        double perfectWidth = GAUGE_WIDTH * (perfectEndVal - perfectStartVal);
        Rectangle perfectZoneRect = new Rectangle(perfectWidth, GAUGE_HEIGHT - 4);
        perfectZoneRect.setFill(Color.web("#00ff66", 0.7)); // High opacity Green

        // Align StackPane children
        // In JavaFX, StackPane overlays items. We can set alignments or translateX.
        // Let's create an anchor or set relative coordinates using a Pane.
        // StackPane is easiest if we position with Alignment.
        Pane overlayPane = new Pane();
        overlayPane.setPrefSize(GAUGE_WIDTH, GAUGE_HEIGHT);
        
        goodZoneRect.setX(GAUGE_WIDTH * goodStartVal);
        goodZoneRect.setY(2);
        
        perfectZoneRect.setX(GAUGE_WIDTH * perfectStartVal);
        perfectZoneRect.setY(2);

        // 3. Draw cursor
        rpmCursor = new Rectangle(4, GAUGE_HEIGHT - 4);
        rpmCursor.setFill(Color.web("#00f3ff")); // Cyan neon pointer
        rpmCursor.setX(0);
        rpmCursor.setY(2);

        overlayPane.getChildren().addAll(goodZoneRect, perfectZoneRect, rpmCursor);
        rpmGaugeContainer.getChildren().add(overlayPane);

        rpmTextLabel = new Label("RPM: 0%");
        rpmTextLabel.setStyle("-fx-text-fill: #e2e2e8; -fx-font-weight: bold; -fx-font-size: 14px;");

        gaugeWrapper.getChildren().addAll(rpmGaugeContainer, rpmTextLabel);
        root.getChildren().add(gaugeWrapper);
    }

    private void setupFeedbackArea() {
        VBox fbBox = new VBox(5);
        fbBox.setAlignment(Pos.CENTER);

        countdownLabel = new Label("GET READY!");
        countdownLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 28px; -fx-font-weight: bold;");

        feedbackLabel = new Label("");
        feedbackLabel.setStyle("-fx-text-fill: #00f3ff; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label instructions = new Label("PRESS SPACEBAR TO SHIFT GEAR");
        instructions.setStyle("-fx-text-fill: #8a8a99; -fx-font-size: 12px; -fx-font-weight: bold;");

        fbBox.getChildren().addAll(countdownLabel, feedbackLabel, instructions);
        root.getChildren().add(fbBox);
    }

    private void setupInputHandlers() {
        // Register key handler globally on the scene as an event filter
        // This ensures Spacebar is caught even if children components steal focus
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                        if (!countdownActive && !race.isFinished()) {
                            race.shiftPlayerGear();
                            event.consume(); // Prevent default button activation / spacebar behavior
                        }
                    }
                });
            }
        });

        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                if (!countdownActive && !race.isFinished()) {
                    race.shiftPlayerGear();
                    e.consume();
                }
            }
        });
        root.setFocusTraversable(true);
    }


    public void startRaceCountdown() {
        countdownActive = true;
        countdownLabel.setText("3...");
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> countdownLabel.setText("2...")),
            new KeyFrame(Duration.seconds(2), e -> countdownLabel.setText("1...")),
            new KeyFrame(Duration.seconds(3), e -> {
                countdownLabel.setText("GO!");
                countdownActive = false;
                lastTime = System.nanoTime();
                startGameLoop();
            }),
            new KeyFrame(Duration.seconds(4), e -> countdownLabel.setText(""))
        );
        timeline.play();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Cap deltaTime to avoid massive jumps on lag spikes
                if (deltaTime > 0.1) deltaTime = 0.1;

                // Update Race Simulation logic
                race.update(deltaTime);

                // Update UI Telemetry
                playerProgress.setProgress(race.getPlayerProgress());
                playerSpeedLabel.setText(String.format("%.0f km/h", race.getPlayerSpeed()));
                playerGearLabel.setText("GEAR " + race.getPlayerGear());

                opponentProgress.setProgress(race.getOpponentProgress());
                opponentSpeedLabel.setText(String.format("%.0f km/h", race.getOpponentSpeed()));
                opponentGearLabel.setText("GEAR " + race.getOpponentGear());

                // Update RPM Cursor position
                double rpm = race.getPlayerRpm();
                rpmCursor.setX(GAUGE_WIDTH * rpm);
                rpmTextLabel.setText(String.format("RPM: %.0f%%", rpm * 100));

                // Change cursor color based on redline
                if (rpm >= 0.95) {
                    rpmCursor.setFill(Color.web("#ff0055"));
                } else if (rpm >= race.getPerfectZoneStart() && rpm <= race.getPerfectZoneEnd()) {
                    rpmCursor.setFill(Color.web("#00ff66"));
                } else {
                    rpmCursor.setFill(Color.web("#00f3ff"));
                }

                // Update Feedback text
                feedbackLabel.setText(race.getPlayerFeedback());

                // Check finish
                if (race.isFinished()) {
                    stop();
                    handleRaceFinished();
                }
            }
        };
        gameLoop.start();
    }

    private void handleRaceFinished() {
        boolean playerWon = race.isPlayerWinner();
        
        // Process results in Progression and Economy services
        var rewards = app.getRaceService().processRaceResult(app.getPlayer(), opponent, playerWon, isBoss);
        
        if (playerWon) {
            app.getProgressionService().recordWin(isBoss);
        }

        // Wait 2.5 seconds to show results screen
        Timeline resultDelay = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> {
            app.showResults(playerWon, rewards.getMoney(), rewards.getReputation(), isBoss, opponent);
        }));
        resultDelay.play();
    }

    public VBox getView() {
        root.requestFocus(); // Proactively request focus
        return root;
    }
}
