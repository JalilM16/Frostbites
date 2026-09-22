package backend;

import com.frostbites.game.GameSession;

/**
 * Abstract base class for all power-ups.
 * Each subclass implements applyEffect() to define its behaviour.
 */
public abstract class PowerUp {

    protected PowerUpType type;
    protected boolean isActive;
    protected int durationMs;
    protected String name;

    /**
     * Constructs a PowerUp with the given type and duration.
     *
     * @param type       the PowerUpType enum value
     * @param durationMs how long the power-up lasts in milliseconds
     */
    public PowerUp(PowerUpType type, int durationMs) {
        this.type = type;
        this.durationMs = durationMs;
        this.isActive = false;
    }

    /**
     * Activates this power-up.
     */
    public void activate() {
        isActive = true;
    }

    /**
     * Deactivates this power-up.
     */
    public void deactivate() {
        isActive = false;
    }

    /**
     * Returns whether this power-up is currently active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns the type of this power-up.
     *
     * @return PowerUpType enum value
     */
    public PowerUpType getType() {
        return type;
    }

    /**
     * Applies this power-up's effect to the current game session.
     * Each subclass defines its own behaviour here.
     *
     * @param session the active GameSession to apply the effect to
     */
    public abstract void applyEffect(GameSession session);
}
