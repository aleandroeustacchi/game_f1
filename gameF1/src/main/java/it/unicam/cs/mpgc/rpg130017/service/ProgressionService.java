package it.unicam.cs.mpgc.rpg130017.service;

import it.unicam.cs.mpgc.rpg130017.model.*;
import java.util.ArrayList;
import java.util.List;

public class ProgressionService {

    private int     currentTier        = 1;
    private int     winsInCurrentTier  = 0;
    private boolean bossDefeated       = false;

    public int  getCurrentTier()           { return currentTier; }
    public void setCurrentTier(int v)      { this.currentTier = v; }
    public int  getWinsInCurrentTier()     { return winsInCurrentTier; }
    public void setWinsInCurrentTier(int v){ this.winsInCurrentTier = v; }
    public boolean isBossDefeated()        { return bossDefeated; }
    public void setBossDefeated(boolean v) { this.bossDefeated = v; }
    public boolean isBossUnlocked()        { return winsInCurrentTier >= 3; }

    public void recordWin(boolean isBoss) {
        if (isBoss) bossDefeated = true;
        else if (winsInCurrentTier < 3) winsInCurrentTier++;
    }

    public boolean promoteToNextTier() {
        if (bossDefeated && currentTier < 3) {
            currentTier++;  winsInCurrentTier = 0;  bossDefeated = false;
            return true;
        }
        return false;
    }

    // ── Regular opponents for 1v1 ─────────────────────────────────────────
    public List<Racer> getAvailableOpponents() {
        return switch (currentTier) {
            case 1 -> List.of(
                    new Racer("Slick Rick",       new Car(160, 11, 50, 1450)),
                    new Racer("Speedy Gonzales",  new Car(165, 12, 55, 1400)),
                    new Racer("Turbo Tim",        new Car(170, 13, 60, 1350)));
            case 2 -> List.of(
                    new Racer("Asphalt Assassin", new Car(185, 16, 70, 1300)),
                    new Racer("Drift King",       new Car(190, 17, 75, 1280)),
                    new Racer("Shift Shifter",    new Car(195, 18, 80, 1250)));
            default -> List.of(
                    new Racer("Road Rebel",       new Car(215, 22, 90, 1200)),
                    new Racer("Nitrous Ned",      new Car(220, 23, 95, 1180)),
                    new Racer("Shadow Racer",     new Car(225, 24, 100, 1150)));
        };
    }

    // ── Tournament opponents (5 AI cars) ─────────────────────────────────
    public List<Racer> getTournamentOpponents() {
        List<Racer> all = new ArrayList<>(getAvailableOpponents());
        // Fill up to 5 with mixed-tier extras
        return switch (currentTier) {
            case 1 -> List.of(
                    new Racer("Slick Rick",       new Car(160, 11, 50, 1450)),
                    new Racer("Speedy Gonzales",  new Car(165, 12, 55, 1400)),
                    new Racer("Turbo Tim",        new Car(170, 13, 60, 1350)),
                    new Racer("Flash Carlos",     new Car(163, 11, 52, 1420)),
                    new Racer("Dirt Tracker",     new Car(168, 12, 58, 1380)));
            case 2 -> List.of(
                    new Racer("Asphalt Assassin", new Car(185, 16, 70, 1300)),
                    new Racer("Drift King",       new Car(190, 17, 75, 1280)),
                    new Racer("Shift Shifter",    new Car(195, 18, 80, 1250)),
                    new Racer("Nitro Blaze",      new Car(188, 16, 72, 1290)),
                    new Racer("Redline Raja",     new Car(192, 17, 77, 1260)));
            default -> List.of(
                    new Racer("Road Rebel",       new Car(215, 22, 90, 1200)),
                    new Racer("Nitrous Ned",      new Car(220, 23, 95, 1180)),
                    new Racer("Shadow Racer",     new Car(225, 24, 100, 1150)),
                    new Racer("Ghost Circuit",    new Car(218, 23, 92, 1190)),
                    new Racer("Zenith Jr",        new Car(222, 24, 98, 1160)));
        };
    }

    // ── Boss ──────────────────────────────────────────────────────────────
    public BossRacer getCurrentBoss() {
        return switch (currentTier) {
            case 1  -> new BossRacer("Apex",  new Car(185, 15, 70, 1250), 1, 2000, "Nitro Stage 2 Unlocked");
            case 2  -> new BossRacer("Ghost", new Car(215, 21, 90, 1150), 2, 4000, "V8 Engine Unlocked");
            default -> new BossRacer("Zenith",new Car(250, 28, 115,1000), 3,10000, "V12 & Full Carbon Unlocked");
        };
    }
}
