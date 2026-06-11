package it.unicam.cs.mpgc.rpg130017;

import it.unicam.cs.mpgc.rpg130017.model.GameState;
import it.unicam.cs.mpgc.rpg130017.model.Player;
import it.unicam.cs.mpgc.rpg130017.model.EngineType;
import it.unicam.cs.mpgc.rpg130017.persistence.JsonSaveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSaveRepositoryTest {
    private JsonSaveRepository repository;
    private final String testFilename = "saves/test_savegame.json";

    @BeforeEach
    public void setUp() {
        repository = new JsonSaveRepository();
    }

    @AfterEach
    public void tearDown() {
        File file = new File(testFilename);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testSaveAndLoadSuccess() throws IOException {
        Player player = new Player("CustomRacer");
        player.setMoney(4500);
        player.setReputation(150);
        player.getCar().buyEngine(EngineType.V8);
        player.getCar().equipEngine(EngineType.V8);
        player.getCar().upgradeEngine(); // level 1
        player.getCar().upgradeEngine(); // level 2
        player.getCar().upgradeEngine(); // level 3

        GameState originalState = GameState.fromPlayer(player, 2, 1, false);
        repository.save(originalState, testFilename);

        File file = new File(testFilename);
        assertTrue(file.exists());

        GameState loadedState = repository.load(testFilename);
        assertNotNull(loadedState);
        assertEquals("CustomRacer", loadedState.getPlayerName());
        assertEquals(4500, loadedState.getMoney());
        assertEquals(150, loadedState.getReputation());
        assertEquals(2, loadedState.getTier());
        assertEquals(EngineType.V8.name(), loadedState.getEquippedEngine());
        assertEquals(3, loadedState.getEngineLevels().get(EngineType.V8.name()));
        assertFalse(loadedState.isBossDefeated());
    }
}
