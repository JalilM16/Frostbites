package com.frostbites.ui;

import backend.AccountManager;
import com.frostbites.audio.AudioManager;
import com.frostbites.game.GameSession;
import com.frostbites.game.Level;
import com.frostbites.ui.screens.BaseScreen;
import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Central app state: {@link CardLayout} container of {@link BaseScreen}s, login handle, selected {@link Level},
 * latest {@link GameResult}, and shared {@link AccountManager}. Menu {@link AudioManager} uses the same volume/mute as settings.
 */
public class ScreenManager {
    /** {@link #registerScreen} / {@link #showScreen} key for the main menu */
    public static final String MAIN_MENU = "mainMenu";
    /** login form screen */
    public static final String LOGIN = "login";
    /** post-login hub */
    public static final String DASHBOARD = "dashboard";
    /** how-to slides */
    public static final String TUTORIAL = "tutorial";
    /** leaderboard table */
    public static final String HIGH_SCORES = "highScores";
    /** PIN-protected admin tools */
    public static final String MANAGERS_OFFICE = "managersOffice";
    /** typing minigame */
    public static final String GAMEPLAY = "gameplay";
    /** end-of-level summary */
    public static final String RESULTS = "results";
    /** audio preferences */
    public static final String SETTINGS = "settings";

    private final CardLayout cardLayout;
    private final JPanel container;
    private final Map<String, BaseScreen> screens;
    private final AccountManager accountManager;
    /** Volume/mute the user picked in Settings (copied into each new game). */
    private float masterVolume = 0.85f;
    private boolean muted;
    /** Button clicks on menus use this; the running game uses {@code gameSession.getAudioManager()}. */
    private final AudioManager menuAudioManager = new AudioManager();

    private Level selectedGameplayLevel = Level.createIceCreamShop();
    private String currentScreenKey;
    private GameResult latestGameResult;
    private String loggedInUsername;

    /**
     * builds empty card deck and applies current volume/mute to {@link #menuAudioManager}
     *
     * @param accountManager shared backend (players JSON)
     */
    public ScreenManager(AccountManager accountManager) {
        this.cardLayout = new CardLayout();
        this.container = new JPanel(cardLayout);
        this.screens = new HashMap<>();
        this.accountManager = accountManager;
        copyVolumeSettingsTo(menuAudioManager);
    }

