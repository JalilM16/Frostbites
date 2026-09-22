package com.frostbites.game;

import backend.Difficulty;

/**
 * Stores simple level information for a gameplay session.
 */
public class Level {

    public static final String ICE_CREAM_SHOP = "Ice Cream Shop";
    public static final String BUSY_MALL = "Busy Mall";
    public static final String MEGA_DESSERT_FACTORY = "Mega Dessert Factory";

    private String levelName;
    private int targetScore;
    private int timeLimit;

    /**
     * Creates a level w name, score goal, and time limit
     *
     * @param levelName level display name
     * @param targetScore score needed to complete the level
     * @param timeLimit time limit in seconds
     */
    public Level(String levelName, int targetScore, int timeLimit) {
        this.levelName = levelName;
        this.targetScore = targetScore;
        this.timeLimit = timeLimit;
    }

    /**
     * Returns the level name
     *
     * @return level name
     */
    public String getLevelName() {
        return levelName;
    }

    /**
     * Sets the level name
     *
     * @param levelName new name
     */
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    /**
     * Returns the score needed to pass
     *
     * @return target score
     */
    public int getTargetScore() {
        return targetScore;
    }

    /**
     * Sets the score needed to pass
     *
     * @param targetScore new target score
     */
    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }

    /**
     * Returns the level time limit in s
     *
     * @return time limit
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /**
     * Sets the level time limit in seconds
     *
     * @param timeLimit new time limit
     */
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * Word-list difficulty for this level (matches {@link #ICE_CREAM_SHOP} / {@link #BUSY_MALL} / factory)
     */
    public Difficulty getWordDifficulty() {
        String n = levelName;
        if (ICE_CREAM_SHOP.equals(n) || "Ice Cream Stand".equals(n)) {
            return Difficulty.EASY;
        }
        if (BUSY_MALL.equals(n) || "Busy Mall Shop".equals(n)) {
            return Difficulty.MEDIUM;
        }
        return Difficulty.HARD;
    }

    /**
     * Whether this is the final shipped tier (Mega Dessert Factory). Used when there is no harder level yet
     */
    public boolean isHardestLevel() {
        return levelName != null && MEGA_DESSERT_FACTORY.equals(levelName);
    }

    /**
     * Creates the Ice Cream Shop (easy) level
     *
     * @return level data
     */
    public static Level createIceCreamShop() {
        return new Level(ICE_CREAM_SHOP, 250, 25);
    }

    /**
     * Creates the Busy Mall (medium) level
     *
     * @return level data
     */
    public static Level createBusyMall() {
        return new Level(BUSY_MALL, 450, 20);
    }

    /**
     * Creates the Mega Dessert Factory (hard) level
     *
     * @return level data
     */
    public static Level createMegaDessertFactory() {
        return new Level(MEGA_DESSERT_FACTORY, 700, 15);
    }

    /**
     * Maps dashboard "Level n" buttons to shop difficulty (1–3 cycle for higher slots)
     *
     * @param dashboardSlot 1-based index from the level grid
     * @return matching {@link Level}
     */
    public static Level forDashboardSlot(int dashboardSlot) {
        int tier = ((dashboardSlot - 1) % 3) + 1;
        if (tier == 1) {
            return createIceCreamShop();
        } else if (tier == 2) {
            return createBusyMall();
        } else {
            return createMegaDessertFactory();
        }
    }

    /**
     * Build a level from the same name string used in {@link com.frostbites.ui.ScreenManager.GameResult}.
     * Accepts legacy names from older saves.
     */
    public static Level fromLevelName(String name) {
        if (name == null) {
            return null;
        }
        if (ICE_CREAM_SHOP.equals(name) || "Ice Cream Stand".equals(name)) {
            return createIceCreamShop();
        }
        if (BUSY_MALL.equals(name) || "Busy Mall Shop".equals(name)) {
            return createBusyMall();
        }
        if (MEGA_DESSERT_FACTORY.equals(name)) {
            return createMegaDessertFactory();
        }
        return null;
    }

    /**
     * Next harder stage after the one just played (Easy → Medium → Hard; Hard stays Hard).
     */
    public static Level nextAfter(Level current) {
        if (current == null) {
            return createIceCreamShop();
        }
        String n = current.getLevelName();
        if (ICE_CREAM_SHOP.equals(n) || "Ice Cream Stand".equals(n)) {
            return createBusyMall();
        }
        if (BUSY_MALL.equals(n) || "Busy Mall Shop".equals(n)) {
            return createMegaDessertFactory();
        }
        return createMegaDessertFactory();
    }

    /**
     * Folder under {@code assets/images/gameplay/} with per-stage PNGs ({@code cN_order|build|serve.png}).
     */
    public String getGameplayAssetDirectory() {
        String n = levelName;
        if (ICE_CREAM_SHOP.equals(n) || "Ice Cream Stand".equals(n)) {
            return "assets/images/gameplay/level_1/";
        }
        if (BUSY_MALL.equals(n) || "Busy Mall Shop".equals(n)) {
            return "assets/images/gameplay/level_2/";
        }
        return "assets/images/gameplay/level_3/";
    }
}
