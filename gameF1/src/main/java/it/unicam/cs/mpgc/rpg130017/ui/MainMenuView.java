package it.unicam.cs.mpgc.rpg130017.ui;

import it.unicam.cs.mpgc.rpg130017.launcher.MainApp;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;

public class MainMenuView {
    private final VBox root;

    public MainMenuView(MainApp app) {
        root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Text mainTitle = new Text("UNDERGROUND");
        mainTitle.getStyleClass().add("title-text");

        Text subtitle = new Text("STREET RACING RPG");
        subtitle.setStyle("-fx-fill: #e2e2e8; -fx-font-size: 18px; -fx-font-weight: bold; -fx-letter-spacing: 2px;");

        VBox titleBox = new VBox(5, mainTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        VBox newGameBox = new VBox(10);
        newGameBox.setAlignment(Pos.CENTER);
        newGameBox.setMaxWidth(300);
        newGameBox.getStyleClass().add("panel-dark");

        Label nameLabel = new Label("ENTER YOUR DRIVER NAME:");
        nameLabel.getStyleClass().add("label-normal");
        
        TextField nameField = new TextField("RacerX");
        nameField.getStyleClass().add("text-field-dark");

        Button newGameBtn = new Button("START NEW CAREER");
        newGameBtn.getStyleClass().add("btn-danger");
        newGameBtn.setPrefWidth(260);
        newGameBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Name");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid driver name.");
                alert.showAndWait();
            } else {
                app.createNewGame(name);
            }
        });

        newGameBox.getChildren().addAll(nameLabel, nameField, newGameBtn);

        Button loadGameBtn = new Button("LOAD CAREER");
        loadGameBtn.getStyleClass().add("btn-primary");
        loadGameBtn.setPrefWidth(300);
        
        File saveFile = new File(MainApp.SAVE_FILE);
        if (!saveFile.exists()) {
            loadGameBtn.setDisable(true);
            loadGameBtn.setText("LOAD CAREER (NO SAVE FOUND)");
        }

        loadGameBtn.setOnAction(e -> {
            boolean loaded = app.loadGame();
            if (!loaded) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Load Failed");
                alert.setHeaderText(null);
                alert.setContentText("Could not load save game data.");
                alert.showAndWait();
            }
        });

        Button exitBtn = new Button("EXIT GAME");
        exitBtn.getStyleClass().add("btn-primary");
        exitBtn.setPrefWidth(300);
        exitBtn.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(titleBox, newGameBox, loadGameBtn, exitBtn);
    }

    public VBox getView() {
        return root;
    }
}
