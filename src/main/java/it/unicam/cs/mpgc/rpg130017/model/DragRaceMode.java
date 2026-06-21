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

    private double getGearCap(int currentGear, int maxGears) {
        if (currentGear >= maxGears) return 1.0;
        return Math.pow((double) currentGear / maxGears, 0.9);
    }

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
    private boolean started      = false;
    private boolean falseStart   = false;
    private double  greenTime    = 0;
    private double  opponentGreenTime = 0;
    private double  opponentReactionTarget = 0.3;
    private final Random random  = new Random();

    public DragRaceMode(Player player, Racer opponent) {
        this.player   = player;
        this.opponent = opponent;
    }

    @Override
    public void start() {
        playerDistance = opponentDistance = 0;
        playerSpeed    = opponentSpeed    = 0;
        playerGear     = opponentGear     = 0;
        playerRpm      = opponentRpm      = 0.15;
        playerAccelMod = opponentAccelMod = 1.0;
        playerFeedback = "WAIT FOR GREEN...";
        feedbackTimer  = 0;
        started = false;
        falseStart = false;
        greenTime = opponentGreenTime = 0;
        opponentReactionTarget = 0.2 + random.nextDouble() * 0.4;
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

        if (!started) {
            // Idle revving
            playerRpm = 0.15 + random.nextDouble() * 0.05;
            opponentRpm = 0.15 + random.nextDouble() * 0.05;
            if (feedbackTimer > 0 && (feedbackTimer -= dt) <= 0) playerFeedback = "";
            return;
        }

        // Feedback timer
        if (feedbackTimer > 0 && (feedbackTimer -= dt) <= 0) playerFeedback = "";

        // ── Player ──────────────────────────────────────────────────────
        int pMaxGears = player.getCar().getMaxGears();
        double pMax = player.getCar().getTopSpeed() / 3.6;
        double pMaxGear = pMax * getGearCap(playerGear, pMaxGears);

        if (playerGear == 0) {
            greenTime += dt;
            if (falseStart && greenTime > 0.0) {
                playerFeedback = "BAD START!";
                playerAccelMod = 0.6;
                feedbackTimer = 1.5;
                playerGear = 1;
                playerRpm = 0.2;
            } else {
                playerRpm = 0.15 + random.nextDouble() * 0.05;
            }
        } else if (playerGear == pMaxGears) {
            // Top gear logic: float RPM around 70-85%, reduce accel gradually
            double speedRatio = playerSpeed / pMax;
            playerRpm = 0.65 + 0.20 * speedRatio;
            playerAccelMod = Math.max(0.1, 1.0 - Math.pow(speedRatio, 3));
        } else {
            double rpmRate = (0.75 / (playerGear * 0.4 + 0.6))
                    * (1.0 - playerSpeed / (pMax + 15.0));
            rpmRate = Math.max(0.08, rpmRate);
            playerRpm = Math.min(1.0, playerRpm + rpmRate * dt);

            if (playerRpm >= 1.0) {
                playerAccelMod = 0.25;
                playerFeedback = "REDLINE!";
                feedbackTimer  = 0.3;
            }
        }

        if (playerGear > 0) {
            double pAccel = player.getCar().getAcceleration() * playerAccelMod;
            playerSpeed = Math.min(pMaxGear, playerSpeed + pAccel * dt);
            playerDistance += playerSpeed * dt;
        }

        // ── Opponent ─────────────────────────────────────────────────────
        int oMaxGears = opponent.getCar().getMaxGears();
        double oMax    = opponent.getCar().getTopSpeed() / 3.6;
        double oMaxGear = oMax * getGearCap(opponentGear, oMaxGears);

        if (opponentGear == 0) {
            opponentGreenTime += dt;
            if (opponentGreenTime >= opponentReactionTarget) {
                opponentGear = 1;
                opponentRpm = 0.3;
                if (opponentReactionTarget < 0.25) opponentAccelMod = 1.25;
                else if (opponentReactionTarget < 0.5) opponentAccelMod = 1.05;
                else opponentAccelMod = 0.8;
            } else {
                opponentRpm = 0.15 + random.nextDouble() * 0.05;
            }
        } else if (opponentGear == oMaxGears) {
            double speedRatio = opponentSpeed / oMax;
            opponentRpm = 0.65 + 0.20 * speedRatio;
            opponentAccelMod = Math.max(0.1, 1.0 - Math.pow(speedRatio, 3));
        } else {
            double oRpm = (0.75 / (opponentGear * 0.4 + 0.6))
                    * (1.0 - opponentSpeed / (oMax + 15.0));
            oRpm = Math.max(0.08, oRpm);
            opponentRpm = Math.min(1.0, opponentRpm + oRpm * dt);

            if (opponentRpm >= opponentShiftTarget || opponentRpm >= 1.0) shiftOpponent();
        }

        if (opponentGear > 0) {
            double oAccel = opponent.getCar().getAcceleration() * opponentAccelMod;
            opponentSpeed = Math.min(oMaxGear, opponentSpeed + oAccel * dt);
            opponentDistance += opponentSpeed * dt;
        }

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

        if (playerGear == 0) {
            if (!started) {
                falseStart = true;
                playerFeedback = "TOO EARLY!";
                feedbackTimer = 2.0;
            } else {
                if (greenTime < 0.25) { playerFeedback = "PERFECT LAUNCH!"; playerAccelMod = 1.35; }
                else if (greenTime < 0.6) { playerFeedback = "GOOD LAUNCH!"; playerAccelMod = 1.1; }
                else { playerFeedback = "LATE LAUNCH"; playerAccelMod = 0.8; }
                feedbackTimer = 1.5;
                playerGear = 1;
                playerRpm = 0.3;
            }
            return;
        }

        if      (playerRpm >= PERFECT_START && playerRpm <= PERFECT_END) { playerFeedback = "PERFECT SHIFT!"; playerAccelMod = 1.4; }
        else if (playerRpm >= GOOD_START    && playerRpm <= GOOD_END)    { playerFeedback = "GOOD SHIFT!";    playerAccelMod = 1.1; }
        else                                                              { playerFeedback = "BAD SHIFT!";     playerAccelMod = 0.55; }

        feedbackTimer = 1.0;
        int pMaxGears = player.getCar().getMaxGears();
        if (playerGear < pMaxGears) playerGear++;
        playerRpm = 0.22;
    }

    private void shiftOpponent() {
        if      (opponentRpm >= PERFECT_START && opponentRpm <= PERFECT_END) opponentAccelMod = 1.25;
        else if (opponentRpm >= GOOD_START    && opponentRpm <= GOOD_END)    opponentAccelMod = 1.05;
        else                                                                  opponentAccelMod = 0.6;

        int oMaxGears = opponent.getCar().getMaxGears();
        if (opponentGear < oMaxGears) opponentGear++;
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
    public int getMaxGears() { return player.getCar().getMaxGears(); }
    @Override public double getPlayerRpm()        { return playerRpm; }
    @Override public double getPerfectZoneStart() { return PERFECT_START; }
    @Override public double getPerfectZoneEnd()   { return PERFECT_END; }
    @Override public double getGoodZoneStart()    { return GOOD_START; }
    @Override public double getGoodZoneEnd()      { return GOOD_END; }
    @Override public boolean isFinished()         { return finished; }
    @Override public boolean isStarted()          { return started; }
    @Override public boolean isPlayerWinner()     { return playerWinner; }
    @Override public void setStarted(boolean s)   { this.started = s; }
    @Override public String  getPlayerFeedback()  { return playerFeedback; }
    @Override public String  getModeName()        { return "Drag Race"; }
}
