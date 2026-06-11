package it.unicam.cs.mpgc.rpg130017.model;

public enum FrameType {
    STOCK("Stock Frame",          0,   0,    0,    0,  "Factory chassis"),
    LIGHTWEIGHT("Lightweight",  -180,  4.0,  3, 2200,  "Stripped interior, lighter panels"),
    CARBON("Carbon Fiber",      -380,  9.5,  6, 7000,  "Full carbon body – extreme weight cut");

    private final String displayName;
    private final double weightReduction;   // negative = less weight (kg)
    private final double accelBonus;
    private final int    unlockLevel;
    private final int    purchaseCost;
    private final String description;

    FrameType(String displayName, double weightReduction, double accelBonus,
              int unlockLevel, int purchaseCost, String description) {
        this.displayName    = displayName;
        this.weightReduction = weightReduction;
        this.accelBonus     = accelBonus;
        this.unlockLevel    = unlockLevel;
        this.purchaseCost   = purchaseCost;
        this.description    = description;
    }

    public String getDisplayName()    { return displayName; }
    public double getWeightReduction(){ return weightReduction; }
    public double getAccelBonus()     { return accelBonus; }
    public int    getUnlockLevel()    { return unlockLevel; }
    public int    getPurchaseCost()   { return purchaseCost; }
    public String getDescription()    { return description; }
}
