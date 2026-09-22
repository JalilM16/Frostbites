package com.frostbites.ui.screens;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import backend.PlayerAccount;

import com.frostbites.audio.AudioManager;
import com.frostbites.game.GameSession;
import com.frostbites.game.Level;
import com.frostbites.game.Order;
import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.theme.UITheme;

/**
 * the actual typing minigame screen: HUD, scene picture, timer, powerups, pause card, hooks into {@link GameSession}
 */
public class GameplayScreen extends BaseScreen {
    private static final int TICK_INTERVAL_MS = 1000;
    /** card names — flip between actual game and pause without stacking a million JPanels */
    private static final String CARD_GAMEPLAY = "gameplay";
    private static final String CARD_PAUSE = "pause";
    /** Unified size for Score, WPM, and the Health title so the top bar reads as one row. */
    private static final float TOP_BAR_LABEL_PT = 15f;

    private JLabel scoreLabel;
    private JLabel wpmLabel;
    /** Personal best on the current level (saved when logged in). */
    private JLabel levelBestLabel;
    private JPanel healthPanel;
    private JPanel healthHeartsRow;
    private JLabel stageLabel;
    private JLabel orderWordLabel;
    private JLabel typedWordLabel;
    private JLabel sceneHintLabel;
    private JLabel sceneImageLabel;
    private Image fullSizeScene;
    private String scenePathLoaded;
    private JLabel timerLabel;
    private JProgressBar timerBar;
    private SecondaryButton freezePowerUpButton;
    private SecondaryButton autoCleanPowerUpButton;

    private GameSession gameSession;
    private Timer gameLoopTimer;
    private boolean sessionFinished;
    private boolean paused;
    private KeyAdapter gameplayKeyListener;
    private JPanel pauseOverlay;
    private JPanel gameplayLayerPanel;
    private JPanel gameAndPauseStack;
    private CardLayout gamePauseLayout;

    private int currentCustomer = 1;
    private int currentStage = 1;
    private int lastStageSnap = 0;

    private static final String IMAGE_HEART = "assets/images/gameplay/heart.png";
    /** Max hearts (GameSession) — fixed slots so icons spread across the health bar. */
    private static final int MAX_HEART_SLOTS = 3;
    private static final int HEART_ICON_PX = 34;

    /**
     * @param screenManager passed to {@link BaseScreen}
     */
    public GameplayScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    /**
     * lays out gameplay stack ({@link CardLayout} between live game and pause overlay) inside the screen
     */
    @Override
    protected void initializeUI() {
        setLayout(new BorderLayout());

        gameplayLayerPanel = new JPanel(new BorderLayout(UITheme.SPACE_MD, UITheme.SPACE_MD));
        gameplayLayerPanel.setOpaque(false);
        gameplayLayerPanel.setFocusable(true);
        gameplayLayerPanel.add(buildTopBar(), BorderLayout.NORTH);
        gameplayLayerPanel.add(buildCenterArea(), BorderLayout.CENTER);
        gameplayLayerPanel.add(buildBottomArea(), BorderLayout.SOUTH);

        pauseOverlay = buildPauseOverlay();

        gamePauseLayout = new CardLayout();
        gameAndPauseStack = new JPanel(gamePauseLayout);
        gameAndPauseStack.add(gameplayLayerPanel, CARD_GAMEPLAY);
        gameAndPauseStack.add(pauseOverlay, CARD_PAUSE);
        gamePauseLayout.show(gameAndPauseStack, CARD_GAMEPLAY);

        add(gameAndPauseStack, BorderLayout.CENTER);
    }

    /**
     * starts a fresh {@link GameSession} and attaches the keyboard listener so typing hits the game
     */
    @Override
    public void onShow() {
        startNewSession();
        attachGameplayKeyListener();
    }

    /**
     * stops timers and keys, stops BGM through the session audio manager
     */
    @Override
    public void onHide() {
        stopGameLoop();
        detachGameplayKeyListener();
        if (gameSession != null) {
            gameSession.getAudioManager().playSFX(AudioManager.BGM_STOP);
        }
    }

