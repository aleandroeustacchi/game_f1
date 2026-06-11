package it.unicam.cs.mpgc.rpg130017.launcher;

import it.unicam.cs.mpgc.rpg130017.model.*;
import it.unicam.cs.mpgc.rpg130017.persistence.JsonSaveRepository;
import it.unicam.cs.mpgc.rpg130017.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg130017.service.*;
import it.unicam.cs.mpgc.rpg130017.ui.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainApp extends Application {

    private Stage primaryStage;
    private Player player;

    public static final String SAVE_FILE = "saves/savegame.json";

    private final ProgressionService progressionService = new ProgressionService();
    private final UpgradeService     upgradeService     = new UpgradeService();
    private final RaceService        raceService        = new RaceService();
    private final SaveRepository     saveRepository     = new JsonSaveRepository();
    private final SaveGameService    saveGameService    = new SaveGameService(saveRepository);

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Underground Street Racing RPG");
        showMainMenu();
        stage.show();
    }

    // ── New / Load ─────────────────────────────────────────────────────────
    public void createNewGame(String name) {
        this.player = new Player(name);
        progressionService.setCurrentTier(1);
        progressionService.setWinsInCurrentTier(0);
        progressionService.setBossDefeated(false);
        showGarage();
    }

    public boolean loadGame() {
        try {
            File f = new File(SAVE_FILE);
            if (!f.exists()) return false;
            GameState gs = saveGameService.loadGame(SAVE_FILE);
            this.player  = gs.toPlayer();
            progressionService.setCurrentTier(gs.getTier());
            progressionService.setWinsInCurrentTier(gs.getWinsInCurrentTier());
            progressionService.setBossDefeated(gs.isBossDefeated());
            showGarage();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean saveGame() {
        try {
            saveGameService.saveGame(player,
                    progressionService.getCurrentTier(),
                    progressionService.getWinsInCurrentTier(),
                    progressionService.isBossDefeated(), SAVE_FILE);
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Scene routing ──────────────────────────────────────────────────────
    public void showMainMenu() {
        MainMenuView v = new MainMenuView(this);
        setScene(v.getView(), 800, 600);
    }

    public void showGarage() {
        GarageView v = new GarageView(this);
        setScene(v.getView(), 1100, 720);
    }

    public void showRace(Racer opponent, boolean isBoss) {
        RaceView v = new RaceView(this, opponent, isBoss);
        setScene(v.getView(), 1050, 720);
        v.startRaceCountdown();
    }

    public void showTournamentRace() {
        List<Racer> opps = progressionService.getTournamentOpponents();
        TournamentRaceMode race = raceService.createTournamentRace(player, opps);
        TournamentRaceView v = new TournamentRaceView(this, race);
        setScene(v.getView(), 1150, 740);
        v.startRaceCountdown();
    }

    public void showResults(boolean isWinner, int money, int rep, boolean isBoss, Racer opponent) {
        ResultsView v = new ResultsView(this, isWinner, money, rep, isBoss, opponent);
        setScene(v.getView(), 820, 620);
    }

    public void showTournamentResults(int position, int money, int rep) {
        ResultsView v = ResultsView.forTournament(this, position, money, rep);
        setScene(v.getView(), 820, 620);
    }

    private void setScene(javafx.scene.Parent root, double w, double h) {
        Scene scene = new Scene(root, w, h);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        primaryStage.setScene(scene);
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public Player            getPlayer()            { return player; }
    public ProgressionService getProgressionService(){ return progressionService; }
    public UpgradeService    getUpgradeService()    { return upgradeService; }
    public RaceService       getRaceService()       { return raceService; }

    public static void main(String[] args) { launch(args); }
}
