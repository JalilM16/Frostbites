package com.frostbites.game;

import backend.Word;

/**
 * One customer's order. Goes through stages 1 to 3 (order, build, serve).
 * Patience ticks down until 0 (failure)
 */
public class Order {
    private int currentStage;
    private int patienceTimer;
    private Word stageWord;
    private boolean isComplete;

    /**
     * Order with no word yet (some code paths set {@link #setStageWord(Word)} later).
     *
     * @param currentStage  starting stage (usually 1)
     * @param patienceTimer starting patience in seconds (or ticks, depending on caller)
     */
    public Order(int currentStage, int patienceTimer) {
        this.currentStage = currentStage;
        this.patienceTimer = patienceTimer;
        this.stageWord = null;
        this.isComplete = false;
    }

    /**
     * Normal case: the word for this stage is known up front.
     *
     * @param currentStage  starting stage (usually 1)
     * @param patienceTimer starting patience value
     * @param stageWord     word the player types for this stage
     */
    public Order(int currentStage, int patienceTimer, Word stageWord) {
        this.currentStage = currentStage;
        this.patienceTimer = patienceTimer;
        this.stageWord = stageWord;
        this.isComplete = false;
    }

    /**
     * Called each tick. Patience goes down by 1 but never below 0.
     */
    public void reducePatience() {
        if (patienceTimer > 0) {
            patienceTimer = patienceTimer - 1;
        }
    }

    /**
     * After you finish the word for this stage, moves 1 to 2 to 3 (stops at 3 aka max level).
     */
    public void nextStage() {
        if (currentStage < 3) {
            currentStage = currentStage + 1;
        }
    }

    /**
     * Whether this order is fully done (served).
     *
     * @return {@code true} if complete
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Auto-clean / cheats: marks the whole order done without typing the rest.
     */
    public void forceComplete() {
        this.isComplete = true;
    }

    /**
     * Current pipeline stage for this order.
     *
     * @return stage number 1, 2, or 3
     */
    public int getCurrentStage() {
        return currentStage;
    }

    /**
     * Sets the stage (used when resetting or jumping state).
     *
     * @param currentStage new stage value
     */
    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;
    }

    /**
     * How much patience is left.
     *
     * @return patience counter
     */
    public int getPatienceTimer() {
        return patienceTimer;
    }

    /**
     * Sets patience (reset or power-up).
     *
     * @param patienceTimer new patience value
     */
    public void setPatienceTimer(int patienceTimer) {
        this.patienceTimer = patienceTimer;
    }

    /**
     * The word tied to this stage, if any.
     *
     * @return current {@link Word}, or {@code null}
     */
    public Word getStageWord() {
        return stageWord;
    }

    /**
     * Assigns or replaces the word for this stage.
     *
     * @param stageWord new word (may be {@code null})
     */
    public void setStageWord(Word stageWord) {
        this.stageWord = stageWord;
    }
}