    /**
     * top row: best score, score, wpm, health strip, pause button ({@link GridBagLayout})
     *
     * @return bar panel for {@link BorderLayout#NORTH}
     */
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);

        Font statTileFont = UITheme.FONT_BODY.deriveFont(Font.BOLD, TOP_BAR_LABEL_PT);
        scoreLabel = makeTileLabel("Score: 0", statTileFont);
        wpmLabel = makeTileLabel("WPM: 0", statTileFont);
        healthPanel = createHealthPanel();
        levelBestLabel = makeTileLabel("Best Score: —", statTileFont);
        levelBestLabel.setToolTipText("Your saved best score on this level (log in to keep records).");

        SecondaryButton pauseButton = new SecondaryButton("Pause");
        pauseButton.setFocusable(false);
        pauseButton.addActionListener(withUiClick(e -> togglePause()));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, UITheme.SPACE_SM);

        c.gridx = 0;
        c.weightx = 1;
        topBar.add(levelBestLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        topBar.add(scoreLabel, c);

        c.gridx = 2;
        c.weightx = 1;
        topBar.add(wpmLabel, c);

        c.gridx = 3;
        c.weightx = 1;
        topBar.add(healthPanel, c);

        c.gridx = 4;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        topBar.add(pauseButton, c);

        return topBar;
    }

    /**
     * middle of the screen: scene image above typing readout
     *
     * @return center panel
     */
    private JPanel buildCenterArea() {
        JPanel center = new JPanel(new BorderLayout(UITheme.SPACE_MD, UITheme.SPACE_MD));
        center.setOpaque(false);
        center.add(buildScenePanel(), BorderLayout.CENTER);
        center.add(buildTypingPanel(), BorderLayout.SOUTH);
        return center;
    }

    /**
     * hint label + big {@link JLabel} for PNG; resize listener re-runs scaling when the window changes
     *
     * GENERATED WITH AI
     */
    private JPanel buildScenePanel() {
        JPanel scene = new JPanel(new BorderLayout(0, 8));
        scene.setBackground(new Color(229, 234, 243));
        scene.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 223), 1));

        sceneHintLabel = new JLabel("Type the word shown below.", SwingConstants.CENTER);
        sceneHintLabel.setFont(UITheme.FONT_BODY.deriveFont(14f));
        sceneHintLabel.setForeground(UITheme.TEXT_SECONDARY);

        sceneImageLabel = new JLabel("", SwingConstants.CENTER);
        sceneImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        sceneImageLabel.setOpaque(true);
        sceneImageLabel.setBackground(new Color(229, 234, 243));
        sceneImageLabel.addComponentListener(new ComponentAdapter() {
            /** keeps scene png fitted when the label resizes */
            @Override
            public void componentResized(ComponentEvent e) {
                fitSceneImageToLabel();
            }
        });

        scene.add(sceneHintLabel, BorderLayout.NORTH);
        scene.add(sceneImageLabel, BorderLayout.CENTER);
        return scene;
    }

    /**
     * “Typing” box: stage name, HTML word line, typed-so-far row
     *
     * @return panel for south of {@link #buildCenterArea()}
     */
    private JPanel buildTypingPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 215), 1),
                BorderFactory.createEmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_MD, UITheme.SPACE_SM, UITheme.SPACE_MD)
        ));
        panel.setBackground(UITheme.BG_PANEL);

        JLabel typingTitle = new JLabel("Typing", SwingConstants.CENTER);
        typingTitle.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        typingTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel lines = new JPanel(new GridLayout(2, 1, 6, 6));
        lines.setOpaque(false);
        stageLabel = makeInfoLabel("Step: Order");
        orderWordLabel = makeInfoLabel("Current Word: -");
        orderWordLabel.setFont(UITheme.FONT_BODY.deriveFont(22f));
        lines.add(stageLabel);
        lines.add(orderWordLabel);

        JPanel typedRow = new JPanel(new BorderLayout(8, 0));
        typedRow.setOpaque(false);
        JLabel typedCaption = new JLabel("Your typing:");
        typedCaption.setFont(UITheme.FONT_BODY.deriveFont(14f));
        typedCaption.setForeground(UITheme.TEXT_SECONDARY);
        typedWordLabel = new JLabel(" ");
        typedWordLabel.setFont(UITheme.FONT_BODY.deriveFont(20f));
        typedWordLabel.setOpaque(true);
        typedWordLabel.setBackground(new Color(248, 250, 252));
        typedWordLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 215), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        typedWordLabel.setHorizontalAlignment(SwingConstants.LEFT);
        typedRow.add(typedCaption, BorderLayout.WEST);
        typedRow.add(typedWordLabel, BorderLayout.CENTER);

        panel.add(typingTitle, BorderLayout.NORTH);
        panel.add(lines, BorderLayout.CENTER);
        panel.add(typedRow, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * accent timer strip plus freeze / auto-clean buttons
     *
     * @return panel for {@link BorderLayout#SOUTH} of gameplay layer
     */
    private JPanel buildBottomArea() {
        JPanel bottom = new JPanel(new BorderLayout(UITheme.SPACE_SM, UITheme.SPACE_SM));
        bottom.setOpaque(false);

        JPanel timerBox = new JPanel(new BorderLayout(0, 8));
        timerBox.setOpaque(true);
        timerBox.setBackground(UITheme.ACCENT);
        timerBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_HOVER, 1),
                BorderFactory.createEmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_MD, UITheme.SPACE_SM, UITheme.SPACE_MD)
        ));

        timerLabel = new JLabel("Time Remaining: 0s", SwingConstants.CENTER);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD, 15f));

        timerBar = new JProgressBar(0, 100);
        timerBar.setValue(100);
        timerBar.setStringPainted(false);
        timerBar.setBorderPainted(false);
        timerBar.setOpaque(true);
        timerBar.setForeground(new Color(200, 230, 255));
        timerBar.setBackground(new Color(28, 72, 118));
        timerBar.setPreferredSize(new Dimension(0, 12));

        timerBox.add(timerLabel, BorderLayout.NORTH);
        timerBox.add(timerBar, BorderLayout.SOUTH);
        timerBox.setPreferredSize(new Dimension(0, 52));
        timerBox.setMinimumSize(new Dimension(0, 52));

        bottom.add(timerBox, BorderLayout.NORTH);

        JPanel powerUps = new JPanel(new GridLayout(1, 2, UITheme.SPACE_MD, 0));
        powerUps.setOpaque(false);
        freezePowerUpButton = new SecondaryButton("Freeze time");
        autoCleanPowerUpButton = new SecondaryButton("Auto-clean");
        freezePowerUpButton.setFocusable(false);
        autoCleanPowerUpButton.setFocusable(false);
        freezePowerUpButton.addActionListener(withUiClick(e -> useFreezePowerUp()));
        autoCleanPowerUpButton.addActionListener(withUiClick(e -> useAutoCleanPowerUp()));
        powerUps.add(freezePowerUpButton);
        powerUps.add(autoCleanPowerUpButton);
        bottom.add(powerUps, BorderLayout.SOUTH);

        return bottom;
    }

    /**
     * freeze powerup: calls {@link GameSession#freezeAllStations(int)} and tints the button so you know you used it
     */
    private void useFreezePowerUp() {
        if (gameSession == null || sessionFinished || paused) {
            return;
        }
        if (gameSession.isGameOver()) {
            return;
        }
        gameSession.freezeAllStations(5000);
        markPowerUpButtonBlue(freezePowerUpButton);
    }

    /**
     * auto-clean: completes current word via {@link GameSession#autoCompleteCurrentWord(int)}, refreshes UI,
     * ends session if that triggered game over
     */
    private void useAutoCleanPowerUp() {
        if (gameSession == null || sessionFinished || paused) {
            return;
        }
        if (gameSession.isGameOver()) {
            return;
        }
        gameSession.autoCompleteCurrentWord(1);
        markPowerUpButtonBlue(autoCleanPowerUpButton);
        refreshView();
        if (gameSession.isGameOver()) {
            finishSession();
        }
    }

    /**
     * visual feedback after using a powerup (accent fill)
     *
     * @param button which powerup row button, or null no-op
     */
    private void markPowerUpButtonBlue(SecondaryButton button) {
        if (button == null) {
            return;
        }
        button.setBackground(UITheme.ACCENT);
        button.setForeground(Color.WHITE);
        button.repaint();
    }

    /** puts freeze / auto-clean back to default {@link UITheme#SECONDARY_BUTTON} colors for a new run */
    private void resetPowerUpButtonStyles() {
        if (freezePowerUpButton != null) {
            freezePowerUpButton.setBackground(UITheme.SECONDARY_BUTTON);
            freezePowerUpButton.setForeground(UITheme.TEXT_PRIMARY);
        }
        if (autoCleanPowerUpButton != null) {
            autoCleanPowerUpButton.setBackground(UITheme.SECONDARY_BUTTON);
            autoCleanPowerUpButton.setForeground(UITheme.TEXT_PRIMARY);
        }
    }

    /**
     * small bordered stat tile used in the top bar
     *
     * @param text initial label text
     * @param font usually bold body at {@link #TOP_BAR_LABEL_PT}
     * @return the configured label
     */
    private JLabel makeTileLabel(String text, Font font) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(240, 243, 249));
        label.setBorder(BorderFactory.createLineBorder(new Color(200, 205, 215), 1));
        label.setForeground(UITheme.TEXT_PRIMARY);
        label.setFont(font);
        return label;
    }

    /**
     * centered line for stage / word area (18pt body)
     *
     * @param text starting text
     * @return the configured label
     */
    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(UITheme.TEXT_PRIMARY);
        label.setFont(UITheme.FONT_BODY.deriveFont(18f));
        return label;
    }

    /**
     * maps {@link Level} word difficulty to {@link AudioManager} BGM tag
     *
     * @param level current level or null → easy track
     * @return one of {@link AudioManager#BGM_EASY} / {@link AudioManager#BGM_MEDIUM} / {@link AudioManager#BGM_HARD}
     */
    private static String bgmEventForLevel(Level level) {
        if (level == null) {
            return AudioManager.BGM_EASY;
        }
        switch (level.getWordDifficulty()) {
            case EASY:
                return AudioManager.BGM_EASY;
            case MEDIUM:
                return AudioManager.BGM_MEDIUM;
            default:
                return AudioManager.BGM_HARD;
        }
    }

    /**
     * builds {@link GameSession} from {@link ScreenManager#getSelectedGameplayLevel()} (ice cream default),
     * resets scene indices, BGM, powerup colors, shows gameplay card, starts {@link #startGameLoop()}
     */
    private void startNewSession() {
        sessionFinished = false;
        paused = false;

        Level level = screenManager.getSelectedGameplayLevel();
        if (level == null) {
            level = Level.createIceCreamShop();
        }
        gameSession = new GameSession(level);
        gameSession.applyAudioPreferences(screenManager.getMasterVolume(), screenManager.isMuted());
        gameSession.getAudioManager().playSFX(bgmEventForLevel(level));
        currentCustomer = 1;
        currentStage = 1;
        lastStageSnap = 0;
        fullSizeScene = null;
        scenePathLoaded = null;
        showGameplayCard();
        resetPowerUpButtonStyles();
        refreshView();
        startGameLoop();
    }

    /** one {@link javax.swing.Timer} tick per second driving {@link #onTick()} */
    private void startGameLoop() {
        stopGameLoop();
        gameLoopTimer = new Timer(TICK_INTERVAL_MS, new ActionListener() {
            /** once-per-second game tick */
            @Override
            public void actionPerformed(ActionEvent e) {
                onTick();
            }
        });
        gameLoopTimer.start();
    }

    /** stops and drops the timer reference */
    private void stopGameLoop() {
        if (gameLoopTimer != null) {
            gameLoopTimer.stop();
            gameLoopTimer = null;
        }
    }

    /**
     * each second: add active play time, run {@link GameSession#updateOrderTimer()}, refresh UI,
     * {@link #finishSession()} if session ended
     */
    private void onTick() {
        if (gameSession == null || sessionFinished) {
            return;
        }
        if (paused) {
            return;
        }
        gameSession.addActivePlayMillis(TICK_INTERVAL_MS);
        gameSession.updateOrderTimer();
        refreshView();

        if (gameSession.isGameOver()) {
            finishSession();
        }
    }

    /**
     * forwards printable keys to {@link GameSession#handleKeystroke(char)} (lower-cased), ignores control chars
     *
     * @param typedChar from {@code keyTyped}
     */
    private void onTypedChar(char typedChar) {
        if (gameSession == null || sessionFinished || gameSession.isGameOver() || paused) {
            return;
        }
        if (Character.isISOControl(typedChar)) {
            return;
        }

        gameSession.handleKeystroke(Character.toLowerCase(typedChar));

        refreshView();
        if (gameSession.isGameOver()) {
            finishSession();
        }
    }

    /** flips {@link #gamePauseLayout} to {@link #CARD_GAMEPLAY} */
    private void showGameplayCard() {
        if (gamePauseLayout != null && gameAndPauseStack != null) {
            gamePauseLayout.show(gameAndPauseStack, CARD_GAMEPLAY);
        }
    }

    /** flips {@link #gamePauseLayout} to {@link #CARD_PAUSE} */
    private void showPauseCard() {
        if (gamePauseLayout != null && gameAndPauseStack != null) {
            gamePauseLayout.show(gameAndPauseStack, CARD_PAUSE);
        }
    }

    /**
     * pause: stop timer, BGM pause sfx, show pause card, focus overlay — unpause does the opposite
     */
    private void togglePause() {
        if (gameLoopTimer == null) {
            return;
        }
        if (!paused) {
            gameLoopTimer.stop();
            paused = true;
            if (gameSession != null) {
                gameSession.getAudioManager().playSFX(AudioManager.BGM_PAUSE);
            }
            showPauseCard();
            if (pauseOverlay != null) {
                pauseOverlay.requestFocusInWindow();
            }
            if (sceneHintLabel != null) {
                sceneHintLabel.setText("Paused.");
            }
        } else {
            gameLoopTimer.start();
            paused = false;
            if (gameSession != null) {
                gameSession.getAudioManager().playSFX(AudioManager.BGM_RESUME);
            }
            showGameplayCard();
            gameplayLayerPanel.requestFocusInWindow();
            if (sceneHintLabel != null) {
                sceneHintLabel.setText("Type the word shown below.");
            }
        }
    }

    /**
     * first time only: stop loop, pack {@link ScreenManager.GameResult}, optionally {@link ScreenManager#persistSessionStats},
     * push results screen
     */
    private void finishSession() {
        if (gameSession == null || sessionFinished) {
            return;
        }
        sessionFinished = true;
        stopGameLoop();

        long activeMs = gameSession.getActivePlayMillis();
        int elapsedSeconds = (int) ((activeMs + 999L) / 1000L);
        int attempted = gameSession.getKeystrokesAttempted();
        int correct = gameSession.getKeystrokesCorrect();
        int errors = Math.max(0, attempted - correct);
        ScreenManager.GameResult result = new ScreenManager.GameResult(
                gameSession.getPlayerScore(),
                gameSession.getRemainingHearts(),
                gameSession.isLevelComplete(),
                gameSession.getLevel().getLevelName(),
                calculateWpm(),
                calculateAccuracy(),
                elapsedSeconds,
                errors,
                attempted,
                correct
        );
        boolean persisted = false;
        if (screenManager.isLoggedIn()) {
            screenManager.persistSessionStats(gameSession);
            persisted = true;
        }
        result.setPersistedToProfile(persisted);
        screenManager.setLatestGameResult(result);
        screenManager.showScreen(ScreenManager.RESULTS);
    }

    /**
     * copies live {@link GameSession} fields into HUD labels, timer bar, hearts, and {@link #updateSceneImage()}
     */
    private void refreshView() {
        if (gameSession == null) {
            return;
        }

        scoreLabel.setText("Score: " + gameSession.getPlayerScore());
        wpmLabel.setText("WPM: " + calculateWpm());
        refreshLevelBestLabel();
        updateHealthPanel(gameSession.getRemainingHearts());
        stageLabel.setText("Step: " + gameSession.getCurrentStageName());
        orderWordLabel.setText(gameSession.getCurrentWordHtml());
        typedWordLabel.setText(gameSession.getCurrentTypedText().isEmpty() ? " " : gameSession.getCurrentTypedText());
        if (sceneHintLabel != null && !paused) {
            sceneHintLabel.setText("Type the word shown below.");
        }
        timerBar.setValue(gameSession.getLevelTimePercent());
        if (timerLabel != null) {
            timerLabel.setText("Time Remaining: " + gameSession.getRemainingLevelTimeSeconds() + "s");
        }
        updateSceneImage();
    }

    /** updates {@link #levelBestLabel} from {@link PlayerAccount#getBestScoreOnLevel(String)} when logged in */
    private void refreshLevelBestLabel() {
        if (levelBestLabel == null || gameSession == null) {
            return;
        }
        if (!screenManager.isLoggedIn()) {
            levelBestLabel.setText("Best Score: —");
            return;
        }
        PlayerAccount acc = screenManager.getAccountManager().findAccount(screenManager.getLoggedInUsername());
        if (acc == null) {
            levelBestLabel.setText("Best Score: —");
            return;
        }
        int best = acc.getBestScoreOnLevel(gameSession.getLevel().getLevelName());
        levelBestLabel.setText("Best Score: " + best);
    }

    /** rounded {@link GameSession#getWpm()} for display, 0 if no session */
    private int calculateWpm() {
        if (gameSession == null) {
            return 0;
        }
        return (int) Math.round(gameSession.getWpm());
    }

    /** rounded {@link GameSession#getAccuracy()} percent for display */
    private int calculateAccuracy() {
        if (gameSession == null) {
            return 0;
        }
        return (int) Math.round(gameSession.getAccuracy());
    }

    /**
     * pink-bordered strip with “Health” title and the row where {@link #updateHealthPanel(int)} drops hearts
     *
     * @return panel for the top bar
     */
    private JPanel createHealthPanel() {
        JPanel wrap = new JPanel(new BorderLayout(14, 0));
        wrap.setOpaque(true);
        wrap.setBackground(new Color(252, 240, 244));
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 170, 188), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        JLabel healthTitle = new JLabel("Health");
        healthTitle.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD, TOP_BAR_LABEL_PT));
        healthTitle.setForeground(new Color(130, 55, 80));
        wrap.add(healthTitle, BorderLayout.WEST);

        healthHeartsRow = new JPanel(new GridLayout(1, MAX_HEART_SLOTS, 20, 0));
        healthHeartsRow.setOpaque(false);
        wrap.add(healthHeartsRow, BorderLayout.CENTER);

        return wrap;
    }

    /**
     * rebuilds heart icons (png or unicode fallback) for remaining lives
     *
     * @param hearts from {@link GameSession#getRemainingHearts()}, clamped to {@link #MAX_HEART_SLOTS}
     */
    private void updateHealthPanel(int hearts) {
        if (healthHeartsRow == null) {
            return;
        }
        healthHeartsRow.removeAll();
        int remaining = Math.max(0, Math.min(hearts, MAX_HEART_SLOTS));

        ImageIcon baseIcon = loadScaledIcon(IMAGE_HEART, HEART_ICON_PX, HEART_ICON_PX);

        for (int slot = 0; slot < MAX_HEART_SLOTS; slot++) {
            boolean filled = slot < remaining;
            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(false);
            JLabel iconLabel = new JLabel("", SwingConstants.CENTER);
            if (baseIcon != null) {
                if (filled) {
                    iconLabel.setIcon(baseIcon);
                } else {
                    Image dim = GrayFilter.createDisabledImage(baseIcon.getImage());
                    iconLabel.setIcon(new ImageIcon(dim));
                }
            } else {
                iconLabel.setFont(UITheme.FONT_BODY.deriveFont(filled ? 26f : 22f));
                iconLabel.setForeground(filled ? new Color(200, 60, 90) : new Color(210, 175, 185));
                iconLabel.setText("\u2665");
            }
            cell.add(iconLabel, BorderLayout.CENTER);
            healthHeartsRow.add(cell);
        }

        healthHeartsRow.revalidate();
        healthHeartsRow.repaint();
    }

    /**
     * registers {@link KeyAdapter} on {@link #gameplayLayerPanel}: {@code keyTyped} → {@link #onTypedChar(char)},
     * Escape → {@link #togglePause()} when a run is active, then {@link java.awt.Component#requestFocusInWindow()}
     * so keys do not go to buttons
     */
    private void attachGameplayKeyListener() {
        detachGameplayKeyListener();
        gameplayKeyListener = new KeyAdapter() {
            /** escape toggles pause while a run is active */
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (gameSession != null && !sessionFinished && gameLoopTimer != null) {
                        togglePause();
                    }
                }
            }

            /** forwards typed chars into the typing handler */
            @Override
            public void keyTyped(KeyEvent e) {
                onTypedChar(e.getKeyChar());
            }
        };
        gameplayLayerPanel.addKeyListener(gameplayKeyListener);
        // gotta steal focus or typing dies
        gameplayLayerPanel.requestFocusInWindow();
    }

    /** removes {@link #gameplayKeyListener} from the gameplay panel if present */
    private void detachGameplayKeyListener() {
        if (gameplayKeyListener != null) {
            gameplayLayerPanel.removeKeyListener(gameplayKeyListener);
            gameplayKeyListener = null;
        }
    }

    /**
     * full-screen dim via custom {@code paintComponent}, centered card with stats/exit/return/restart,
     * extra Escape handler on the overlay for focus quirks
     *
     * GENERATED WITH AI
     */
    private JPanel buildPauseOverlay() {
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                // cheap modal look
                g.setColor(new Color(0, 0, 0, 175));
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        overlay.setOpaque(false);
        overlay.setFocusable(true);
        overlay.addKeyListener(new KeyAdapter() {
            /** escape on overlay also unpause (focus weirdness workaround) */
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && paused) {
                    togglePause();
                }
            }
        });

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 223), 1),
                BorderFactory.createEmptyBorder(UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM);

        JLabel frostbitesLabel = new JLabel("FROSTBITES", SwingConstants.CENTER);
        frostbitesLabel.setForeground(UITheme.ACCENT);
        frostbitesLabel.setFont(UITheme.FONT_BUTTON.deriveFont(20f));
        gbc.gridy = 0;
        card.add(frostbitesLabel, gbc);

        JLabel pausedLabel = new JLabel("Gameplay Paused", SwingConstants.CENTER);
        pausedLabel.setForeground(UITheme.TEXT_PRIMARY);
        pausedLabel.setFont(UITheme.FONT_SUBTITLE.deriveFont(52f));
        gbc.gridy = 1;
        card.add(pausedLabel, gbc);

        JPanel rowButtons = new JPanel(new GridLayout(1, 2, UITheme.SPACE_SM, 0));
        rowButtons.setOpaque(false);
        SecondaryButton statsButton = new SecondaryButton("See Statistics");
        statsButton.addActionListener(withUiClick(e -> finishSession()));
        SecondaryButton exitButton = new SecondaryButton("Exit");
        exitButton.addActionListener(withUiClick(e -> {
            stopGameLoop();
            paused = false;
            showGameplayCard();
            if (gameSession != null) {
                gameSession.getAudioManager().playSFX(AudioManager.BGM_STOP);
            }
            screenManager.showScreen(ScreenManager.DASHBOARD);
        }));
        rowButtons.add(statsButton);
        rowButtons.add(exitButton);
        gbc.gridy = 2;
        card.add(rowButtons, gbc);

        SecondaryButton returnButton = new SecondaryButton("\u2190  Return");
        returnButton.addActionListener(withUiClick(e -> togglePause()));
        gbc.gridy = 3;
        card.add(returnButton, gbc);

        javax.swing.JButton restartButton = new javax.swing.JButton("Restart Level");
        restartButton.setFont(UITheme.FONT_BODY);
        restartButton.setForeground(UITheme.TEXT_SECONDARY);
        restartButton.setBorderPainted(false);
        restartButton.setContentAreaFilled(false);
        restartButton.setFocusPainted(false);
        restartButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        restartButton.addActionListener(withUiClick(e -> startNewSession()));
        gbc.gridy = 4;
        card.add(restartButton, gbc);

        overlay.add(card);
        return overlay;
    }

    /**
     * keeps {@link #currentCustomer} / {@link #currentStage} in sync with {@link Order}, picks png path,
     * reloads bitmap when path changes, {@link SwingUtilities#invokeLater} → {@link #fitSceneImageToLabel()}
     *
     * GENERATED WITH AI
     */
    private void updateSceneImage() {
        if (gameSession == null || sceneImageLabel == null) {
            return;
        }
        Order order = gameSession.getActiveOrder();
        if (order == null) {
            return;
        }
        int newStage = order.getCurrentStage();
        if (newStage < 1) {
            newStage = 1;
        }
        if (newStage > 3) {
            newStage = 3;
        }
        if (newStage == 1 && lastStageSnap > 1) {
            advanceCustomer();
        } else if (lastStageSnap >= 1 && newStage > lastStageSnap) {
            advanceStage();
        }
        lastStageSnap = newStage;
        currentStage = newStage;

        String path = getBackgroundImage();
        boolean pathChanged = false;
        if (path == null) {
            pathChanged = scenePathLoaded != null;
        } else {
            pathChanged = scenePathLoaded == null || !path.equals(scenePathLoaded);
        }
        if (pathChanged) {
            loadSceneImageBase(path);
            // layout pass first pls
            SwingUtilities.invokeLater(new Runnable() {
                /** image size needs layout pass first */
                @Override
                public void run() {
                    fitSceneImageToLabel();
                }
            });
        }
    }

    /** bumps {@link #currentStage} for scene filenames when the order advances stages */
    private void advanceStage() {
        currentStage = currentStage + 1;
        if (currentStage > 3) {
            currentStage = 3;
        }
    }

    /** wraps customer index 1–3 and resets stage to 1 when a new order starts */
    private void advanceCustomer() {
        currentStage = 1;
        currentCustomer = currentCustomer + 1;
        if (currentCustomer > 3) {
            currentCustomer = 1;
        }
    }

    /**
     * builds filenames like c1_order.png / c2_build.png under the level asset folder, tries fallbacks if missing
     *
     * @return path string (may point at a non-existent file; caller checks)
     */
    private String getBackgroundImage() {
        if (gameSession == null) {
            return null;
        }
        String base = gameSession.getLevel().getGameplayAssetDirectory();
        int stage = currentStage;
        if (stage < 1) {
            stage = 1;
        } else if (stage > 3) {
            stage = 3;
        }
        String stageFile;
        if (stage == 2) {
            stageFile = "build";
        } else if (stage == 3) {
            stageFile = "serve";
        } else {
            stageFile = "order";
        }
        int cust = currentCustomer;
        if (cust < 1) {
            cust = 1;
        } else if (cust > 3) {
            cust = 3;
        }
        String primary = base + "c" + cust + "_" + stageFile + ".png";
        if (new File(primary).isFile()) {
            return primary;
        }
        if (cust == 3) {
            String fallbackC2 = base + "c2_" + stageFile + ".png";
            if (new File(fallbackC2).isFile()) {
                return fallbackC2;
            }
        }
        String fallbackC1 = base + "c1_" + stageFile + ".png";
        if (new File(fallbackC1).isFile()) {
            return fallbackC1;
        }
        return primary;
    }

    /**
     * stores {@link #fullSizeScene} and {@link #scenePathLoaded} so ticks do not reload the same file
     *
     * @param imagePath filesystem path or null to clear
     */
    private void loadSceneImageBase(String imagePath) {
        scenePathLoaded = imagePath;
        fullSizeScene = null;
        if (imagePath == null) {
            return;
        }
        File file = new File(imagePath);
        if (!file.isFile()) {
            return;
        }
        fullSizeScene = new ImageIcon(imagePath).getImage();
    }

    /**
     * contain-scale {@link #fullSizeScene} into {@link #sceneImageLabel} using min width and height ratios
     *
     * GENERATED WITH AI
     */
    private void fitSceneImageToLabel() {
        if (sceneImageLabel == null) {
            return;
        }
        if (fullSizeScene == null) {
            sceneImageLabel.setIcon(null);
            sceneImageLabel.setText("Scene");
            return;
        }
        int lw = sceneImageLabel.getWidth();
        int lh = sceneImageLabel.getHeight();
        if (lw < 2 || lh < 2) {
            return;
        }
        int iw = fullSizeScene.getWidth(sceneImageLabel);
        int ih = fullSizeScene.getHeight(sceneImageLabel);
        if (iw <= 0 || ih <= 0) {
            return;
        }
        // smaller ratio = whole pic visible
        double scale = Math.min(lw / (double) iw, lh / (double) ih);
        int newW = Math.max(1, (int) Math.round(iw * scale));
        int newH = Math.max(1, (int) Math.round(ih * scale));
        Image scaled = fullSizeScene.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        sceneImageLabel.setIcon(new ImageIcon(scaled));
        sceneImageLabel.setText("");
    }

    /**
     * loads image from disk for heart icons, scales to width by height pixels, null if file not there
     *
     * @param imagePath path under project or cwd
     * @param width target width in pixels
     * @param height target height in pixels
     * @return scaled icon or null
     */
    private ImageIcon loadScaledIcon(String imagePath, int width, int height) {
        File file = new File(imagePath);
        if (!file.exists()) {
            return null;
        }
        ImageIcon icon = new ImageIcon(imagePath);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
