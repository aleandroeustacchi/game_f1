package it.unicam.cs.mpgc.rpg130017.model;

public enum TransmissionType {
    SPEED_5_RACE("5-Speed Race",      0,   5.0,  0,    0,  "Explosive acceleration, 5 gears"),
    SPEED_6_SPORT("6-Speed Sport",   15,   3.0,  3, 3000,  "Balanced acceleration and top speed, 6 gears"),
    SPEED_7_PRO("7-Speed Highway",   35,   1.0,  6, 8000,  "Highest top speed, 7 gears");

    private final String displayName;
    private final double topSpeedBonus;
    private final double accelBonus;
    private final int    unlockLevel;
    private final int    purchaseCost;
    private final String description;

    TransmissionType(String displayName, double topSpeedBonus, double accelBonus,
                     int unlockLevel, int purchaseCost, String description) {
        this.displayName   = displayName;
        this.topSpeedBonus = topSpeedBonus;
        this.accelBonus    = accelBonus;
        this.unlockLevel   = unlockLevel;
        this.purchaseCost  = purchaseCost;
        this.description   = description;
    }

    public String getDisplayName()   { return displayName; }
    public double getTopSpeedBonus() { return topSpeedBonus; }
    public double getAccelBonus()    { return accelBonus; }
    public int    getUnlockLevel()   { return unlockLevel; }
    public int    getPurchaseCost()  { return purchaseCost; }
    public String getDescription()   { return description; }

    public int getMaxGears() {
        if (this == SPEED_5_RACE) return 5;
        if (this == SPEED_6_SPORT) return 6;
        return 7;
    }
}
