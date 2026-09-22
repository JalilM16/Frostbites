package com.frostbites.game;

import backend.PlayerAccount;
import backend.Word;
import backend.WordLibrary;
import com.frostbites.audio.AudioManager;

/**
 * Main gameplay controller for one level session.
 * This class connects hearts, score, typing, and stage progression.
 */
public class GameSession {
    private static final int TYPO_HEART_LOSS_THRESHOLD = 3;

    /** Plays typing sounds during this game only (each session has its own) */
    private final AudioManager audioManager = new AudioManager();

    private Level level;
    private Order activeOrder;
    private ScoreTracker scoreTracker;
    private WordLibrary wordLibrary;

    // Timer for the freeze power-up (counts down in update ticks)
    private int freezeTimerTicks = 0;

    // Hearts/health system
    private int hearts;

    // True when session ends (game over or level complete)
    private boolean isGameOver;

    // True when player meets target score
    private boolean levelComplete;

    // Tracks mistakes to decide if tip bous should be applied.
    private int mistakesInCurrentOrder;
    private int typoCountSinceHeartLoss;
    private int remainingLevelTimeSeconds;
    private boolean hasPendingWrongChar;

    /** Stage words fully typed this session (for WPM) */
    private int completedWordsCount;

    /** Keystrokes in {@link #handleKeystroke(char)} (includes auto-complete). */
    private int keystrokesAttempted;
    private int keystrokesCorrect;

    /**
     * Active typing time for this session (GUI adds ms each tick while not paused).
     * Used for {@link #getWpm()}.
     */
    private long activeTypingMillis;

    /**
     * Creates a new game session
     *
     * @param level selected level data
     */
    public GameSession(Level level) {
        this.level = level;
        this.scoreTracker = new ScoreTracker();
        this.wordLibrary = new WordLibrary();
        this.wordLibrary.loadDefaults();
        this.hearts = 3;
        this.isGameOver = false;
        this.levelComplete = false;
        this.mistakesInCurrentOrder = 0;
        this.typoCountSinceHeartLoss = 0;
        this.remainingLevelTimeSeconds = level.getTimeLimit();
        this.hasPendingWrongChar = false;
        this.completedWordsCount = 0;
        this.keystrokesAttempted = 0;
        this.keystrokesCorrect = 0;
        this.activeTypingMillis = 0L;

        spawnNewOrder();
    }

    /**
     * Accumulates play time for WPM (call from gameplay loop when not paused)
     *
     * @param millis elapsed milliseconds to add
     */
    public void addActivePlayMillis(long millis) {
        if (isGameOver || millis <= 0) {
            return;
        }
        activeTypingMillis += millis;
    }

    /** Milliseconds of active play accumulated for this session */
    public long getActivePlayMillis() {
        return activeTypingMillis;
    }

    /**
     * Session words per minute: {@code completedWordsCount / minutesElapsed} where
     * {@code minutesElapsed = activeTypingMillis / 60000}, returns 0 if no words yet or no time
     */
    public double getWpm() {
        if (completedWordsCount <= 0) {
            return 0.0;
        }
        double minutes = activeTypingMillis / 60000.0;
        if (minutes <= 0.0) {
            minutes = 1.0 / 60.0;
        }
        return completedWordsCount / minutes;
    }

    /**
     * Keystroke accuracy for this session: {@code (correctKeystrokes / totalKeystrokes) * 100}
     * Returns 0 if nothing was typed yet
     */
    public double getAccuracy() {
        if (keystrokesAttempted <= 0) {
            return 0.0;
        }
        return (100.0 * keystrokesCorrect) / keystrokesAttempted;
    }

    /** Loudness and mute come from the settings screen; use this to plug them in. */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /** Call once when the level starts so volume/mute match Settings. */
    public void applyAudioPreferences(float masterVolume, boolean muted) {
        audioManager.setVolume(masterVolume);
        audioManager.setMuted(muted);
    }

