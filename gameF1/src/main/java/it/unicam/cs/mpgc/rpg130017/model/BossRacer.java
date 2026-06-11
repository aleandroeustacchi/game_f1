package it.unicam.cs.mpgc.rpg130017.model;

public class BossRacer extends Racer {
    private int unlockTier;
    private int rewardMoney;
    private String rewardUnlocks;

    public BossRacer() {}

    public BossRacer(String name, Car car, int unlockTier, int rewardMoney, String rewardUnlocks) {
        super(name, car);
        this.unlockTier = unlockTier;
        this.rewardMoney = rewardMoney;
        this.rewardUnlocks = rewardUnlocks;
    }

    public int getUnlockTier() {
        return unlockTier;
    }

    public void setUnlockTier(int unlockTier) {
        this.unlockTier = unlockTier;
    }

    public int getRewardMoney() {
        return rewardMoney;
    }

    public void setRewardMoney(int rewardMoney) {
        this.rewardMoney = rewardMoney;
    }

    public String getRewardUnlocks() {
        return rewardUnlocks;
    }

    public void setRewardUnlocks(String rewardUnlocks) {
        this.rewardUnlocks = rewardUnlocks;
    }
}
