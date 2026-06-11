package it.unicam.cs.mpgc.rpg130017.service;

import it.unicam.cs.mpgc.rpg130017.model.*;
import java.util.List;

public class RaceService {

    /** Create a standard 1v1 drag race. */
    public RaceMode createDragRace(Player player, Racer opponent) {
        DragRaceMode race = new DragRaceMode(player, opponent);
        race.start();
        return race;
    }

    /** Create a tournament race with multiple opponents. */
    public TournamentRaceMode createTournamentRace(Player player, List<Racer> opponents) {
        TournamentRaceMode race = new TournamentRaceMode(player, opponents);
        race.start();
        return race;
    }

    /** Process 1v1 result and apply money/rep to player. */
    public RaceRewards processRaceResult(Player player, Racer opponent,
                                         boolean isPlayerWinner, boolean isBoss) {
        int money, rep;
        if (isPlayerWinner) {
            if (isBoss && opponent instanceof BossRacer) {
                money = ((BossRacer) opponent).getRewardMoney();
                rep   = 250;
            } else {
                money = 500; rep = 50;
            }
        } else {
            money = isBoss ? 300 : 150;
            rep   = isBoss ? 15  : 10;
        }
        player.addMoney(money);
        player.addReputation(rep);
        return new RaceRewards(money, rep);
    }

    /** Process tournament result based on finishing position. */
    public RaceRewards processTournamentResult(Player player, int position) {
        int money, rep;
        switch (position) {
            case 1  -> { money = 3000; rep = 150; }
            case 2  -> { money = 1500; rep =  75; }
            case 3  -> { money =  800; rep =  40; }
            default -> { money =  200; rep =  10; }
        }
        player.addMoney(money);
        player.addReputation(rep);
        return new RaceRewards(money, rep);
    }

    // ── Inner result record ────────────────────────────────────────────────
    public static class RaceRewards {
        private final int money, reputation;
        public RaceRewards(int money, int reputation) { this.money = money; this.reputation = reputation; }
        public int getMoney()      { return money; }
        public int getReputation() { return reputation; }
    }
}