    /** @return master volume in {@code [0, 1]} */
    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * clamps to {@code [0, 1]} and pushes into {@link #menuAudioManager}
     *
     * @param masterVolume slider-derived level
     */
    public void setMasterVolume(float masterVolume) {
        if (masterVolume < 0f) {
            masterVolume = 0f;
        } else if (masterVolume > 1f) {
            masterVolume = 1f;
        }
        this.masterVolume = masterVolume;
        copyVolumeSettingsTo(menuAudioManager);
    }

    /** @return true if the user turned mute on in settings */
    public boolean isMuted() {
        return muted;
    }

    /**
     * @param muted when true menu audio is silenced
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
        copyVolumeSettingsTo(menuAudioManager);
    }

    /**
     * @return menu/UI {@link AudioManager} kept in sync with settings before each use
     */
    public AudioManager getMenuAudioManager() {
        copyVolumeSettingsTo(menuAudioManager);
        return menuAudioManager;
    }

    /** applies {@link #masterVolume} and {@link #muted} to the given manager */
    private void copyVolumeSettingsTo(AudioManager audio) {
        audio.setVolume(masterVolume);
        audio.setMuted(muted);
    }

    /**
     * @param level non-null replaces {@link #selectedGameplayLevel}
     */
    public void setSelectedGameplayLevel(Level level) {
        if (level != null) {
            this.selectedGameplayLevel = level;
        }
    }

    /** @return level chosen on dashboard (default ice cream) */
    public Level getSelectedGameplayLevel() {
        return selectedGameplayLevel;
    }

    /**
     * adds a screen to the card deck (call from {@link com.frostbites.ui.MainFrame} startup)
     *
     * @param key    must match a {@code *_} constant on this class
     * @param screen concrete {@link BaseScreen}
     */
    public void registerScreen(String key, BaseScreen screen) {
        screens.put(key, screen);
        container.add(screen, key);
    }

    /**
     * calls {@link BaseScreen#onHide()} on the previous card, shows {@code key}, then {@link BaseScreen#onShow()}
     *
     * @param key registered screen name
     * @throws IllegalArgumentException if {@code key} unknown
     */
    public void showScreen(String key) {
        BaseScreen next = screens.get(key);
        if (next == null) {
            throw new IllegalArgumentException("Unknown screen key: " + key);
        }
        if (currentScreenKey != null) {
            BaseScreen leaving = screens.get(currentScreenKey);
            if (leaving != null) {
                leaving.onHide();
            }
        }
        cardLayout.show(container, key);
        next.onShow();
        currentScreenKey = key;
    }

    /**
     * @param latestGameResult last run handed off from {@link com.frostbites.ui.screens.GameplayScreen}
     */
    public void setLatestGameResult(GameResult latestGameResult) {
        this.latestGameResult = latestGameResult;
    }

    /** @return last {@link GameResult} or null */
    public GameResult getLatestGameResult() {
        return latestGameResult;
    }

    /**
     * Writes session aggregates to the logged-in player's {@link backend.PlayerStats} and saves JSON.
     *
     * @param session finished or abandoned run; no-op if null or not logged in
     */
    public void persistSessionStats(GameSession session) {
        if (!isLoggedIn() || session == null) {
            return;
        }
        double wpm = session.getWpm();
        double accuracy = session.getAccuracy();
        int err = Math.max(0, session.getKeystrokesAttempted() - session.getKeystrokesCorrect());
        int sec = (int) Math.max(0L, (session.getActivePlayMillis() + 999L) / 1000L);
        accountManager.recordTypingResult(
                getLoggedInUsername(),
                session.getPlayerScore(),
                wpm,
                accuracy,
                err,
                sec,
                session.getKeystrokesAttempted(),
                session.getKeystrokesCorrect(),
                session.getLevel().getLevelName(),
                session.isLevelComplete());
    }

    /**
     * @param username account name after successful login
     */
    public void setLoggedInUser(String username) {
        this.loggedInUsername = username;
    }

    /** @return true if {@link #loggedInUsername} is non-blank */
    public boolean isLoggedIn() {
        return loggedInUsername != null && !loggedInUsername.isBlank();
    }

    /** @return logged-in username or null */
    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    /** clears {@link #loggedInUsername} */
    public void logout() {
        loggedInUsername = null;
    }

    /** @return root {@link JPanel} with {@link CardLayout} for {@link javax.swing.JFrame#setContentPane} */
    public JPanel getContainer() {
        return container;
    }

    /** @return shared accounts backend */
    public AccountManager getAccountManager() {
        return accountManager;
    }

    /**
     * DTO from gameplay to {@link com.frostbites.ui.screens.ResultsScreen} and optional {@link #persistSessionStats}
     */
    public static class GameResult {
        private int finalScore;
        private int heartsRemaining;
        private boolean levelComplete;
        private String levelName;
        private int wordsPerMinute;
        private int accuracy;
        private int timeSeconds;
        private int errorCount;
        private int typedChars;
        private int correctChars;
        private boolean persistedToProfile;

        /**
         * @param finalScore      score at end of session
         * @param heartsRemaining hearts left
         * @param levelComplete   beat target score in time
         * @param levelName       display name for {@link Level#fromLevelName(String)}
         * @param wordsPerMinute  rounded WPM for UI
         * @param accuracy        percent 0–100
         * @param timeSeconds     active play seconds
         * @param errorCount      wrong keystrokes
         * @param typedChars      total keystrokes
         * @param correctChars    correct keystrokes
         */
        public GameResult(
                int finalScore,
                int heartsRemaining,
                boolean levelComplete,
                String levelName,
                int wordsPerMinute,
                int accuracy,
                int timeSeconds,
                int errorCount,
                int typedChars,
                int correctChars) {
            this.finalScore = finalScore;
            this.heartsRemaining = heartsRemaining;
            this.levelComplete = levelComplete;
            this.levelName = levelName;
            this.wordsPerMinute = wordsPerMinute;
            this.accuracy = accuracy;
            this.timeSeconds = Math.max(0, timeSeconds);
            this.errorCount = Math.max(0, errorCount);
            this.typedChars = Math.max(0, typedChars);
            this.correctChars = Math.max(0, correctChars);
            this.persistedToProfile = false;
        }

        /** @param persistedToProfile set after {@link ScreenManager#persistSessionStats} or manual save */
        public void setPersistedToProfile(boolean persistedToProfile) {
            this.persistedToProfile = persistedToProfile;
        }

        /** @return whether stats for this run were already saved to the player profile */
        public boolean isPersistedToProfile() {
            return persistedToProfile;
        }

        /** @return final score */
        public int getFinalScore() {
            return finalScore;
        }

        /** @return hearts at end of run */
        public int getHeartsRemaining() {
            return heartsRemaining;
        }

        /** @return true if player hit target score before time ran out */
        public boolean isLevelComplete() {
            return levelComplete;
        }

        /** @return level display name */
        public String getLevelName() {
            return levelName;
        }

        /** @return WPM shown on results */
        public int getWordsPerMinute() {
            return wordsPerMinute;
        }

        /** @return accuracy percent 0–100 */
        public int getAccuracy() {
            return accuracy;
        }

        /** @return session length seconds */
        public int getTimeSeconds() {
            return timeSeconds;
        }

        /** @return typing errors */
        public int getErrorCount() {
            return errorCount;
        }

        /** @return keystrokes attempted */
        public int getTypedChars() {
            return typedChars;
        }

        /** @return correct keystrokes */
        public int getCorrectChars() {
            return correctChars;
        }
    }
}
