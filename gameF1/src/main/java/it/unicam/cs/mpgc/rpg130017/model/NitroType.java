package it.unicam.cs.mpgc.rpg130017.model;

public enum NitroType {
    BASIC("Basic NOS",       0,   0,    0,    0,  "Stock nitrous system"),
    STAGE2("Stage 2 NOS",   10,   3.5,  3, 2000,  "Higher pressure, longer burst"),
    NOS("Full Race NOS",    28,   8.0,  6, 6500,  "Maximum flow racing system");

    private final String displayName;
    private final double topSpeedBonus;
    private final double accelBonus;
    private final int    unlockLevel;
    private final int    purchaseCost;
    private final String description;

    NitroType(String displayName, double topSpeedBonus, double accelBonus,
              int unlockLevel, int purchaseCost, String description) {
        this.displayName   = displayName;
        this.topSpeedBonus = topSpeedBonus;
        this.accelBonus    = accelBonus;
        this.unlockLevel   = unlockLevel;
        this.purchaseCost  = purchaseCost;
        this.description   = description;
    }

    public String getDisplayName()  { return displayName; }
    public double getTopSpeedBonus(){ return topSpeedBonus; }
    public double getAccelBonus()   { return accelBonus; }
    public int    getUnlockLevel()  { return unlockLevel; }
    public int    getPurchaseCost() { return purchaseCost; }
    public String getDescription()  { return description; }
}
