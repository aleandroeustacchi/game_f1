package it.unicam.cs.mpgc.rpg130017.model;

public enum TireType {
    STREET("Street Tires",    0,   0,    0,    0,  "Standard road tires"),
    SPORT("Sport Tires",     12,   4.0,  3, 2500,  "Improved compound for better grip"),
    SLICK("Slick Racing",    30,  10.0,  6, 7500,  "Full racing slicks – maximum grip");

    private final String displayName;
    private final double gripBonus;
    private final double accelBonus;
    private final int    unlockLevel;
    private final int    purchaseCost;
    private final String description;

    TireType(String displayName, double gripBonus, double accelBonus,
             int unlockLevel, int purchaseCost, String description) {
        this.displayName  = displayName;
        this.gripBonus    = gripBonus;
        this.accelBonus   = accelBonus;
        this.unlockLevel  = unlockLevel;
        this.purchaseCost = purchaseCost;
        this.description  = description;
    }

    public String getDisplayName()  { return displayName; }
    public double getGripBonus()    { return gripBonus; }
    public double getAccelBonus()   { return accelBonus; }
    public int    getUnlockLevel()  { return unlockLevel; }
    public int    getPurchaseCost() { return purchaseCost; }
    public String getDescription()  { return description; }
}
