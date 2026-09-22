package backend;

import com.frostbites.game.GameSession;

/**
 * A power-up that automatically completes the current word on the active
 * station
 */
public class AutoCleanPowerUp extends PowerUp {

    private int wordsToComplete;

    /**
     * Constructs an AutoCleanPowerUp.
     *
     * @param wordsToComplete how many words to auto-complete
     */
    public AutoCleanPowerUp(int wordsToComplete) {
        super(PowerUpType.AUTO_CLEAN, 0);
        this.wordsToComplete = wordsToComplete;
        this.name = "Auto Clean";
    }

    /**
     * Auto-completes the current word on the active station
     *
     * @param session the active GameSession
     */
    @Override
    public void applyEffect(GameSession session) {
        session.autoCompleteCurrentWord(wordsToComplete);
        activate();
    }

    /**
     * Returns how many words this power-up will complete
     *
     * @return words to complete
     */
    public int getWordsToComplete() {
        return wordsToComplete;
    }
}
