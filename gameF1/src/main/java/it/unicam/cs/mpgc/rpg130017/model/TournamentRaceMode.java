package it.unicam.cs.mpgc.rpg130017.model;

import java.util.*;

/**
 * Tournament race: player vs N AI opponents simultaneously.
 * Classifica calcolata in tempo reale dalla distanza percorsa.
 */
public class TournamentRaceMode implements RaceMode {

    private static final double TOTAL_DISTANCE = 402.33;
    private static final double[] GEAR_CAPS    = { 0.20, 0.38, 0.57, 0.78, 1.00 };
    private static final double PERFECT_START  = 0.70;
    private static final double PERFECT_END    = 0.82;
    private static final double GOOD_START     = 0.55;
    private static final double GOOD_END       = 0.90;

    private final Player       player;
    private final List<Racer>  opponents;
    private final Random       random = new Random();

    // Player state
    private double playerDistance = 0, playerSpeed = 0;
    private int    playerGear     = 1;
    private double playerRpm      = 0, playerAccelMod = 1.0;
    private String playerFeedback = "START!";
    private double feedbackTimer  = 0;

    // AI states (parallel arrays, index == opponent index)
    private final double[] aiDist, aiSpeed, aiRpm, aiAccelMod, aiShiftTarget;
    private final int[]    aiGear;

    private boolean finished     = false;
    private boolean playerWinner = false;
    private int     playerFinishPos = -1;   // 1-based finish position

    public TournamentRaceMode(Player player, List<Racer> opponents) {
        this.player    = player;
        this.opponents = opponents;
        int n = opponents.size();
        aiDist        = new double[n];
        aiSpeed       = new double[n];
        aiRpm         = new double[n];
        aiAccelMod    = new double[n];
        aiShiftTarget = new double[n];
        aiGear        = new int[n];
    }

    @Override
    public void start() {
        playerDistance = playerSpeed = playerRpm = 0;
        playerGear = 1;  playerAccelMod = 1.0;
        playerFeedback = "READY... GO!";  feedbackTimer = 1.5;
        finished = false;  playerWinner = false;  playerFinishPos = -1;

        Arrays.fill(aiDist, 0);  Arrays.fill(aiSpeed, 0);
        Arrays.fill(aiRpm, 0);   Arrays.fill(aiAccelMod, 1.0);
        Arrays.fill(aiGear, 1);
        for (int i = 0; i < opponents.size(); i++) pickAiShiftPoint(i);
    }

    private void pickAiShiftPoint(int i) {
        double t = 0.68 + random.nextDouble() * 0.14;
        aiShiftTarget[i] = Math.min(GOOD_END, Math.max(GOOD_START, t));
    }

    @Override
    public void update(double dt) {
        if (finished) return;
        if (feedbackTimer > 0 && (feedbackTimer -= dt) <= 0) playerFeedback = "";

        // ── Player ──────────────────────────────────────────────────────
        double pMax    = player.getCar().getTopSpeed() / 3.6;
        double pCapGear = pMax * GEAR_CAPS[playerGear - 1];
        double rpmRate  = Math.max(0.08,
                (0.75 / (playerGear * 0.4 + 0.6)) * (1 - playerSpeed / (pMax + 15)));
        playerRpm = Math.min(1.0, playerRpm + rpmRate * dt);
        if (playerRpm >= 1.0) { playerAccelMod = 0.25; playerFeedback = "REDLINE!"; feedbackTimer = 0.3; }
        playerSpeed    = Math.min(pCapGear, playerSpeed + player.getCar().getAcceleration() * playerAccelMod * dt);
        playerDistance += playerSpeed * dt;

        // ── AI ───────────────────────────────────────────────────────────
        for (int i = 0; i < opponents.size(); i++) {
            Car    car    = opponents.get(i).getCar();
            double oMax   = car.getTopSpeed() / 3.6;
            double oCapGear = oMax * GEAR_CAPS[aiGear[i] - 1];

            double oRpmRate = Math.max(0.08,
                    (0.75 / (aiGear[i] * 0.4 + 0.6)) * (1 - aiSpeed[i] / (oMax + 15)));
            aiRpm[i] = Math.min(1.0, aiRpm[i] + oRpmRate * dt);
            if (aiRpm[i] >= aiShiftTarget[i] || aiRpm[i] >= 1.0) shiftAi(i);

            aiSpeed[i] = Math.min(oCapGear, aiSpeed[i] + car.getAcceleration() * aiAccelMod[i] * dt);
            aiDist[i] += aiSpeed[i] * dt;
        }

        // ── Win check: finished when first crosses line ──────────────────
        if (!finished) {
            boolean anyDone = playerDistance >= TOTAL_DISTANCE;
            for (double d : aiDist) if (d >= TOTAL_DISTANCE) { anyDone = true; break; }

            if (anyDone) {
                finished = true;
                playerFinishPos = computePlayerFinishPosition();
                playerWinner    = (playerFinishPos == 1);
                playerFeedback  = playerWinner ? "VICTORY!" : "P" + playerFinishPos;
                feedbackTimer   = 5.0;
            }
        }
    }