    /**
     * Called once per second during gameplay. Counts down level time
     */
    public void updateOrderTimer() {
        if (isGameOver || activeOrder == null) {
            return;
        }

        // Freeze power-up: whole second is skipped (level timer + patience stay the same).
        if (freezeTimerTicks > 0) {
            freezeTimerTicks = freezeTimerTicks - 1;
            return;
        }

        if (remainingLevelTimeSeconds > 0) {
            remainingLevelTimeSeconds = remainingLevelTimeSeconds - 1;
        }
        if (remainingLevelTimeSeconds <= 0) {
            remainingLevelTimeSeconds = 0;
            isGameOver = true;
            levelComplete = false;
            return;
        }

        if (activeOrder.isComplete()) {
            return;
        }

        activeOrder.reducePatience();

        if (activeOrder.getPatienceTimer() <= 0) {
            hearts = hearts - 1;
            if (hearts < 0) {
                hearts = 0;
            }
            spawnNewOrder();
        }

        checkGameState();
    }

    /**
     * Moves current order to the next station stage.
     */
    public void advanceOrderStage() {
        if (isGameOver || activeOrder == null) {
            return;
        }

        activeOrder.nextStage();

        if (activeOrder.getCurrentStage() <= 3) {
            Word nextWord = createWordForLevel();
            activeOrder.setStageWord(nextWord);
        }
    }

    /**
     * One key from the UI Right letter moves the word forward; wrong adds mistakes / maybe lose a heart.
     * Cursor helped me untangle the case-insensitive compare and the stage 1/2/3 vs spawn order flow (AI GENERATED)
     *
     * @param typedChar what they hit
     */
    public void handleKeystroke(char typedChar) {
        if (isGameOver || activeOrder == null || activeOrder.getStageWord() == null) {
            return;
        }

        String fullWord = activeOrder.getStageWord().getText();
        String typed = activeOrder.getStageWord().getTypedSoFar();
        int nextIndex = typed.length();

        if (nextIndex < 0 || nextIndex >= fullWord.length()) {
            return;
        }

        // make A and a match for letter keys
        char keyInWord = fullWord.charAt(nextIndex);
        char typedNorm = typedChar;
        if (Character.isLetter(keyInWord)) {
            typedNorm = Character.toLowerCase(typedChar);
        }
        char compareKey = keyInWord;
        if (Character.isLetter(keyInWord)) {
            compareKey = Character.toLowerCase(keyInWord);
        }
        // NOT MY CODE, AI GENERATED
        if (typedNorm != compareKey) {
            keystrokesAttempted++;
            audioManager.playSFX(AudioManager.SFX_TYPING_ERROR);
            mistakesInCurrentOrder = mistakesInCurrentOrder + 1;
            typoCountSinceHeartLoss = typoCountSinceHeartLoss + 1;
            if (typoCountSinceHeartLoss >= TYPO_HEART_LOSS_THRESHOLD) {
                typoCountSinceHeartLoss = 0;
                hearts = hearts - 1;
                if (hearts < 0) {
                    hearts = 0;
                }
                checkGameState();
            }
            hasPendingWrongChar = true;
            return;
        }
        hasPendingWrongChar = false;

        keystrokesAttempted++;

        boolean isCorrect = activeOrder.getStageWord().typeChar(keyInWord);
        if (!isCorrect) {
            return;
        }
        keystrokesCorrect++;

        if (activeOrder.getStageWord().isComplete()) {
            completedWordsCount++;
            int stageWordLength = activeOrder.getStageWord().getText().length();
            scoreTracker.addPoints(stageWordLength);

            if (activeOrder.getCurrentStage() < 3) {
                advanceOrderStage();
                audioManager.playSFX(AudioManager.SFX_WORD_COMPLETE);
            } else {
                if (mistakesInCurrentOrder == 0) {
                    scoreTracker.addTipBonus();
                }
                spawnNewOrder();
                audioManager.playSFX(AudioManager.SFX_ORDER_SERVED);
            }
            checkGameState();
        }
    }

    /**
     * Adds score points from gameplay
     *
     * @param points points to add
     */
    public void addScorePoints(int points) {
        if (isGameOver) {
            return;
        }
        if (points > 0) {
            int updatedScore = scoreTracker.getCurrentScore() + points;
            scoreTracker.setCurrentScore(updatedScore);
        }
        checkGameState();
    }

    /**
     * Adds a tip bonus for perfect order
     */
    public void addPerfectOrderBonus() {
        if (isGameOver) {
            return;
        }
        scoreTracker.addTipBonus();
        checkGameState();
    }

    /**
     * Simple state check (1) Game over if hearts are gone (2) Level complete if score reaches target
     */
    public void checkGameState() {
        if (hearts <= 0) {
            hearts = 0;
            isGameOver = true;
            levelComplete = false;
        } else if (scoreTracker.getCurrentScore() >= level.getTargetScore()) {
            isGameOver = true;
            levelComplete = true;
        } else {
            isGameOver = false;
            levelComplete = false;
        }
    }

