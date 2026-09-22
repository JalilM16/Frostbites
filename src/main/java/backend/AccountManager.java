package backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory registry of {@link PlayerAccount} records backed by {@link DataManager} persistence.
 * Loads accounts on construction, writes the list back after mutations (create, stats, admin resets).
 */
public class AccountManager {
    private List<PlayerAccount> accounts;
    private DataManager dataManager = new DataManager();

    /**
     * Loads saved players from disk and ensures a demo account exists (see {@link #ensureDemoTestAccount()}).
     */
    public AccountManager() {
        this.accounts = dataManager.loadPlayers();
        ensureDemoTestAccount();
    }

    /**
     * If no account named {@code test} exists it creates one with password {@code 123} and saves.
     * Used so demos and grading have a known login
     */
    private void ensureDemoTestAccount() {
        if (findAccount("test") == null) {
            accounts.add(new PlayerAccount("test", "123"));
            dataManager.savePlayers(accounts);
        }
    }

    /**
     * Appends a new account and persists the full player list
     *
     * @param user username for the new account
     * @param pass password for the new account
     */
    public void createAccount(String user, String pass) {
        accounts.add(new PlayerAccount(user, pass));
        dataManager.savePlayers(accounts);
    }

    /**
     * Registers a new player if the username is not taken and both fields are non-blank after trim
     *
     * @param user proposed username (may be null)
     * @param pass proposed password (may be null)
     * @return {@code true} if a new account was created; {@code false} if validation failed or name exists
     */
    public boolean registerPlayer(String user, String pass) {
        if (user == null || pass == null) return false;
        String username = user.trim();
        String password = pass.trim();
        if (username.isEmpty() || password.isEmpty()) return false;
        if (usernameExists(username)) return false;
        createAccount(username, password);
        return true;
    }

    /**
     * @param username name to check (trimmed, case-insensitive match via {@link #findAccount(String)})
     * @return {@code true} if an account with that username exists
     */
    public boolean usernameExists(String username) {
        return findAccount(username) != null;
    }