    private void shiftAi(int i) {
        double rpm = aiRpm[i];
        if      (rpm >= PERFECT_START && rpm <= PERFECT_END) aiAccelMod[i] = 1.25;
        else if (rpm >= GOOD_START    && rpm <= GOOD_END)    aiAccelMod[i] = 1.05;
        else                                                  aiAccelMod[i] = 0.6;
        if (aiGear[i] < 5) aiGear[i]++;
        aiRpm[i] = 0.22;
        pickAiShiftPoint(i);
    }

    /** Returns 1-based finish position of the player. */
    private int computePlayerFinishPosition() {
        int ahead = 0;
        for (double d : aiDist) if (d > playerDistance) ahead++;
        return ahead + 1;
    }

    // ── Leaderboard ───────────────────────────────────────────────────────
    /** Returns live standings as a list: index 0 = 1st place, etc.
     *  Each entry: [name, progress 0..1] */
    public List<double[]> getLiveStandings() {
        List<double[]> entries = new ArrayList<>();
        // Player entry
        entries.add(new double[]{ 0 /* marker for player */, playerDistance });
        for (int i = 0; i < opponents.size(); i++) {
            entries.add(new double[]{ i + 1, aiDist[i] });
        }
        entries.sort((a, b) -> Double.compare(b[1], a[1]));
        return entries;
    }

    public List<Racer> getOpponents()       { return opponents; }
    public int  getPlayerFinishPos()        { return playerFinishPos; }
    public double getAiDistance(int i)      { return aiDist[i]; }
    public double getAiSpeed(int i)         { return aiSpeed[i] * 3.6; }

    // ── RaceMode interface ─────────────────────────────────────────────────
    @Override public double getPlayerProgress()   { return Math.min(1.0, playerDistance / TOTAL_DISTANCE); }
    @Override public double getOpponentProgress() {
        double best = 0; for (double d : aiDist) if (d > best) best = d;
        return Math.min(1.0, best / TOTAL_DISTANCE);
    }
    @Override public double getPlayerSpeed()      { return playerSpeed * 3.6; }
    @Override public double getOpponentSpeed()    { return aiSpeed.length > 0 ? aiSpeed[0] * 3.6 : 0; }
    @Override public int    getPlayerGear()       { return playerGear; }
    @Override public int    getOpponentGear()     { return aiGear.length > 0 ? aiGear[0] : 1; }
    @Override public double getPlayerRpm()        { return playerRpm; }
    @Override public double getPerfectZoneStart() { return PERFECT_START; }
    @Override public double getPerfectZoneEnd()   { return PERFECT_END; }
    @Override public double getGoodZoneStart()    { return GOOD_START; }
    @Override public double getGoodZoneEnd()      { return GOOD_END; }
    @Override public boolean isFinished()         { return finished; }
    @Override public boolean isPlayerWinner()     { return playerWinner; }
    @Override public String  getPlayerFeedback()  { return playerFeedback; }
    @Override public String  getModeName()        { return "Tournament"; }

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
}
