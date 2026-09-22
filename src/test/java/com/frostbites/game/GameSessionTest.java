package com.frostbites.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Game session rules exercised by JUnit (complements PowerUp integration tests).
 *
 * @see backend.PowerUpTest freeze / auto-clean integration cases
 */
class GameSessionTest {

    private Level level;

    @BeforeEach
    void setUp() {
        level = new Level(Level.ICE_CREAM_SHOP, 500, 60);
    }

    // -------------------------------------------------------------------------
    // Validation doc TC9 — game over when all hearts lost (FR-3.1.2)
    // -------------------------------------------------------------------------

    @Test
    void whenHeartsSetToZero_sessionIsGameOverAndNotLevelComplete() {
        GameSession session = new GameSession(level);
        assertFalse(session.isGameOver());

        session.setHearts(0);

        assertTrue(session.isGameOver());
        assertFalse(session.isLevelComplete());
        assertEquals(0, session.getHearts());
    }

    // -------------------------------------------------------------------------
    // Validation doc TC10 — level complete when target score reached (FR-3.1.3)
    // -------------------------------------------------------------------------

    @Test
    void whenScoreReachesTarget_sessionIsLevelCompleteAndGameOver() {
        GameSession session = new GameSession(level);

        session.addScorePoints(500);

        assertTrue(session.isLevelComplete());
        assertTrue(session.isGameOver());
    }

    @Test
    void addScorePoints_ignoresNonPositivePoints() {
        GameSession session = new GameSession(level);
        int before = session.getPlayerScore();

        session.addScorePoints(0);
        session.addScorePoints(-10);

        assertEquals(before, session.getPlayerScore());
    }

    // -------------------------------------------------------------------------
    // Lives / typing — heart loss from repeated wrong keys (FR-3.1.2)
    // Documents observable heart loss when patience-expiry path is hard to hit.
    // -------------------------------------------------------------------------

    @Test
    void threeWrongKeystrokesInARow_reducesHeartsByOne() {
        GameSession session = new GameSession(level);
        String text = session.getActiveOrder().getStageWord().getText();
        assertFalse(text.isEmpty(), "Order should start with a word");
        char wrong = text.charAt(0) == 'z' ? 'a' : 'z';

        int heartsBefore = session.getHearts();
        session.handleKeystroke(wrong);
        session.handleKeystroke(wrong);
        assertEquals(heartsBefore, session.getHearts());
        session.handleKeystroke(wrong);

        assertEquals(heartsBefore - 1, session.getHearts());
    }

    // -------------------------------------------------------------------------
    // Level timer edge — actual behaviour for short time limits (vs doc TC8)
    // -------------------------------------------------------------------------

    @Test
    void whenLevelTimeLimitIsOne_firstTimerTickEndsSession_byLevelClock() {
        Level shortLevel = new Level(Level.ICE_CREAM_SHOP, 500, 1);
        GameSession session = new GameSession(shortLevel);

        session.updateOrderTimer();

        assertTrue(session.isGameOver());
        assertFalse(session.isLevelComplete());
    }
}
