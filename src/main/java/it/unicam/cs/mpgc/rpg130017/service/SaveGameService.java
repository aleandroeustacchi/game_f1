package it.unicam.cs.mpgc.rpg130017.service;

import it.unicam.cs.mpgc.rpg130017.model.GameState;
import it.unicam.cs.mpgc.rpg130017.model.Player;
import it.unicam.cs.mpgc.rpg130017.persistence.SaveRepository;

import java.io.IOException;

public class SaveGameService {
    private final SaveRepository saveRepository;

    public SaveGameService(SaveRepository saveRepository) {
        this.saveRepository = saveRepository;
    }

    public void saveGame(Player player, int tier, int winsInCurrentTier, boolean bossDefeated, String filename) throws IOException {
        GameState state = GameState.fromPlayer(player, tier, winsInCurrentTier, bossDefeated);
        saveRepository.save(state, filename);
    }

    public GameState loadGame(String filename) throws IOException {
        return saveRepository.load(filename);
    }
}
