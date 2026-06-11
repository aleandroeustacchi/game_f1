package it.unicam.cs.mpgc.rpg130017.ui;

import it.unicam.cs.mpgc.rpg130017.launcher.MainApp;
import it.unicam.cs.mpgc.rpg130017.model.*;
import it.unicam.cs.mpgc.rpg130017.service.ProgressionService;
import it.unicam.cs.mpgc.rpg130017.service.UpgradeService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class GarageView {

    private final BorderPane root;
    private final MainApp app;

    // Stats labels
    private Label speedLabel, accelLabel, gripLabel, weightLabel;
    private Label moneyLabel, repLabel, tierLabel, driverLabel;

    // Right panel content area (swaps between Upgrade / Shop / Equip / Race)
    private VBox rightContent;

    public GarageView(MainApp app) {
        this.app  = app;
        this.root = new BorderPane();
        root.getStyleClass().add("root");
        root.setPadding(new Insets(16));

        root.setTop(buildTopBar());
        root.setLeft(buildLeftPanel());
        root.setCenter(buildRightPanel());
        BorderPane.setMargin(root.getLeft(),   new Insets(0, 14, 0, 0));
        BorderPane.setMargin(root.getTop(),    new Insets(0, 0, 14, 0));

        showUpgradeTab();
    }

    // ── Top bar ────────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox(20);
        bar.getStyleClass().add("panel-dark");
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        driverLabel = new Label(); driverLabel.setStyle("-fx-text-fill:#00f3ff; -fx-font-weight:bold; -fx-font-size:15px;");
        tierLabel   = new Label(); tierLabel.setStyle("-fx-text-fill:#ffd700; -fx-font-weight:bold;");
        repLabel    = new Label(); repLabel.setStyle("-fx-text-fill:#e2e2e8;");
        moneyLabel  = new Label(); moneyLabel.setStyle("-fx-text-fill:#ffd700; -fx-font-weight:bold; -fx-font-size:15px;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button saveBtn = new Button("SAVE"); saveBtn.getStyleClass().add("btn-success");
        saveBtn.setOnAction(e -> {
            boolean ok = app.saveGame();
            new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                    ok ? "Game saved!" : "Save failed.").showAndWait();
        });

        Button menuBtn = new Button("MENU"); menuBtn.getStyleClass().add("btn-primary");
        menuBtn.setOnAction(e -> app.showMainMenu());

        bar.getChildren().addAll(driverLabel, tierLabel, repLabel, moneyLabel, sp, saveBtn, menuBtn);
        refreshTopBar();
        return bar;
    }

    private void refreshTopBar() {
        Player p = app.getPlayer();
        driverLabel.setText("DRIVER: " + p.getName().toUpperCase());
        tierLabel.setText("TIER " + app.getProgressionService().getCurrentTier());
        repLabel.setText("REP: " + p.getReputation());
        moneyLabel.setText("$" + p.getMoney());
    }

    // ── Left: car stats + nav buttons ─────────────────────────────────────
    private VBox buildLeftPanel() {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("panel-dark");
        panel.setPrefWidth(230);
        panel.setPadding(new Insets(16));

        Label title = new Label("YOUR CAR");
        title.getStyleClass().add("section-header");

        VBox stats = new VBox(8);
        stats.setStyle("-fx-background-color:rgba(0,0,0,0.2); -fx-padding:10; -fx-background-radius:6;");

        speedLabel  = statLabel(); accelLabel = statLabel();
        gripLabel   = statLabel(); weightLabel = statLabel();
        stats.getChildren().addAll(speedLabel, accelLabel, gripLabel, weightLabel);

        Separator sep = new Separator();

        Label navTitle = new Label("GARAGE MENU");
        navTitle.setStyle("-fx-text-fill:#8a8a99; -fx-font-size:11px; -fx-font-weight:bold;");

        Button upgradeBtn   = navBtn("⚙  UPGRADE",   "#00f3ff");
        Button shopBtn      = navBtn("🛒  SHOP",      "#ffd700");
        Button equipBtn     = navBtn("🔧  EQUIP",     "#00ff66");
        Button raceBtn      = navBtn("🏁  RACE",      "#ff0055");
        Button tournamentBtn= navBtn("🏆  TOURNAMENT","#ff9900");

        upgradeBtn.setOnAction(e -> showUpgradeTab());
        shopBtn.setOnAction(e    -> showShopTab());
        equipBtn.setOnAction(e   -> showEquipTab());
        raceBtn.setOnAction(e    -> showRaceTab());
        tournamentBtn.setOnAction(e -> showTournamentTab());

        panel.getChildren().addAll(title, stats, sep, navTitle,
                upgradeBtn, shopBtn, equipBtn, raceBtn, tournamentBtn);

        refreshStats();
        return panel;
    }

    private Label statLabel() {
        Label l = new Label(); l.getStyleClass().add("label-stat"); return l;
    }

    private Button navBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#0d0d12; -fx-text-fill:" + color +
                "; -fx-border-color:" + color + "; -fx-border-width:1.5px; " +
                "-fx-border-radius:6; -fx-background-radius:6; -fx-font-weight:bold; -fx-cursor:hand;");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private void refreshStats() {
        Car car = app.getPlayer().getCar();
        speedLabel.setText(String.format("TOP SPEED  %.0f km/h", car.getTopSpeed()));
        accelLabel.setText(String.format("ACCEL      %.1f m/s²", car.getAcceleration()));
        gripLabel.setText (String.format("GRIP       %.0f",       car.getGrip()));
        weightLabel.setText(String.format("WEIGHT     %.0f kg",  car.getWeight()));
    }

    // ── Right content area ─────────────────────────────────────────────────
    private ScrollPane buildRightPanel() {
        rightContent = new VBox(14);
        rightContent.setPadding(new Insets(4));
        ScrollPane sp = new ScrollPane(rightContent);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
        return sp;
    }

    // ══ UPGRADE TAB ═══════════════════════════════════════════════════════
    private void showUpgradeTab() {
        rightContent.setUserData("upgrade");
        rightContent.getChildren().clear();
        Player p = app.getPlayer();
        UpgradeService svc = app.getUpgradeService();
        Car car = p.getCar();

        Label title = new Label("UPGRADE EQUIPPED PARTS");
        title.getStyleClass().add("section-header");
        rightContent.getChildren().add(title);

        // Engine
        int eLvl  = car.getEngineLevel(); int eCost = svc.upgradeCost(eLvl);
        rightContent.getChildren().add(upgradeRow(
                car.getEquippedEngine().getDisplayName() + "  Lvl " + eLvl,
                "Upgrade  $" + eCost,
                () -> { svc.upgradeEngine(p); refresh(); }));

        // Tires
        int tLvl  = car.getTiresLevel();  int tCost = svc.upgradeCost(tLvl);
        rightContent.getChildren().add(upgradeRow(
                car.getEquippedTires().getDisplayName() + "  Lvl " + tLvl,
                "Upgrade  $" + tCost,
                () -> { svc.upgradeTires(p); refresh(); }));

        // Nitro
        int nLvl  = car.getNitroLevel();  int nCost = svc.upgradeCost(nLvl);
        rightContent.getChildren().add(upgradeRow(
                car.getEquippedNitro().getDisplayName() + "  Lvl " + nLvl,
                "Upgrade  $" + nCost,
                () -> { svc.upgradeNitro(p); refresh(); }));

        // Frame
        int fLvl  = car.getWeightReductionLevel(); int fCost = svc.upgradeCost(fLvl);
        rightContent.getChildren().add(upgradeRow(
                car.getEquippedFrame().getDisplayName() + "  Lvl " + fLvl,
                "Upgrade  $" + fCost,
                () -> { svc.upgradeFrame(p); refresh(); }));
    }

    private HBox upgradeRow(String partName, String btnLabel, Runnable action) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:rgba(255,255,255,0.03); -fx-padding:10; -fx-background-radius:6;");

        Label lbl = new Label(partName);
        lbl.setStyle("-fx-text-fill:#e2e2e8; -fx-font-weight:bold; -fx-font-size:13px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btn = new Button(btnLabel); btn.getStyleClass().add("btn-primary");
        btn.setOnAction(e -> action.run());
        row.getChildren().addAll(lbl, sp, btn);
        return row;
    }

    // ══ SHOP TAB ═══════════════════════════════════════════════════════════
    private void showShopTab() {
        rightContent.setUserData("shop");
        rightContent.getChildren().clear();
        Player p = app.getPlayer();
        UpgradeService svc = app.getUpgradeService();

        Label title = new Label("PARTS SHOP  —  Buy new component tiers");
        title.getStyleClass().add("section-header"); title.setStyle("-fx-text-fill:#ffd700;");
        rightContent.getChildren().add(title);

        // Engines
        rightContent.getChildren().add(sectionHeader("ENGINES"));
        for (EngineType t : EngineType.values()) {
            if (t == EngineType.V6) continue;
            boolean owned    = p.getCar().ownsEngine(t);
            boolean canBuy   = !owned && p.getCar().getEngineLevel() >= t.getUnlockLevel();
            boolean equipped = p.getCar().getEquippedEngine() == t;
            String info = t.getDisplayName() + " — " + t.getDescription()
                    + (owned ? " [OWNED]" : "  $" + t.getPurchaseCost())
                    + "  unlock: Engine Lvl " + t.getUnlockLevel();
            rightContent.getChildren().add(shopRow(info, "BUY  $" + t.getPurchaseCost(),
                    owned, canBuy, () -> { svc.buyEngineType(p, t); refresh(); }));
        }

        // Tires
        rightContent.getChildren().add(sectionHeader("TIRES"));
        for (TireType t : TireType.values()) {
            if (t == TireType.STREET) continue;
            boolean owned  = p.getCar().ownsTires(t);
            boolean canBuy = !owned && p.getCar().getTiresLevel() >= t.getUnlockLevel();
            String info = t.getDisplayName() + " — " + t.getDescription()
                    + (owned ? " [OWNED]" : "  $" + t.getPurchaseCost())
                    + "  unlock: Tires Lvl " + t.getUnlockLevel();
            rightContent.getChildren().add(shopRow(info, "BUY  $" + t.getPurchaseCost(),
                    owned, canBuy, () -> { svc.buyTireType(p, t); refresh(); }));
        }

        // Nitro
        rightContent.getChildren().add(sectionHeader("NITRO"));
        for (NitroType t : NitroType.values()) {
            if (t == NitroType.BASIC) continue;
            boolean owned  = p.getCar().ownsNitro(t);
            boolean canBuy = !owned && p.getCar().getNitroLevel() >= t.getUnlockLevel();
            String info = t.getDisplayName() + " — " + t.getDescription()
                    + (owned ? " [OWNED]" : "  $" + t.getPurchaseCost())
                    + "  unlock: Nitro Lvl " + t.getUnlockLevel();
            rightContent.getChildren().add(shopRow(info, "BUY  $" + t.getPurchaseCost(),
                    owned, canBuy, () -> { svc.buyNitroType(p, t); refresh(); }));
        }

        // Frame
        rightContent.getChildren().add(sectionHeader("FRAME / WEIGHT"));
        for (FrameType t : FrameType.values()) {
            if (t == FrameType.STOCK) continue;
            boolean owned  = p.getCar().ownsFrame(t);
            boolean canBuy = !owned && p.getCar().getWeightReductionLevel() >= t.getUnlockLevel();
            String info = t.getDisplayName() + " — " + t.getDescription()
                    + (owned ? " [OWNED]" : "  $" + t.getPurchaseCost())
                    + "  unlock: Frame Lvl " + t.getUnlockLevel();
            rightContent.getChildren().add(shopRow(info, "BUY  $" + t.getPurchaseCost(),
                    owned, canBuy, () -> { svc.buyFrameType(p, t); refresh(); }));
        }
    }

    private Label sectionHeader(String text) {
        Label l = new Label("── " + text + " ──");
        l.setStyle("-fx-text-fill:#8a8a99; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:8 0 2 0;");
        return l;
    }

    private HBox shopRow(String info, String btnLabel, boolean owned, boolean canBuy, Runnable action) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:rgba(255,255,255,0.03); -fx-padding:10; -fx-background-radius:6;");

        Label lbl = new Label(info);
        lbl.setWrapText(true); lbl.setMaxWidth(600);
        lbl.setStyle("-fx-text-fill:" + (owned ? "#00ff66" : "#e2e2e8") + "; -fx-font-size:12px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btn = new Button(owned ? "OWNED ✓" : btnLabel);
        btn.getStyleClass().add(owned ? "btn-success" : "btn-primary");
        btn.setDisable(owned || !canBuy);
        btn.setOnAction(e -> action.run());
        row.getChildren().addAll(lbl, sp, btn);
        return row;
    }

    // ══ EQUIP TAB ══════════════════════════════════════════════════════════
    private void showEquipTab() {
        rightContent.setUserData("equip");
        rightContent.getChildren().clear();
        Player p = app.getPlayer();
        UpgradeService svc = app.getUpgradeService();
        Car car = p.getCar();

        Label title = new Label("EQUIP OWNED PARTS");
        title.getStyleClass().add("section-header"); title.setStyle("-fx-text-fill:#00ff66;");
        rightContent.getChildren().add(title);

        rightContent.getChildren().add(sectionHeader("ENGINES"));
        for (EngineType t : EngineType.values()) {
            if (!car.ownsEngine(t)) continue;
            boolean eq = car.getEquippedEngine() == t;
            int lvl = car.getEngineLevel(t);
            rightContent.getChildren().add(equipRow(
                    t.getDisplayName() + "  Lvl " + lvl + "  " + t.getDescription(),
                    eq, () -> { svc.equipEngine(p, t); refresh(); }));
        }

        rightContent.getChildren().add(sectionHeader("TIRES"));
        for (TireType t : TireType.values()) {
            if (!car.ownsTires(t)) continue;
            boolean eq = car.getEquippedTires() == t;
            int lvl = car.getTireLevel(t);
            rightContent.getChildren().add(equipRow(
                    t.getDisplayName() + "  Lvl " + lvl + "  " + t.getDescription(),
                    eq, () -> { svc.equipTires(p, t); refresh(); }));
        }

        rightContent.getChildren().add(sectionHeader("NITRO"));
        for (NitroType t : NitroType.values()) {
            if (!car.ownsNitro(t)) continue;
            boolean eq = car.getEquippedNitro() == t;
            int lvl = car.getNitroLevel(t);
            rightContent.getChildren().add(equipRow(
                    t.getDisplayName() + "  Lvl " + lvl + "  " + t.getDescription(),
                    eq, () -> { svc.equipNitro(p, t); refresh(); }));
        }

        rightContent.getChildren().add(sectionHeader("FRAME"));
        for (FrameType t : FrameType.values()) {
            if (!car.ownsFrame(t)) continue;
            boolean eq = car.getEquippedFrame() == t;
            int lvl = car.getFrameLevel(t);
            rightContent.getChildren().add(equipRow(
                    t.getDisplayName() + "  Lvl " + lvl + "  " + t.getDescription(),
                    eq, () -> { svc.equipFrame(p, t); refresh(); }));
        }
    }

    private HBox equipRow(String info, boolean equipped, Runnable action) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:" + (equipped ? "rgba(0,255,102,0.06)" : "rgba(255,255,255,0.03)")
                + "; -fx-padding:10; -fx-background-radius:6; -fx-border-color:"
                + (equipped ? "#00ff66" : "transparent") + "; -fx-border-width:1.2; -fx-border-radius:6;");

        Label lbl = new Label(info);
        lbl.setStyle("-fx-text-fill:" + (equipped ? "#00ff66" : "#e2e2e8") + "; -fx-font-size:12px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btn = new Button(equipped ? "EQUIPPED ✓" : "EQUIP");
        btn.getStyleClass().add(equipped ? "btn-success" : "btn-primary");
        btn.setDisable(equipped);
        btn.setOnAction(e -> action.run());
        row.getChildren().addAll(lbl, sp, btn);
        return row;
    }

    // ══ RACE TAB ═══════════════════════════════════════════════════════════
    private void showRaceTab() {
        rightContent.setUserData("race");
        rightContent.getChildren().clear();
        ProgressionService prog = app.getProgressionService();

        Label title = new Label("STREET RACES  —  1v1");
        title.getStyleClass().add("section-header"); title.setStyle("-fx-text-fill:#ff0055;");
        rightContent.getChildren().add(title);

        Label status = new Label("Wins this tier: " + prog.getWinsInCurrentTier() + " / 3  →  unlock Boss");
        status.setStyle("-fx-text-fill:#8a8a99; -fx-font-size:12px;");
        rightContent.getChildren().add(status);

        if (prog.isBossDefeated()) {
            // Tier clear
            Label ok = new Label("✓ Boss Defeated! You can advance to the next tier.");
            ok.setStyle("-fx-text-fill:#00ff66; -fx-font-size:14px; -fx-font-weight:bold;");
            Button next = new Button("ADVANCE TO TIER " + (prog.getCurrentTier() + 1));
            next.getStyleClass().add("btn-success");
            next.setDisable(prog.getCurrentTier() >= 3);
            if (prog.getCurrentTier() >= 3) next.setText("ALL TIERS CONQUERED! 🏆");
            next.setOnAction(e -> { prog.promoteToNextTier(); refresh(); });
            rightContent.getChildren().addAll(ok, next);
            return;
        }

        if (prog.isBossUnlocked()) {
            BossRacer boss = prog.getCurrentBoss();
            VBox bossBox = new VBox(12);
            bossBox.setStyle("-fx-background-color:rgba(255,0,85,0.08); -fx-border-color:#ff0055; " +
                    "-fx-border-width:1.5; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:16;");
            bossBox.setAlignment(Pos.CENTER);
            Label bLbl = new Label("BOSS: " + boss.getName().toUpperCase());
            bLbl.setStyle("-fx-text-fill:#ff0055; -fx-font-size:20px; -fx-font-weight:bold;");
            Label rew = new Label("Reward: $" + boss.getRewardMoney() + "  |  " + boss.getRewardUnlocks());
            rew.setStyle("-fx-text-fill:#ffd700; -fx-font-weight:bold;");
            Button bBtn = new Button("CHALLENGE " + boss.getName().toUpperCase());
            bBtn.getStyleClass().add("btn-danger"); bBtn.setPrefWidth(280);
            bBtn.setOnAction(e -> app.showRace(boss, true));
            bossBox.getChildren().addAll(bLbl, rew, bBtn);
            rightContent.getChildren().add(bossBox);
            return;
        }

        for (Racer r : prog.getAvailableOpponents()) {
            HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color:rgba(255,255,255,0.03); -fx-padding:12; -fx-background-radius:6;");
            VBox info = new VBox(3);
            Label nm = new Label(r.getName().toUpperCase());
            nm.setStyle("-fx-text-fill:#00f3ff; -fx-font-weight:bold;");
            Label stats = new Label(String.format("Top Speed: %.0f km/h  |  Accel: %.1f m/s²",
                    r.getCar().getTopSpeed(), r.getCar().getAcceleration()));
            stats.setStyle("-fx-text-fill:#8a8a99; -fx-font-size:11px;");
            info.getChildren().addAll(nm, stats);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Button rb = new Button("RACE  +$500");
            rb.getStyleClass().add("btn-danger");
            rb.setOnAction(e -> app.showRace(r, false));
            row.getChildren().addAll(info, sp, rb);
            rightContent.getChildren().add(row);
        }
    }

    // ══ TOURNAMENT TAB ═════════════════════════════════════════════════════
    private void showTournamentTab() {
        rightContent.setUserData("tour");
        rightContent.getChildren().clear();

        Label title = new Label("TOURNAMENT MODE  —  1 vs 5 rivals");
        title.getStyleClass().add("section-header"); title.setStyle("-fx-text-fill:#ff9900;");
        rightContent.getChildren().add(title);

        Label desc = new Label(
                "Race against 5 opponents simultaneously.\n" +
                "Live leaderboard tracks all positions.\n\n" +
                "Rewards by finishing position:\n" +
                "  🥇  1st  →  $3,000  +  150 REP\n" +
                "  🥈  2nd  →  $1,500  +   75 REP\n" +
                "  🥉  3rd  →    $800  +   40 REP\n" +
                "      4th+ →    $200  +   10 REP");
        desc.setStyle("-fx-text-fill:#e2e2e8; -fx-font-size:13px; -fx-line-spacing:4;");
        desc.setWrapText(true);

        VBox card = new VBox(14, desc);
        card.getStyleClass().add("panel-glowing");
        card.setPadding(new Insets(20));
        card.setMaxWidth(600);

        Button startBtn = new Button("START TOURNAMENT 🏆");
        startBtn.getStyleClass().add("btn-success");
        startBtn.setStyle("-fx-font-size:15px; -fx-padding:14 30 14 30;");
        startBtn.setOnAction(e -> app.showTournamentRace());

        card.getChildren().add(startBtn);
        rightContent.getChildren().add(card);
    }

    // ── Global refresh ─────────────────────────────────────────────────────
    private void refresh() {
        refreshTopBar();
        refreshStats();
        // Re-render whichever tab is open
        if (!rightContent.getChildren().isEmpty()) {
            Object tag = rightContent.getUserData();
            if ("shop".equals(tag))    showShopTab();
            else if ("equip".equals(tag)) showEquipTab();
            else if ("race".equals(tag))  showRaceTab();
            else if ("tour".equals(tag))  showTournamentTab();
            else                          showUpgradeTab();
        }
    }


    public BorderPane getView() { return root; }
}