    /**
     * Returns remaining hearts
     *
     * @return hearts count
     */
    public int getHearts() {
        return hearts;
    }

    /**
     * Sets hearts count
     *
     * @param hearts new hearts count
     */
    public void setHearts(int hearts) {
        this.hearts = hearts;
        checkGameState();
    }

    /**
     * Returns true when the session has ended
     *
     * @return game over flag
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Returns true if the level was completed successfully
     *
     * @return level complete flag
     */
    public boolean isLevelComplete() {
        return levelComplete;
    }

    /**
     * Returns current level object
     *
     * @return level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Sets current level object
     *
     * @param level new level
     */
    public void setLevel(Level level) {
        this.level = level;
        this.remainingLevelTimeSeconds = level.getTimeLimit();
        spawnNewOrder();
        checkGameState();
    }

    /**
     * Returns active customer order
     *
     * @return active order
     */
    public Order getActiveOrder() {
        return activeOrder;
    }

    /**
     * Sets active customer order
     *
     * @param activeOrder new order
     */
    public void setActiveOrder(Order activeOrder) {
        this.activeOrder = activeOrder;
    }

    /**
     * Returns score tracker
     *
     * @return score tracker
     */
    public ScoreTracker getScoreTracker() {
        return scoreTracker;
    }

    /**
     * Sets score tracker
     *
     * @param scoreTracker new score tracker
     */
    public void setScoreTracker(ScoreTracker scoreTracker) {
        this.scoreTracker = scoreTracker;
        checkGameState();
    }

    /**
     * Returns current score quickly (helper for GUI updates)
     *
     * @return current score
     */
    public int getPlayerScore() {
        return scoreTracker.getCurrentScore();
    }

    /**
     * Returns hearts quickly (helper for GUI updates).
     *
     * @return remaining hearts
     */
    public int getRemainingHearts() {
        return hearts;
    }

    /**
     * Returns current stage name for UI labels.
     *
     * @return Order, Build, or Serve
     */
    public String getCurrentStageName() {
        if (activeOrder == null) {
            return "Order";
        }
        if (activeOrder.getCurrentStage() == 1) {
            return "Order";
        } else if (activeOrder.getCurrentStage() == 2) {
            return "Build";
        }
        return "Serve";
    }

    /**
     * Returns patience percent from 0 to 100 for timer bar rendering.
     *
     * @return patience percentage
     */
    public int getPatiencePercent() {
        if (activeOrder == null || level.getTimeLimit() <= 0) {
            return 0;
        }
        int value = (activeOrder.getPatienceTimer() * 100) / level.getTimeLimit();
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }

    /**
     * Returns remaining session time in seconds.
     *
     * @return remaining level time
     */
    public int getRemainingLevelTimeSeconds() {
        return remainingLevelTimeSeconds;
    }

    /** Words (stage prompts) fully typed this session; excludes timer ticks, pauses handled in UI. */
    public int getCompletedWordsCount() {
        return completedWordsCount;
    }

    /** Keys sent to {@link #handleKeystroke(char)} this session (includes wrong keys). */
    public int getKeystrokesAttempted() {
        return keystrokesAttempted;
    }

    /** Correct keystrokes counted in {@link #handleKeystroke(char)} (used for session accuracy). */
    public int getKeystrokesCorrect() {
        return keystrokesCorrect;
    }

    /**
     * Returns session time percent from 0 to 100.
     *
     * @return remaining level timer percentage
     */
    public int getLevelTimePercent() {
        if (level.getTimeLimit() <= 0) {
            return 0;
        }
        int value = (remainingLevelTimeSeconds * 100) / level.getTimeLimit();
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }

    /**
     * Returns the full current stage word text.
     *
     * @return current stage word
     */
    public String getCurrentWordText() {
        if (activeOrder == null || activeOrder.getStageWord() == null) {
            return "";
        }
        return activeOrder.getStageWord().getText();
    }

    /**
     * Returns correctly typed progress for the current stage word.
     *
     * @return typed progress
     */
    public String getCurrentTypedText() {
        if (activeOrder == null || activeOrder.getStageWord() == null) {
            return "";
        }
        return activeOrder.getStageWord().getTypedSoFar();
    }

