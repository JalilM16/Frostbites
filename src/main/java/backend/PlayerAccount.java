package backend;

import java.util.HashMap;
import java.util.Map;

/**
 * One login: username/password, overall high score, {@link PlayerStats}, and per-level bests.
 * Gson reads and writes instances to JSON.
 */
public class PlayerAccount {
    private String username;
    private String password;
    private int highScore;
    private PlayerStats stats;
    /** Hardest level the player has cleared (saved to Players.json). */
    private String highestLevelReached;
    /** Best score achieved on each level name (Gson: string keys to integers). */
    private Map<String, Integer> bestScoreByLevel;

    /**
     * Default account for Gson; fields get safe defaults and lazy init in getters.
     */
    public PlayerAccount() {
        this.username = "";
        this.password = "";
        this.highScore = 0;
        this.stats = new PlayerStats();
        this.highestLevelReached = "";
    }

    /**
     * New account with login fields set.
     *
     * @param username chosen username
     * @param password chosen password (stored as provided; project may hash elsewhere)
     */
    public PlayerAccount(String username, String password) {
        this.username = username;
        this.password = password;
        this.highScore = 0;
        this.stats = new PlayerStats();
        this.highestLevelReached = "";
    }

    /**
     * @return stored username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return stored password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return overall high score for this account
     */
    public int getHighScore() {
        return highScore;
    }

    /**
     * @param highScore new overall high score
     */
    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    /**
     * Never returns {@code null}; creates {@link PlayerStats} if Gson left it out.
     *
     * @return this player's aggregate stats object
     */
    public PlayerStats getStats() {
        if (stats == null) {
            stats = new PlayerStats();
        }
        return stats;
    }

    /**
     * @param stats stats to attach; {@code null} becomes a fresh {@link PlayerStats}
     */
    public void setStats(PlayerStats stats) {
        this.stats = (stats == null) ? new PlayerStats() : stats;
    }

    /**
     * @return hardest level name saved, or empty string if none / blank in file
     */
    public String getHighestLevelReached() {
        if (highestLevelReached == null || highestLevelReached.isBlank()) {
            return "";
        }
        return highestLevelReached;
    }

    /**
     * @param highestLevelReached level name to store, or {@code null} to clear
     */
    public void setHighestLevelReached(String highestLevelReached) {
        this.highestLevelReached = highestLevelReached != null ? highestLevelReached : "";
    }

    /**
     * Map from level name to best score. Lazily creates an empty map if Gson deserialized {@code null}.
     *
     * @return mutable map of level name to best score
     */
    public Map<String, Integer> getBestScoreByLevel() {
        if (bestScoreByLevel == null) {
            bestScoreByLevel = new HashMap<>();
        }
        return bestScoreByLevel;
    }

    /**
     * @param bestScoreByLevel map to store; {@code null} becomes an empty map
     */
    public void setBestScoreByLevel(Map<String, Integer> bestScoreByLevel) {
        this.bestScoreByLevel = bestScoreByLevel != null ? bestScoreByLevel : new HashMap<>();
    }

    /**
     * Looks up the saved personal best for one level.
     *
     * @param levelName level display name or id as used in the game
     * @return best score for that level, or 0 if missing / blank name
     */
    public int getBestScoreOnLevel(String levelName) {
        if (levelName == null || levelName.isBlank()) {
            return 0;
        }
        String key = levelName.trim();
        Integer v = getBestScoreByLevel().get(key);
        return v != null ? v : 0;
    }

    /**
     * Stores {@code score} for {@code levelName} only if it beats the previous best.
     *
     * @param levelName which level (trimmed key)
     * @param score     score just achieved; ignored if negative or name invalid
     */
    public void updateBestScoreOnLevel(String levelName, int score) {
        if (levelName == null || levelName.isBlank() || score < 0) {
            return;
        }
        String key = levelName.trim();
        int prev = getBestScoreOnLevel(key);
        if (score > prev) {
            getBestScoreByLevel().put(key, score);
        }
    }

    /**
     * Clears every per-level best (e.g. profile reset).
     */
    public void clearPerLevelBestScores() {
        getBestScoreByLevel().clear();
    }

    /**
     * If the player completed a level and {@link #rankForLevel(String)} says it is harder than
     * {@link #highestLevelReached}, updates the stored name.
     *
     * @param levelName     name of the level just finished
     * @param levelComplete whether the run actually cleared the level
     */
    public void updateHighestLevelIfBetter(String levelName, boolean levelComplete) {
        if (!levelComplete || levelName == null || levelName.isBlank()) {
            return;
        }
        if (rankForLevel(levelName) > rankForLevel(this.highestLevelReached)) {
            this.highestLevelReached = levelName.trim();
        }
    }

    /**
     * Rough difficulty rank from substring checks on the level name (0 unknown, 3 hardest).
     *
     * @param name level name to classify
     * @return integer rank for comparison only
     */
    public static int rankForLevel(String name) {
        if (name == null || name.isEmpty()) {
            return 0;
        }
        String n = name.toLowerCase();
        if (n.contains("factory") || n.contains("mega")) {
            return 3;
        }
        if (n.contains("mall") || n.contains("busy")) {
            return 2;
        }
        if (n.contains("ice cream") || n.contains("stand") || n.contains("shop")) {
            return 1;
        }
        return 0;
    }
}
