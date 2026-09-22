package backend;

import com.frostbites.game.GameSession;

/**
 * A powerup that freezes all station timers for a set duration
 */
public class FreezeTimePowerUp extends PowerUp {

    private int freezeDurationMs;

    /**
     * Constructs a FreezeTimePowerUp with the given freeze duration
     *
     * @param freezeDurationMs how long to freeze timers in milliseconds
     */
    public FreezeTimePowerUp(int freezeDurationMs) {
        super(PowerUpType.FREEZE_TIME, freezeDurationMs);
        this.freezeDurationMs = freezeDurationMs;
        this.name = "Freeze Time";
    }

    /**
     * Freezes all stations in the session for the freeze duration
     *
     * @param session the active GameSession
     */
    @Override
    public void applyEffect(GameSession session) {
        session.freezeAllStations(freezeDurationMs);
        activate();
    }

    /**
     * Returns the freeze duration
     *
     * @return freeze duration in milliseconds
     */
    public int getFreezeDuration() {
        return freezeDurationMs;
    }
}
