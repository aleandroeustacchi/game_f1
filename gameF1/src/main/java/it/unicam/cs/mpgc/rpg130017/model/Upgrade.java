package it.unicam.cs.mpgc.rpg130017.model;

public enum Upgrade {
    ENGINE("Engine", "Improves Top Speed and Acceleration"),
    TIRES("Tires", "Improves Grip and Acceleration"),
    NITRO("Nitro", "Improves Acceleration and Nitro duration"),
    WEIGHT_REDUCTION("Weight Reduction", "Reduces weight, improving Acceleration");

    private final String name;
    private final String description;

    Upgrade(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Calculates the cost of upgrading to the next level.
     * Level 0 to 1 costs 500, level 1 to 2 is 1000, etc.
     */
    public int getCost(int currentLevel) {
        return (int) (500 * Math.pow(currentLevel + 1, 1.5));
    }
}
