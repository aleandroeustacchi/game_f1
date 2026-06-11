package it.unicam.cs.mpgc.rpg130017.model;

public interface RaceMode {
    void start();
    void update(double deltaTime);
    void shiftPlayerGear();
    
    double getPlayerProgress(); // 0.0 to 1.0
    double getOpponentProgress(); // 0.0 to 1.0
    
    double getPlayerSpeed(); // km/h
    double getOpponentSpeed(); // km/h
    
    int getPlayerGear();
    int getOpponentGear();
    
    double getPlayerRpm(); // 0.0 to 1.0
    double getPerfectZoneStart();
    double getPerfectZoneEnd();
    double getGoodZoneStart();
    double getGoodZoneEnd();
    
    boolean isFinished();
    boolean isPlayerWinner();
    
    String getPlayerFeedback(); // e.g. "PERFECT SHIFT!", "GOOD SHIFT!", "BAD SHIFT!"
    String getModeName();
}
