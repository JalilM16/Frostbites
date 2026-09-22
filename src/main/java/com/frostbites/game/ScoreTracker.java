package com.frostbites.game;

/**
 * Keeps track of your score during one run of a level.
 * Points are multiplied by {@code multiplier} (mostly 1 for now).
 */
public class ScoreTracker {
    private int currentScore;
    private int multiplier;
    /** How many words actually added points (used for stats / WPM type stuff). */
    private int wordsCompletedThisSession;

    /** Start at 0 points and multiplier 1. */
    public ScoreTracker() {
        this.currentScore = 0;
        this.multiplier = 1;
    }

    /**
     * When you finish typing a word, call this. Longer words give more points.
     * Formula: (word length * 10) * multiplier, added to the running total.
     *
     * @param wordLength length of the word you finished; nothing happens if this is not positive
     */
    public void addPoints(int wordLength) {
        if (wordLength > 0) {
            wordsCompletedThisSession++;
            int basePoints = wordLength * 10;
            currentScore = currentScore + (basePoints * multiplier);
        }
    }

    /**
     * How many words have scored points so far this session.
     *
     * @return number of times {@link #addPoints(int)} counted a word
     */
    public int getWordsCompletedThisSession() {
        return wordsCompletedThisSession;
    }

    /**
     * Little bonus if the player did the order perfectly (+25 points).
     */
    public void addTipBonus() {
        currentScore = currentScore + 25;
    }

    /**
     * Whatever the score is right now.
     *
     * @return total points for this run
     */
    public int getCurrentScore() {
        return currentScore;
    }

    /**
     * Setter, mostly for testing or if we ever load mid-game (we do not really).
     *
     * @param currentScore new total score to store
     */
    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    /**
     * Current score multiplier applied to base word points.
     *
     * @return multiplier value (usually 1)
     */
    public int getMultiplier() {
        return multiplier;
    }

    /**
     * Sets the score multiplier.
     *
     * @param multiplier new multiplier
     */
    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }
}
