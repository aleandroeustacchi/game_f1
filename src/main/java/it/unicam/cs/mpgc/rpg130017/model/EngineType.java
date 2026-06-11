package it.unicam.cs.mpgc.rpg130017.model;

/**
 * Engine tiers. Each tier has fixed bonuses on top of which per-level upgrades
 * apply.
 * Unlock requires reaching the specified engineLevel on the current engine.
 */
public enum EngineType {
    V6("V6 Naturally Aspirated", 0, 0, 0, 0, "Base engine – starter kit"),
    V8("V8 Performance", 20, 3.5, 3, 3000, "More displacement, more power"),
    V12("V12 Supercharged", 55, 9.0, 6, 9000, "Top-tier powerplant for champions");

    private final String displayName;
    private final double topSpeedBonus; // added to baseTopSpeed
    private final double accelBonus; // added to baseAcceleration
    private final int unlockLevel; // engine upgrade level needed to purchase
    private final int purchaseCost; // money required to buy
    private final String description;

    EngineType(String displayName, double topSpeedBonus, double accelBonus,
            int unlockLevel, int purchaseCost, String description) {
        this.displayName = displayName;
        this.topSpeedBonus = topSpeedBonus;
        this.accelBonus = accelBonus;
        this.unlockLevel = unlockLevel;
        this.purchaseCost = purchaseCost;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getTopSpeedBonus() {
        return topSpeedBonus;
    }

    public double getAccelBonus() {
        return accelBonus;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public int getPurchaseCost() {
        return purchaseCost;
    }

    public String getDescription() {
        return description;
    }
}
