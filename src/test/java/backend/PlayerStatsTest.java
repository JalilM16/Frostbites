package backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerStatsTest {

    private PlayerStats stats;

    @BeforeEach
    void setUp() {
        stats = new PlayerStats();
    }

    @Test
    void testInitialValuesAreZero() {
        assertEquals(0, stats.getGamesPlayed());
        assertEquals(0.0, stats.getBestWpm(), 0.01);
        assertEquals(0.0, stats.getBestAccuracyPct(), 0.01);
    }

    @Test
    void testRecordGameUpdatesGamesPlayed() {
        stats.recordGame(45.0, 90.0, 5, 60, 100, 95);
        assertEquals(1, stats.getGamesPlayed());
    }

    @Test
    void testRecordGameUpdatesBestWpm() {
        stats.recordGame(45.0, 90.0, 5, 60, 100, 95);
        stats.recordGame(70.0, 85.0, 3, 60, 100, 97);
        assertEquals(70.0, stats.getBestWpm(), 0.01);
    }

    @Test
    void testRecordGameUpdatesBestAccuracy() {
        stats.recordGame(45.0, 90.0, 5, 60, 100, 95);
        stats.recordGame(45.0, 95.0, 2, 60, 100, 98);
        assertEquals(95.0, stats.getBestAccuracyPct(), 0.01);
    }

    @Test
    void testAverageAccuracyCalculation() {
        stats.recordGame(45.0, 90.0, 5, 60, 100, 90);
        assertEquals(90.0, stats.getAverageAccuracyPct(), 0.01);
    }

    @Test
    void testTotalTimeAccumulates() {
        stats.recordGame(45.0, 90.0, 5, 60, 100, 95);
        stats.recordGame(45.0, 90.0, 5, 60, 100, 95);
        assertEquals(120, stats.getTotalTimeSeconds());
    }

    @Test
    void testTotalErrorCountAccumulates() {
        stats.recordGame(45.0, 90.0, 5, 60, 100, 95);
        stats.recordGame(45.0, 90.0, 3, 60, 100, 95);
        assertEquals(8, stats.getTotalErrorCount());
    }

    @Test
    void testNegativeValuesAreIgnored() {
        stats.recordGame(45.0, 90.0, -5, -60, -100, -95);
        assertEquals(0, stats.getTotalErrorCount());
        assertEquals(0, stats.getTotalTimeSeconds());
    }

    @Test
    void testAverageWpmIsRunningAverageOfSessions() {
        stats.recordGame(40.0, 90.0, 2, 60, 100, 95);
        stats.recordGame(60.0, 88.0, 3, 60, 100, 96);
        assertEquals(50.0, stats.getAverageWpm(), 0.01);
    }
}
