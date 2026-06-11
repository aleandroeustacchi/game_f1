package it.unicam.cs.mpgc.rpg130017.service;

import it.unicam.cs.mpgc.rpg130017.model.*;

public class UpgradeService {

    // ── Upgrade currently-equipped part ────────────────────────────────────
    public boolean upgradeEngine(Player p) {
        int level = p.getCar().getEngineLevel();
        int cost  = upgradeCost(level);
        if (p.spendMoney(cost)) { p.getCar().upgradeEngine(); return true; }
        return false;
    }
    public boolean upgradeTires(Player p) {
        int level = p.getCar().getTiresLevel();
        int cost  = upgradeCost(level);
        if (p.spendMoney(cost)) { p.getCar().upgradeTires(); return true; }
        return false;
    }
    public boolean upgradeNitro(Player p) {
        int level = p.getCar().getNitroLevel();
        int cost  = upgradeCost(level);
        if (p.spendMoney(cost)) { p.getCar().upgradeNitro(); return true; }
        return false;
    }
    public boolean upgradeFrame(Player p) {
        int level = p.getCar().getWeightReductionLevel();
        int cost  = upgradeCost(level);
        if (p.spendMoney(cost)) { p.getCar().upgradeFrame(); return true; }
        return false;
    }

    /** Exponential cost per level: 500, 840, 1299, 1892 … */
    public int upgradeCost(int currentLevel) {
        return (int) (500 * Math.pow(currentLevel + 1, 1.5));
    }

    // ── Buy a new component tier ───────────────────────────────────────────
    public boolean buyEngineType(Player p, EngineType type) {
        if (p.getCar().ownsEngine(type)) return false;              // already owned
        if (p.getCar().getEngineLevel() < type.getUnlockLevel()) return false; // not unlocked
        if (!p.spendMoney(type.getPurchaseCost())) return false;
        p.getCar().buyEngine(type);
        return true;
    }
    public boolean buyTireType(Player p, TireType type) {
        if (p.getCar().ownsTires(type)) return false;
        if (p.getCar().getTiresLevel() < type.getUnlockLevel()) return false;
        if (!p.spendMoney(type.getPurchaseCost())) return false;
        p.getCar().buyTires(type);
        return true;
    }
    public boolean buyNitroType(Player p, NitroType type) {
        if (p.getCar().ownsNitro(type)) return false;
        if (p.getCar().getNitroLevel() < type.getUnlockLevel()) return false;
        if (!p.spendMoney(type.getPurchaseCost())) return false;
        p.getCar().buyNitro(type);
        return true;
    }
    public boolean buyFrameType(Player p, FrameType type) {
        if (p.getCar().ownsFrame(type)) return false;
        if (p.getCar().getWeightReductionLevel() < type.getUnlockLevel()) return false;
        if (!p.spendMoney(type.getPurchaseCost())) return false;
        p.getCar().buyFrame(type);
        return true;
    }

    // ── Equip an owned component ───────────────────────────────────────────
    public boolean equipEngine(Player p, EngineType type) {
        if (!p.getCar().ownsEngine(type)) return false;
        p.getCar().equipEngine(type); return true;
    }
    public boolean equipTires(Player p, TireType type) {
        if (!p.getCar().ownsTires(type)) return false;
        p.getCar().equipTires(type); return true;
    }
    public boolean equipNitro(Player p, NitroType type) {
        if (!p.getCar().ownsNitro(type)) return false;
        p.getCar().equipNitro(type); return true;
    }
    public boolean equipFrame(Player p, FrameType type) {
        if (!p.getCar().ownsFrame(type)) return false;
        p.getCar().equipFrame(type); return true;
    }
}
