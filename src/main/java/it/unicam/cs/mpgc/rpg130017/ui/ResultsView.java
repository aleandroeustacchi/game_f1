package it.unicam.cs.mpgc.rpg130017.ui;

import it.unicam.cs.mpgc.rpg130017.launcher.MainApp;
import it.unicam.cs.mpgc.rpg130017.model.Racer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ResultsView {

    private final VBox root;

    /** Standard 1v1 result constructor */
    public ResultsView(MainApp app, boolean isWinner, int money, int rep,
                       boolean isBoss, Racer opponent) {
        root = new VBox(22);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Label outcome = new Label(isWinner ? "VICTORY" : "DEFEAT");
        outcome.setStyle("-fx-font-size:52px; -fx-font-weight:bold; -fx-effect:" +
                "dropshadow(three-pass-box," +
                (isWinner ? "rgba(0,255,102,0.45)" : "rgba(255,0,85,0.45)") +
                ",10,0,0,0); -fx-text-fill:" +
                (isWinner ? "#00ff66" : "#ff0055") + ";");

        Label sub = new Label((isWinner ? "YOU BEAT " : "YOU LOST TO ") +
                opponent.getName().toUpperCase());
        sub.setStyle("-fx-text-fill:#e2e2e8; -fx-font-size:17px;");

        VBox rewards = rewardsPanel(money, rep);

        if (isBoss && isWinner) {
            Label bossMsg = new Label("TIER BOSS DEFEATED! 🏆");
            bossMsg.setStyle("-fx-text-fill:#ffd700; -fx-font-size:15px; -fx-font-weight:bold;");
            rewards.getChildren().add(bossMsg);
        }

        Button back = new Button("RETURN TO GARAGE");
        back.getStyleClass().add("btn-success");
        back.setPrefWidth(240);
        back.setOnAction(e -> app.showGarage());

        root.getChildren().addAll(outcome, sub, rewards, back);
    }

    /** Tournament result factory method */
    public static ResultsView forTournament(MainApp app, int position, int money, int rep) {
        ResultsView rv = new ResultsView();

        String medal = switch (position) {
            case 1 -> "🥇  1st PLACE  —  VICTORY!";
            case 2 -> "🥈  2nd PLACE";
            case 3 -> "🥉  3rd PLACE";
            default -> "P" + position + "  —  Better luck next time";
        };

        Label outcome = new Label(medal);
        outcome.setStyle("-fx-font-size:" + (position == 1 ? "40" : "32") +
                "px; -fx-font-weight:bold; -fx-text-fill:" +
                (position == 1 ? "#ffd700" : position <= 3 ? "#00f3ff" : "#e2e2e8") + ";");

        Label sub = new Label("TOURNAMENT FINISHED  —  " + app.getPlayer().getName().toUpperCase());
        sub.setStyle("-fx-text-fill:#8a8a99; -fx-font-size:14px;");

        VBox rewards = rewardsPanel(money, rep);

        Button back = new Button("RETURN TO GARAGE");
        back.getStyleClass().add(position == 1 ? "btn-success" : "btn-primary");
        back.setPrefWidth(240);
        back.setOnAction(e -> app.showGarage());

        rv.root.getChildren().addAll(outcome, sub, rewards, back);
        return rv;
    }

    private ResultsView() {
        root = new VBox(22);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");
    }

    private static VBox rewardsPanel(int money, int rep) {
        VBox panel = new VBox(12);
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("panel-dark");
        panel.setPrefWidth(380); panel.setMaxWidth(380);
        panel.setStyle("-fx-padding:20;");

        Label header = new Label("RACE REWARDS");
        header.getStyleClass().add("section-header");

        Label moneyLbl = new Label("CASH:  +$" + money);
        moneyLbl.setStyle("-fx-text-fill:#ffd700; -fx-font-size:18px; -fx-font-weight:bold;");

        Label repLbl = new Label("REPUTATION:  +" + rep + " REP");
        repLbl.setStyle("-fx-text-fill:#00f3ff; -fx-font-size:18px; -fx-font-weight:bold;");

        panel.getChildren().addAll(header, moneyLbl, repLbl);
        return panel;
    }

    public VBox getView() { return root; }
}