    /**
     * Returns true if latest typed character was part of completed word.
     *
     * @return true if stage word is complete
     */
    public boolean isCurrentWordComplete() {
        if (activeOrder == null || activeOrder.getStageWord() == null) {
            return false;
        }
        return activeOrder.getStageWord().isComplete();
    }

    /**
     * Saves final score into player account when session ends.
     * This keeps gameplay code ready for backend persistence integration.
     *
     * @param account player account object
     */
    public void saveFinalScoreToAccount(PlayerAccount account) {
        if (account == null) {
            return;
        }
        // Placeholder method body, Ethan's backend can read score from this session
        // Kept intentionally simple for integration step.
    }

    /**
     * Creates a new order at stage 1 with a fresh word
     */
    private void spawnNewOrder() {
        Word word = createWordForLevel();
        this.activeOrder = new Order(1, level.getTimeLimit(), word);
        this.mistakesInCurrentOrder = 0;
        this.typoCountSinceHeartLoss = 0;
        this.hasPendingWrongChar = false;
    }

    /**
     * Creates a random word from the library based on level difficulty
     *
     * @return random word for current level
     */
    private Word createWordForLevel() {
        return wordLibrary.getRandomWord(level.getWordDifficulty());
    }

    /**
     * HTML for the current stage word, meant for a {@link javax.swing.JLabel} with HTML enabled.
     * Typed prefix is green. the next character is dark gray or red if the last key was wrong
     * the rest of the word is dark gray
     *
     * NOT MY CODE, IMPLEMENTED W AI
     * @return a string starting with {@code <html>} (minimal empty html if there is no active word)
     */
    public String getCurrentWordHtml() {
        if (activeOrder == null || activeOrder.getStageWord() == null) {
            return "<html></html>";
        }

        String fullWord = activeOrder.getStageWord().getText();
        String typed = activeOrder.getStageWord().getTypedSoFar();
        int typedLen = typed.length();
        if (typedLen > fullWord.length()) {
            typedLen = fullWord.length();
        }

        StringBuilder html = new StringBuilder();
        html.append("<html>Current Word: ");
        html.append("<span style='color:#2E7D32;'>");
        html.append(escapeHtml(fullWord.substring(0, typedLen)));
        html.append("</span>");

        if (typedLen < fullWord.length()) {
            String nextChar = escapeHtml(String.valueOf(fullWord.charAt(typedLen)));
            if (hasPendingWrongChar) {
                html.append("<span style='color:#C62828;'>");
                html.append(nextChar);
                html.append("</span>");
            } else {
                html.append("<span style='color:#1F2937;'>");
                html.append(nextChar);
                html.append("</span>");
            }

            if (typedLen + 1 < fullWord.length()) {
                html.append("<span style='color:#1F2937;'>");
                html.append(escapeHtml(fullWord.substring(typedLen + 1)));
                html.append("</span>");
            }
        }

        html.append("</html>");
        return html.toString();
    }

    /** so someone can't type > ; in a word and blow up the label (from tests lol) — tiny helper */
    private String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /**
     * Freeze power-up: skips whole {@link #updateOrderTimer()} ticks (~1s each), so {@code durationMs}
     * maps to about {@code durationMs/1000} frozen seconds.
     *
     * @param durationMs requested freeze length from the power-up
     */
    public void freezeAllStations(int durationMs) {
        if (durationMs <= 0) {
            return;
        }
        int ticks = (durationMs + 999) / 1000;
        if (ticks > freezeTimerTicks) {
            freezeTimerTicks = ticks;
        }
    }

    /**
     * AUTO CLEAN POWERUP: fake keystrokes for whatever's left of the word so all the normal rules run.
     * 
     * @param howManyWords usually 1 for us
     */
    public void autoCompleteCurrentWord(int howManyWords) {
        if (isGameOver || howManyWords <= 0) {
            return;
        }
        for (int w = 0; w < howManyWords; w++) {
            if (activeOrder == null || activeOrder.getStageWord() == null) {
                return;
            }
            String full = activeOrder.getStageWord().getText();
            if (full.length() == 0) {
                return;
            }
            String soFar = activeOrder.getStageWord().getTypedSoFar();
            for (int i = soFar.length(); i < full.length(); i++) {
                if (isGameOver) {
                    return;
                }
                char c = full.charAt(i);
                if (Character.isLetter(c)) {
                    c = Character.toLowerCase(c);
                }
                handleKeystroke(c);
            }
            if (isGameOver) {
                return;
            }
        }
    }
}
