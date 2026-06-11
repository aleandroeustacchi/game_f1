package it.unicam.cs.mpgc.rpg130017.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the player's car with a tiered component system.
 * Each component category (engine, tires, nitro, frame) has:
 *  - a set of OWNED types
 *  - a map of upgrade levels per type
 *  - a currently EQUIPPED type
 *
 * Stats are computed from the equipped type's bonuses + per-level upgrades on that type.
 */
public class Car {

    // --- Base Stats (before any upgrades) ---
    private double baseTopSpeed    = 130.0;  // km/h
    private double baseAcceleration = 8.0;   // m/s²
    private double baseGrip        = 40.0;
    private double baseWeight      = 1500.0; // kg

    // --- Engine ---
    private EngineType equippedEngine = EngineType.V6;
    private Set<String>         ownedEngines  = new HashSet<>();
    private Map<String, Integer> engineLevels  = new HashMap<>();

    // --- Tires ---
    private TireType equippedTires = TireType.STREET;
    private Set<String>         ownedTires    = new HashSet<>();
    private Map<String, Integer> tireLevels   = new HashMap<>();

    // --- Nitro ---
    private NitroType equippedNitro = NitroType.BASIC;
    private Set<String>         ownedNitros   = new HashSet<>();
    private Map<String, Integer> nitroLevels  = new HashMap<>();

    // --- Frame ---
    private FrameType equippedFrame = FrameType.STOCK;
    private Set<String>         ownedFrames   = new HashSet<>();
    private Map<String, Integer> frameLevels  = new HashMap<>();

    public Car() {
        // Player starts owning the base tier of everything at level 0
        ownedEngines.add(EngineType.V6.name());
        engineLevels.put(EngineType.V6.name(), 0);

        ownedTires.add(TireType.STREET.name());
        tireLevels.put(TireType.STREET.name(), 0);

        ownedNitros.add(NitroType.BASIC.name());
        nitroLevels.put(NitroType.BASIC.name(), 0);

        ownedFrames.add(FrameType.STOCK.name());
        frameLevels.put(FrameType.STOCK.name(), 0);
    }

    /**
     * Convenience constructor for AI opponents: sets base stats directly.
     * No inventory tracking needed for NPCs.
     */
    public Car(double baseTopSpeed, double baseAcceleration, double baseGrip, double baseWeight) {
        this(); // initialise default inventory
        this.baseTopSpeed     = baseTopSpeed;
        this.baseAcceleration = baseAcceleration;
        this.baseGrip         = baseGrip;
        this.baseWeight       = baseWeight;
    }

    // ============================================================
    //  COMPUTED STATS
    // ============================================================

    public double getTopSpeed() {
        int eLevel = getEngineLevel(equippedEngine);
        int nLevel = getNitroLevel(equippedNitro);
        return baseTopSpeed
                + equippedEngine.getTopSpeedBonus() + eLevel * 8.0
                + equippedNitro.getTopSpeedBonus()  + nLevel * 4.0;
    }

    public double getAcceleration() {
        int eLevel = getEngineLevel(equippedEngine);
        int tLevel = getTireLevel(equippedTires);
        int nLevel = getNitroLevel(equippedNitro);
        int fLevel = getFrameLevel(equippedFrame);
        double weightMod = getWeight() / 160.0;
        return Math.max(2.0,
                baseAcceleration
                + equippedEngine.getAccelBonus() + eLevel * 2.5
                + equippedTires.getAccelBonus()  + tLevel * 1.5
                + equippedNitro.getAccelBonus()  + nLevel * 2.0
                + equippedFrame.getAccelBonus()  + fLevel * 1.0
                - weightMod);
    }

    public double getGrip() {
        int tLevel = getTireLevel(equippedTires);
        return baseGrip + equippedTires.getGripBonus() + tLevel * 5.0;
    }

    public double getWeight() {
        int fLevel = getFrameLevel(equippedFrame);
        return Math.max(700.0,
                baseWeight + equippedFrame.getWeightReduction() - fLevel * 60.0);
    }

    // ============================================================
    //  LEVEL HELPERS
    // ============================================================

    public int getEngineLevel(EngineType type) {
        return engineLevels.getOrDefault(type.name(), 0);
    }
    public int getTireLevel(TireType type) {
        return tireLevels.getOrDefault(type.name(), 0);
    }
    public int getNitroLevel(NitroType type) {
        return nitroLevels.getOrDefault(type.name(), 0);
    }
    public int getFrameLevel(FrameType type) {
        return frameLevels.getOrDefault(type.name(), 0);
    }

    /** Convenience – level of the currently equipped engine */
    public int getEngineLevel()  { return getEngineLevel(equippedEngine); }
    public int getTiresLevel()   { return getTireLevel(equippedTires); }
    public int getNitroLevel()   { return getNitroLevel(equippedNitro); }
    public int getWeightReductionLevel() { return getFrameLevel(equippedFrame); }

