package it.unicam.cs.mpgc.rpg130017;

import it.unicam.cs.mpgc.rpg130017.model.Player;
import it.unicam.cs.mpgc.rpg130017.model.EngineType;
import it.unicam.cs.mpgc.rpg130017.service.UpgradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UpgradeServiceTest {
    private Player player;
    private UpgradeService upgradeService;

    @BeforeEach
    public void setUp() {
        player = new Player("TestDriver");
        upgradeService = new UpgradeService();
        player.setMoney(10000);
    }

    @Test
    public void testUpgradeEngineSuccess() {
        int initialLevel = player.getCar().getEngineLevel();
        int cost = upgradeService.upgradeCost(initialLevel);
        int initialMoney = player.getMoney();
        
        assertTrue(upgradeService.upgradeEngine(player));
        assertEquals(initialLevel + 1, player.getCar().getEngineLevel());
        assertEquals(initialMoney - cost, player.getMoney());
    }

    @Test
    public void testUpgradeEngineInsufficientFunds() {
        player.setMoney(10);
        assertFalse(upgradeService.upgradeEngine(player));
        assertEquals(0, player.getCar().getEngineLevel());
        assertEquals(10, player.getMoney());
    }

    @Test
    public void testBuyEngineSuccess() {
        // give max level for base engine to unlock V8
        for(int i = 0; i < 3; i++) upgradeService.upgradeEngine(player);
        int initialMoney = player.getMoney();
        int cost = EngineType.V8.getPurchaseCost();

        assertTrue(upgradeService.buyEngineType(player, EngineType.V8));
        assertTrue(player.getCar().ownsEngine(EngineType.V8));
        assertEquals(initialMoney - cost, player.getMoney());
    }

    @Test
    public void testEquipEngine() {
        // give max level for base engine to unlock V8
        for(int i = 0; i < 3; i++) upgradeService.upgradeEngine(player);
        upgradeService.buyEngineType(player, EngineType.V8);
        assertTrue(upgradeService.equipEngine(player, EngineType.V8));
        assertEquals(EngineType.V8, player.getCar().getEquippedEngine());
    }
}
