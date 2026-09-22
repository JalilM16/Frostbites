package backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class PowerUpTest {

    @Test
    void testFreezeTimePowerUpType() {
        FreezeTimePowerUp freeze = new FreezeTimePowerUp(5000);
        assertEquals(PowerUpType.FREEZE_TIME, freeze.getType());
    }

    @Test
    void testFreezeTimePowerUpDuration() {
        FreezeTimePowerUp freeze = new FreezeTimePowerUp(5000);
        assertEquals(5000, freeze.getFreezeDuration());
    }

    @Test
    void testAutoCleanPowerUpType() {
        AutoCleanPowerUp clean = new AutoCleanPowerUp(1);
        assertEquals(PowerUpType.AUTO_CLEAN, clean.getType());
    }

    @Test
    void testAutoCleanPowerUpWordsToComplete() {
        AutoCleanPowerUp clean = new AutoCleanPowerUp(2);
        assertEquals(2, clean.getWordsToComplete());
    }

    @Test
    void testPowerUpStartsInactive() {
        FreezeTimePowerUp freeze = new FreezeTimePowerUp(5000);
        assertFalse(freeze.isActive());
    }

    @Test
    void testActivatePowerUp() {
        FreezeTimePowerUp freeze = new FreezeTimePowerUp(5000);
        freeze.activate();
        assertTrue(freeze.isActive());
    }

    @Test
    void testDeactivatePowerUp() {
        FreezeTimePowerUp freeze = new FreezeTimePowerUp(5000);
        freeze.activate();
        freeze.deactivate();
        assertFalse(freeze.isActive());
    }

    @Test
    void testFreezeTimePowerUpStopsHeartLoss() {
        com.frostbites.game.Level level = new com.frostbites.game.Level("Ice Cream Shop", 500, 60);
        com.frostbites.game.GameSession session = new com.frostbites.game.GameSession(level);
        int heartsBefore = session.getHearts();

        FreezeTimePowerUp freeze = new FreezeTimePowerUp(5000);
        freeze.applyEffect(session);

        session.updateOrderTimer();
        assertEquals(heartsBefore, session.getHearts());
    }

    @Test
    void testAutoCleanPowerUpCompletesOrder() {
        com.frostbites.game.Level level = new com.frostbites.game.Level("Ice Cream Shop", 500, 60);
        com.frostbites.game.GameSession session = new com.frostbites.game.GameSession(level);

        AutoCleanPowerUp clean = new AutoCleanPowerUp(1);
        clean.applyEffect(session);

        assertEquals(2, session.getActiveOrder().getCurrentStage());
        assertTrue(session.getCompletedWordsCount() >= 1);
    }

    @Test
    void testAutoCleanPowerUpAwardsPoints() {
        com.frostbites.game.Level level = new com.frostbites.game.Level("Ice Cream Shop", 500, 60);
        com.frostbites.game.GameSession session = new com.frostbites.game.GameSession(level);
        int scoreBefore = session.getPlayerScore();

        AutoCleanPowerUp clean = new AutoCleanPowerUp(1);
        clean.applyEffect(session);

        assertTrue(session.getPlayerScore() > scoreBefore);
    }

}