    // ============================================================
    //  OWNERSHIP CHECKS
    // ============================================================

    public boolean ownsEngine(EngineType t) { return ownedEngines.contains(t.name()); }
    public boolean ownsTires(TireType t)    { return ownedTires.contains(t.name()); }
    public boolean ownsNitro(NitroType t)   { return ownedNitros.contains(t.name()); }
    public boolean ownsFrame(FrameType t)   { return ownedFrames.contains(t.name()); }

    // ============================================================
    //  BUY (add to inventory)
    // ============================================================

    public void buyEngine(EngineType t) {
        ownedEngines.add(t.name());
        engineLevels.putIfAbsent(t.name(), 0);
    }
    public void buyTires(TireType t) {
        ownedTires.add(t.name());
        tireLevels.putIfAbsent(t.name(), 0);
    }
    public void buyNitro(NitroType t) {
        ownedNitros.add(t.name());
        nitroLevels.putIfAbsent(t.name(), 0);
    }
    public void buyFrame(FrameType t) {
        ownedFrames.add(t.name());
        frameLevels.putIfAbsent(t.name(), 0);
    }

    // ============================================================
    //  UPGRADE (increment level of currently equipped part)
    // ============================================================

    public void upgradeEngine() {
        engineLevels.merge(equippedEngine.name(), 1, Integer::sum);
    }
    public void upgradeTires() {
        tireLevels.merge(equippedTires.name(), 1, Integer::sum);
    }
    public void upgradeNitro() {
        nitroLevels.merge(equippedNitro.name(), 1, Integer::sum);
    }
    public void upgradeFrame() {
        frameLevels.merge(equippedFrame.name(), 1, Integer::sum);
    }

    // ============================================================
    //  EQUIP
    // ============================================================

    public void equipEngine(EngineType t) {
        if (ownedEngines.contains(t.name())) equippedEngine = t;
    }
    public void equipTires(TireType t) {
        if (ownedTires.contains(t.name())) equippedTires = t;
    }
    public void equipNitro(NitroType t) {
        if (ownedNitros.contains(t.name())) equippedNitro = t;
    }
    public void equipFrame(FrameType t) {
        if (ownedFrames.contains(t.name())) equippedFrame = t;
    }

    // ============================================================
    //  GETTERS / SETTERS (for Jackson serialization)
    // ============================================================

    public EngineType getEquippedEngine()  { return equippedEngine; }
    public void setEquippedEngine(EngineType e) { this.equippedEngine = e; }

    public TireType getEquippedTires()     { return equippedTires; }
    public void setEquippedTires(TireType t){ this.equippedTires = t; }

    public NitroType getEquippedNitro()    { return equippedNitro; }
    public void setEquippedNitro(NitroType n){ this.equippedNitro = n; }

    public FrameType getEquippedFrame()    { return equippedFrame; }
    public void setEquippedFrame(FrameType f){ this.equippedFrame = f; }

    public Set<String> getOwnedEngines()  { return ownedEngines; }
    public void setOwnedEngines(Set<String> s){ this.ownedEngines = s; }

    public Set<String> getOwnedTires()    { return ownedTires; }
    public void setOwnedTires(Set<String> s){ this.ownedTires = s; }

    public Set<String> getOwnedNitros()   { return ownedNitros; }
    public void setOwnedNitros(Set<String> s){ this.ownedNitros = s; }

    public Set<String> getOwnedFrames()   { return ownedFrames; }
    public void setOwnedFrames(Set<String> s){ this.ownedFrames = s; }

    public Map<String, Integer> getEngineLevels() { return engineLevels; }
    public void setEngineLevels(Map<String, Integer> m){ this.engineLevels = m; }

    public Map<String, Integer> getTireLevels()   { return tireLevels; }
    public void setTireLevels(Map<String, Integer> m){ this.tireLevels = m; }

    public Map<String, Integer> getNitroLevels()  { return nitroLevels; }
    public void setNitroLevels(Map<String, Integer> m){ this.nitroLevels = m; }

    public Map<String, Integer> getFrameLevels()  { return frameLevels; }
    public void setFrameLevels(Map<String, Integer> m){ this.frameLevels = m; }

    public double getBaseTopSpeed()      { return baseTopSpeed; }
    public void setBaseTopSpeed(double v){ this.baseTopSpeed = v; }

    public double getBaseAcceleration()      { return baseAcceleration; }
    public void setBaseAcceleration(double v){ this.baseAcceleration = v; }

    public double getBaseGrip()      { return baseGrip; }
    public void setBaseGrip(double v){ this.baseGrip = v; }

    public double getBaseWeight()      { return baseWeight; }
    public void setBaseWeight(double v){ this.baseWeight = v; }
}