    /**
     * Looks up an account by username; matching ignores surrounding whitespace and is case-insensitive
     *
     * @param username the login name (may be null)
     * @return the matching {@link PlayerAccount}, or {@code null} if none
     */
    public PlayerAccount findAccount(String username) {
        if (username == null) return null;
        String key = username.trim().toLowerCase();
        for (PlayerAccount account : accounts) {
            if (account != null && account.getUsername() != null
                && account.getUsername().trim().toLowerCase().equals(key)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Confirms the account exists and the given password equals the stored password (both trimmed)
     *
     * @param username login name
     * @param password password attempt
     * @return {@code true} only if credentials match
     */
    public boolean verifyCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        PlayerAccount account = findAccount(username);
        if (account == null) {
            return false;
        }
        String stored = account.getPassword();
        if (stored == null) {
            return false;
        }
        return stored.trim().equals(password.trim());
    }

    /**
     * Updates the given player's global high score, per-level best, aggregate {@link PlayerStats}, and
     * highest level reached when applicable; then saves all players
     * Does nothing if the username is unknown.
     *
     * @param username      player to update
     * @param score         score from the finished session
     * @param wpm           words per minute for that game
     * @param accuracyPct   accuracy percentage
     * @param errorCount    number of typing errors
     * @param timeSeconds   session length in seconds
     * @param typedChars    total keystrokes attempted
     * @param correctChars  correct keystrokes
     * @param levelName     level identifier for per-level bests
     * @param levelComplete whether the level was completed successfully
     */
    public void recordTypingResult(
        String username,
        int score,
        double wpm,
        double accuracyPct,
        int errorCount,
        int timeSeconds,
        int typedChars,
        int correctChars,
        String levelName,
        boolean levelComplete
    ) {
        PlayerAccount account = findAccount(username);
        if (account == null) return;

        if (score > account.getHighScore()) {
            account.setHighScore(score);
        }
        account.updateBestScoreOnLevel(levelName, score);

        PlayerStats stats = account.getStats();
        stats.recordGame(wpm, accuracyPct, errorCount, timeSeconds, typedChars, correctChars);
        account.updateHighestLevelIfBetter(levelName, levelComplete);
        dataManager.savePlayers(accounts);
    }

    /**
     * @return a new list of all accounts sorted by {@link PlayerAccount#getHighScore()} descending
     */
    public List<PlayerAccount> getLeaderboardByHighScore() {
        List<PlayerAccount> copy = new ArrayList<>(accounts);
        for (int i = 0; i < copy.size() - 1; i++) {
            for (int j = i + 1; j < copy.size(); j++) {
                if (copy.get(j).getHighScore() > copy.get(i).getHighScore()) {
                    PlayerAccount temp = copy.get(i);
                    copy.set(i, copy.get(j));
                    copy.set(j, temp);
                }
            }
        }
        return copy;
    }

    /**
     * @return a new list of all accounts sorted by each account's best WPM descending
     */
    public List<PlayerAccount> getLeaderboardByBestWpm() {
        List<PlayerAccount> copy = new ArrayList<>(accounts);
        for (int i = 0; i < copy.size() - 1; i++) {
            for (int j = i + 1; j < copy.size(); j++) {
                if (copy.get(j).getStats().getBestWpm() > copy.get(i).getStats().getBestWpm()) {
                    PlayerAccount temp = copy.get(i);
                    copy.set(i, copy.get(j));
                    copy.set(j, temp);
                }
            }
        }
        return copy;
    }

    /**
     * @return non-empty trimmed usernames from all valid accounts, sorted alphabetically
     */
    public List<String> getAllUsernames() {
        List<String> names = new ArrayList<>();
        for (PlayerAccount account : accounts) {
            if (account != null && account.getUsername() != null && !account.getUsername().trim().isEmpty()) {
                names.add(account.getUsername().trim());
            }
        }
        Collections.sort(names);
        return names;
    }

    /**
     * @param username player login
     * @return that player's {@link PlayerStats}, or {@code null} if not found
     */
    public PlayerStats getStatsFor(String username) {
        PlayerAccount account = findAccount(username);
        return (account == null) ? null : account.getStats();
    }

    /**
     * Checks whether the entered PIN matches the stored admin PIN
     *
     * @param enteredPin the PIN typed by the user
     * @return true if the PIN is correct, false otherwise
     */
    public boolean verifyAdminPin(String enteredPin) {
        String storedPin = dataManager.loadAdminPin();
        return storedPin.equals(enteredPin);
    }

    /**
     * Resets a player's password to a new value
     *
     * @param username    the player whose password should be changed
     * @param newPassword the new password to set
     * @return true if the password was updated, false if the player was not found
     *         or the new password is blank
     */
    public boolean resetPassword(String username, String newPassword) {
        PlayerAccount account = findAccount(username);
        if (account == null) {
            return false;
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        account.setPassword(newPassword.trim());
        dataManager.savePlayers(accounts);
        return true;
    }

    /**
     * Resets all typing statistics and the high score for a specific player.
     * Their account (username and password) is kept intact.
     *
     * @param username the player whose stats should be cleared
     * @return true if the stats were reset, false if the player was not found
     */
    public boolean resetStats(String username) {
        PlayerAccount account = findAccount(username);
        if (account == null) {
            return false;
        }
        account.setStats(new PlayerStats());
        account.setHighScore(0);
        account.clearPerLevelBestScores();
        account.setHighestLevelReached("");
        dataManager.savePlayers(accounts);
        return true;
    }

    /**
     * Resets the high score to 0 for every player on the leaderboard
     * Individual typing statistics (WPM, accuracy, etc.) are left unchanged
     */
    public void resetAllHighScores() {
        for (PlayerAccount account : accounts) {
            if (account != null) {
                account.setHighScore(0);
                account.clearPerLevelBestScores();
            }
        }
        dataManager.savePlayers(accounts);
    }
}