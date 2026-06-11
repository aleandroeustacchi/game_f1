package it.unicam.cs.mpgc.rpg130017.model;

import java.util.Random;

/**
 * Drag race simulation.
 * Speed cap per gear ensures top speed is only reached in 5th gear:
 *   Gear 1 → 20 %   Gear 2 → 38 %   Gear 3 → 57 %
 *   Gear 4 → 78 %   Gear 5 → 100 %
 */
public class DragRaceMode implements RaceMode {

    private final Player player;
    private final Racer  opponent;
    private static final double TOTAL_DISTANCE = 402.33; // quarter mile (m)

    private static final double[] GEAR_SPEED_CAPS = { 0.20, 0.38, 0.57, 0.78, 1.00 };

    // ── Player state ──────────────────────────────────────────────────────
    private double playerDistance = 0;
    private double playerSpeed    = 0;   // m/s
    private int    playerGear     = 1;
    private double playerRpm      = 0;   // 0..1
    private double playerAccelMod = 1.0;
    private String playerFeedback = "START!";
    private double feedbackTimer  = 0;

    // ── Opponent state ────────────────────────────────────────────────────
    private double opponentDistance = 0;
    private double opponentSpeed    = 0;
    private int    opponentGear     = 1;
    private double opponentRpm      = 0;
    private double opponentAccelMod = 1.0;
    private double opponentShiftTarget = 0.75;

    // ── Shift zones ───────────────────────────────────────────────────────
    private static final double PERFECT_START = 0.70;
    private static final double PERFECT_END   = 0.82;
    private static final double GOOD_START    = 0.55;
    private static final double GOOD_END      = 0.90;

    private boolean finished     = false;
    private boolean playerWinner = false;
    private final Random random  = new Random();

    public DragRaceMode(Player player, Racer opponent) {
        this.player   = player;
        this.opponent = opponent;
    }

    @Override
    public void start() {
        playerDistance = opponentDistance = 0;
        playerSpeed    = opponentSpeed    = 0;
        playerGear     = opponentGear     = 1;
        playerRpm      = opponentRpm      = 0;
        playerAccelMod = opponentAccelMod = 1.0;
        playerFeedback = "READY... GO!";
        feedbackTimer  = 1.5;
        finished = playerWinner = false;
        pickOpponentShiftPoint();
    }

    private void pickOpponentShiftPoint() {
        opponentShiftTarget = 0.68 + random.nextDouble() * 0.14;
        opponentShiftTarget = Math.min(GOOD_END, Math.max(GOOD_START, opponentShiftTarget));
    }

    @Override
    public void update(double dt) {
        if (finished) return;

        // Feedback timer
        if (feedbackTimer > 0 && (feedbackTimer -= dt) <= 0) playerFeedback = "";

        // ── Player ──────────────────────────────────────────────────────
        double pMax = player.getCar().getTopSpeed() / 3.6;
        double pMaxGear = pMax * GEAR_SPEED_CAPS[playerGear - 1];

        double rpmRate = (0.75 / (playerGear * 0.4 + 0.6))
                * (1.0 - playerSpeed / (pMax + 15.0));
        rpmRate = Math.max(0.08, rpmRate);
        playerRpm = Math.min(1.0, playerRpm + rpmRate * dt);

        if (playerRpm >= 1.0) {
            playerAccelMod = 0.25;
            playerFeedback = "REDLINE!";
            feedbackTimer  = 0.3;
        }

        double pAccel = player.getCar().getAcceleration() * playerAccelMod;
        playerSpeed = Math.min(pMaxGear, playerSpeed + pAccel * dt);
        playerDistance += playerSpeed * dt;

        // ── Opponent ─────────────────────────────────────────────────────
        double oMax    = opponent.getCar().getTopSpeed() / 3.6;
        double oMaxGear = oMax * GEAR_SPEED_CAPS[opponentGear - 1];

        double oRpm = (0.75 / (opponentGear * 0.4 + 0.6))
                * (1.0 - opponentSpeed / (oMax + 15.0));
        oRpm = Math.max(0.08, oRpm);
        opponentRpm = Math.min(1.0, opponentRpm + oRpm * dt);

        if (opponentRpm >= opponentShiftTarget || opponentRpm >= 1.0) shiftOpponent();

        double oAccel = opponent.getCar().getAcceleration() * opponentAccelMod;
        opponentSpeed = Math.min(oMaxGear, opponentSpeed + oAccel * dt);
        opponentDistance += opponentSpeed * dt;

        // ── Win check ────────────────────────────────────────────────────
        if (playerDistance >= TOTAL_DISTANCE || opponentDistance >= TOTAL_DISTANCE) {
            finished     = true;
            playerWinner = (playerDistance >= opponentDistance);
            playerFeedback = playerWinner ? "YOU WIN!" : "YOU LOSE!";
            feedbackTimer  = 5.0;
        }
    }

    @Override
    public void shiftPlayerGear() {
        if (finished || playerRpm < 0.15) return;

        if      (playerRpm >= PERFECT_START && playerRpm <= PERFECT_END) { playerFeedback = "PERFECT SHIFT!"; playerAccelMod = 1.4; }
        else if (playerRpm >= GOOD_START    && playerRpm <= GOOD_END)    { playerFeedback = "GOOD SHIFT!";    playerAccelMod = 1.1; }
        else                                                              { playerFeedback = "BAD SHIFT!";     playerAccelMod = 0.55; }

        feedbackTimer = 1.0;
        if (playerGear < 5) playerGear++;
        playerRpm = 0.22;
    }

    private void shiftOpponent() {
        if      (opponentRpm >= PERFECT_START && opponentRpm <= PERFECT_END) opponentAccelMod = 1.25;
        else if (opponentRpm >= GOOD_START    && opponentRpm <= GOOD_END)    opponentAccelMod = 1.05;
        else                                                                  opponentAccelMod = 0.6;

        if (opponentGear < 5) opponentGear++;
        opponentRpm = 0.22;
        pickOpponentShiftPoint();
    }

    // ── Telemetry ─────────────────────────────────────────────────────────
    @Override public double getPlayerProgress()   { return Math.min(1.0, playerDistance   / TOTAL_DISTANCE); }
    @Override public double getOpponentProgress() { return Math.min(1.0, opponentDistance / TOTAL_DISTANCE); }
    @Override public double getPlayerSpeed()      { return playerSpeed   * 3.6; }
    @Override public double getOpponentSpeed()    { return opponentSpeed * 3.6; }
    @Override public int    getPlayerGear()       { return playerGear; }
    @Override public int    getOpponentGear()     { return opponentGear; }
    @Override public double getPlayerRpm()        { return playerRpm; }
    @Override public double getPerfectZoneStart() { return PERFECT_START; }
    @Override public double getPerfectZoneEnd()   { return PERFECT_END; }
    @Override public double getGoodZoneStart()    { return GOOD_START; }
    @Override public double getGoodZoneEnd()      { return GOOD_END; }
    @Override public boolean isFinished()         { return finished; }
    @Override public boolean isPlayerWinner()     { return playerWinner; }
    @Override public String  getPlayerFeedback()  { return playerFeedback; }
    @Override public String  getModeName()        { return "Drag Race"; }
}
