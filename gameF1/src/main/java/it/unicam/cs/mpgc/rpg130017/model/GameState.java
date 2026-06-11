package it.unicam.cs.mpgc.rpg130017.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameState {

    private String playerName   = "StreetRacer";
    private int    money        = 1000;
    private int    reputation   = 0;
    private int    tier         = 1;
    private int    winsInCurrentTier = 0;
    private boolean bossDefeated    = false;

    // Equipped component types (stored as enum name strings for clean JSON)
    private String equippedEngine = EngineType.V6.name();
    private String equippedTires  = TireType.STREET.name();
    private String equippedNitro  = NitroType.BASIC.name();
    private String equippedFrame  = FrameType.STOCK.name();

    // Owned inventory
    private Set<String> ownedEngines = new HashSet<>();
    private Set<String> ownedTires   = new HashSet<>();
    private Set<String> ownedNitros  = new HashSet<>();
    private Set<String> ownedFrames  = new HashSet<>();

    // Per-type upgrade levels
    private Map<String, Integer> engineLevels = new HashMap<>();
    private Map<String, Integer> tireLevels   = new HashMap<>();
    private Map<String, Integer> nitroLevels  = new HashMap<>();
    private Map<String, Integer> frameLevels  = new HashMap<>();

    public GameState() {}

    public static GameState fromPlayer(Player player, int tier,
                                       int winsInCurrentTier, boolean bossDefeated) {
        GameState s = new GameState();
        s.playerName         = player.getName();
        s.money              = player.getMoney();
        s.reputation         = player.getReputation();
        s.tier               = tier;
        s.winsInCurrentTier  = winsInCurrentTier;
        s.bossDefeated       = bossDefeated;

        Car car = player.getCar();
        s.equippedEngine = car.getEquippedEngine().name();
        s.equippedTires  = car.getEquippedTires().name();
        s.equippedNitro  = car.getEquippedNitro().name();
        s.equippedFrame  = car.getEquippedFrame().name();

        s.ownedEngines = new HashSet<>(car.getOwnedEngines());
        s.ownedTires   = new HashSet<>(car.getOwnedTires());
        s.ownedNitros  = new HashSet<>(car.getOwnedNitros());
        s.ownedFrames  = new HashSet<>(car.getOwnedFrames());

        s.engineLevels = new HashMap<>(car.getEngineLevels());
        s.tireLevels   = new HashMap<>(car.getTireLevels());
        s.nitroLevels  = new HashMap<>(car.getNitroLevels());
        s.frameLevels  = new HashMap<>(car.getFrameLevels());

        return s;
    }

    public Player toPlayer() {
        Player player = new Player(this.playerName);
        player.setMoney(this.money);
        player.setReputation(this.reputation);

        Car car = new Car();

        // Restore inventory
        car.setOwnedEngines(new HashSet<>(this.ownedEngines));
        car.setOwnedTires(new HashSet<>(this.ownedTires));
        car.setOwnedNitros(new HashSet<>(this.ownedNitros));
        car.setOwnedFrames(new HashSet<>(this.ownedFrames));

        // Restore levels
        car.setEngineLevels(new HashMap<>(this.engineLevels));
        car.setTireLevels(new HashMap<>(this.tireLevels));
        car.setNitroLevels(new HashMap<>(this.nitroLevels));
        car.setFrameLevels(new HashMap<>(this.frameLevels));

        // Restore equipped
        try { car.equipEngine(EngineType.valueOf(this.equippedEngine)); } catch (Exception ignored) {}
        try { car.equipTires(TireType.valueOf(this.equippedTires));     } catch (Exception ignored) {}
        try { car.equipNitro(NitroType.valueOf(this.equippedNitro));    } catch (Exception ignored) {}
        try { car.equipFrame(FrameType.valueOf(this.equippedFrame));     } catch (Exception ignored) {}

        player.setCar(car);
        return player;
    }

    // --- Getters & Setters ---
    public String getPlayerName()             { return playerName; }
    public void   setPlayerName(String v)     { this.playerName = v; }

    public int  getMoney()                    { return money; }
    public void setMoney(int v)               { this.money = v; }

    public int  getReputation()               { return reputation; }
    public void setReputation(int v)          { this.reputation = v; }

    public int  getTier()                     { return tier; }
    public void setTier(int v)                { this.tier = v; }

    public int  getWinsInCurrentTier()        { return winsInCurrentTier; }
    public void setWinsInCurrentTier(int v)   { this.winsInCurrentTier = v; }

    public boolean isBossDefeated()           { return bossDefeated; }
    public void    setBossDefeated(boolean v) { this.bossDefeated = v; }

    public String getEquippedEngine()         { return equippedEngine; }
    public void   setEquippedEngine(String v) { this.equippedEngine = v; }

    public String getEquippedTires()          { return equippedTires; }
    public void   setEquippedTires(String v)  { this.equippedTires = v; }

    public String getEquippedNitro()          { return equippedNitro; }
    public void   setEquippedNitro(String v)  { this.equippedNitro = v; }

    public String getEquippedFrame()          { return equippedFrame; }
    public void   setEquippedFrame(String v)  { this.equippedFrame = v; }

    public Set<String> getOwnedEngines()      { return ownedEngines; }
    public void setOwnedEngines(Set<String> v){ this.ownedEngines = v; }

    public Set<String> getOwnedTires()        { return ownedTires; }
    public void setOwnedTires(Set<String> v)  { this.ownedTires = v; }

    public Set<String> getOwnedNitros()       { return ownedNitros; }
    public void setOwnedNitros(Set<String> v) { this.ownedNitros = v; }

    public Set<String> getOwnedFrames()       { return ownedFrames; }
    public void setOwnedFrames(Set<String> v) { this.ownedFrames = v; }

    public Map<String, Integer> getEngineLevels()      { return engineLevels; }
    public void setEngineLevels(Map<String, Integer> v){ this.engineLevels = v; }

    public Map<String, Integer> getTireLevels()        { return tireLevels; }
    public void setTireLevels(Map<String, Integer> v)  { this.tireLevels = v; }

    public Map<String, Integer> getNitroLevels()       { return nitroLevels; }
    public void setNitroLevels(Map<String, Integer> v) { this.nitroLevels = v; }

    public Map<String, Integer> getFrameLevels()       { return frameLevels; }
    public void setFrameLevels(Map<String, Integer> v) { this.frameLevels = v; }
}
