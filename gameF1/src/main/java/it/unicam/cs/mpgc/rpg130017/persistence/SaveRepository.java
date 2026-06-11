package it.unicam.cs.mpgc.rpg130017.persistence;

import it.unicam.cs.mpgc.rpg130017.model.GameState;
import java.io.IOException;

public interface SaveRepository {
    void save(GameState state, String filename) throws IOException;
    GameState load(String filename) throws IOException;
}
