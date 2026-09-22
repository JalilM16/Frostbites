package com.frostbites.ui.screens;

import com.frostbites.game.Level;
import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.theme.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * post-game summary: win/lose title, score/WPM/accuracy tiles, next level / restart / dashboard, optional save-to-profile line
 */
public class ResultsScreen extends BaseScreen {
    private JLabel titleLabel;
    private JLabel scoreValueLabel;
    private JLabel wpmValueLabel;
    private JLabel accuracyValueLabel;
    private JLabel persistLabel;
    private SecondaryButton nextLevelButton;
    private JLabel nextLevelComingSoonLabel;

    /**
     * @param screenManager passed to {@link BaseScreen}
     */
    public ResultsScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    /**
     * grid of stat labels, button row (next level + restart), dashboard, persist hint, save-to-leaderboard
     */
    @Override
    protected void initializeUI() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM);

        titleLabel = new JLabel("Level Complete!", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_SUBTITLE.deriveFont(58f));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JPanel stats = new JPanel(new GridLayout(3, 2, UITheme.SPACE_SM, UITheme.SPACE_SM));
        stats.setOpaque(false);
        stats.add(makeStatName("Score"));
        scoreValueLabel = makeStatValue("0");
        stats.add(scoreValueLabel);
        stats.add(makeStatName("Words Per Minute"));
        wpmValueLabel = makeStatValue("0");
        stats.add(wpmValueLabel);
        stats.add(makeStatName("Accuracy"));
        accuracyValueLabel = makeStatValue("0%");
        stats.add(accuracyValueLabel);

        gbc.gridy = 1;
        panel.add(stats, gbc);

        JPanel firstButtons = new JPanel(new GridLayout(1, 2, UITheme.SPACE_SM, 0));
        firstButtons.setOpaque(false);
        JPanel nextRow = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.SPACE_SM, 0));
        nextRow.setOpaque(false);
        nextLevelButton = new SecondaryButton("Next Level");
        nextLevelButton.addActionListener(withUiClick(e -> goToNextLevel()));
        nextLevelComingSoonLabel = new JLabel("Coming soon");
        nextLevelComingSoonLabel.setFont(UITheme.FONT_BODY.deriveFont(16f));
        nextLevelComingSoonLabel.setForeground(UITheme.TEXT_SECONDARY);
        nextLevelComingSoonLabel.setVisible(false);
        nextRow.add(nextLevelButton);
        nextRow.add(nextLevelComingSoonLabel);
        SecondaryButton restartButton = new SecondaryButton("Restart");
        restartButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.GAMEPLAY)));
        firstButtons.add(nextRow);
        firstButtons.add(restartButton);
        gbc.gridy = 2;
        panel.add(firstButtons, gbc);

        SecondaryButton returnButton = new SecondaryButton("Return to Dashboard");
        returnButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.DASHBOARD)));
        gbc.gridy = 3;
        panel.add(returnButton, gbc);

        persistLabel = new JLabel(" ", SwingConstants.CENTER);
        persistLabel.setForeground(UITheme.TEXT_SECONDARY);
        persistLabel.setFont(UITheme.FONT_BODY.deriveFont(16f));
        gbc.gridy = 4;
        panel.add(persistLabel, gbc);

        SecondaryButton saveScoreButton = new SecondaryButton("Save Score to Leaderboard");
        saveScoreButton.addActionListener(withUiClick(e -> onSaveScore()));
        gbc.gridy = 5;
        panel.add(saveScoreButton, gbc);

        wrapper.add(panel);
        add(wrapper, BorderLayout.CENTER);
    }

    /** Fills labels from {@link ScreenManager#getLatestGameResult()} or shows placeholders if none. */
    @Override
    public void onShow() {
        ScreenManager.GameResult result = screenManager.getLatestGameResult();
        if (result == null) {
            titleLabel.setText("Level Complete!");
            scoreValueLabel.setText("0");
            wpmValueLabel.setText("0");
            accuracyValueLabel.setText("0%");
            persistLabel.setText("Play a game first to generate results.");
            if (nextLevelButton != null) {
                nextLevelButton.setEnabled(false);
            }
            if (nextLevelComingSoonLabel != null) {
                nextLevelComingSoonLabel.setVisible(false);
            }
            return;
        }

        if (result.isLevelComplete()) {
            titleLabel.setText("Level Complete!");
        } else {
            titleLabel.setText("Game Over");
        }

        scoreValueLabel.setText(String.valueOf(result.getFinalScore()));
        wpmValueLabel.setText(String.valueOf(result.getWordsPerMinute()));
        accuracyValueLabel.setText(result.getAccuracy() + "%");
        if (result.isPersistedToProfile()) {
            persistLabel.setText("Stats are saved to your profile. Open the leaderboard below.");
        } else {
            persistLabel.setText("Log in from the main menu to record this session on your dashboard.");
        }

        Level completed = Level.fromLevelName(result.getLevelName());
        boolean beatFinalTier = result.isLevelComplete() && completed != null && completed.isHardestLevel();
        nextLevelButton.setEnabled(!beatFinalTier);
        nextLevelComingSoonLabel.setVisible(beatFinalTier);
    }

    /**
     * picks {@link Level#nextAfter(Level)} from last result (or selected level), skips if hardest already beaten
     */
    private void goToNextLevel() {
        ScreenManager.GameResult result = screenManager.getLatestGameResult();
        Level current = null;
        if (result != null) {
            current = Level.fromLevelName(result.getLevelName());
        }
        if (current == null) {
            current = screenManager.getSelectedGameplayLevel();
        }
        if (result != null && result.isLevelComplete() && current != null && current.isHardestLevel()) {
            return;
        }
        Level next = Level.nextAfter(current);
        screenManager.setSelectedGameplayLevel(next);
        screenManager.showScreen(ScreenManager.GAMEPLAY);
    }

    /**
     * if logged in and not already saved, calls {@link backend.AccountManager#recordTypingResult} then opens high scores;
     * uses {@link SwingUtilities#invokeLater} for screen switch after message update
     *
     * GENERATED WITH AI
     */
    private void onSaveScore() {
        ScreenManager.GameResult result = screenManager.getLatestGameResult();
        if (result == null) {
            persistLabel.setText("No completed session to save yet.");
            return;
        }
        if (result.isPersistedToProfile()) {
            persistLabel.setText("Opening leaderboard...");
            SwingUtilities.invokeLater(() -> screenManager.showScreen(ScreenManager.HIGH_SCORES));
            return;
        }
        if (!screenManager.isLoggedIn()) {
            persistLabel.setText("Please log in first to save your score.");
            return;
        }

        String username = screenManager.getLoggedInUsername();
        double wpm = result.getWordsPerMinute();
        double accuracy = result.getAccuracy();
        screenManager.getAccountManager().recordTypingResult(
            username,
            result.getFinalScore(),
            wpm,
            accuracy,
            result.getErrorCount(),
            result.getTimeSeconds(),
            result.getTypedChars(),
            result.getCorrectChars(),
            result.getLevelName(),
            result.isLevelComplete()
        );
        result.setPersistedToProfile(true);
        persistLabel.setText("Saved. Opening leaderboard...");
        SwingUtilities.invokeLater(() -> screenManager.showScreen(ScreenManager.HIGH_SCORES));
    }

    /**
     * left column style cell in the stats grid
     *
     * @param text row caption (Score, WPM, …)
     */
    private JLabel makeStatName(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(242, 245, 250));
        label.setForeground(UITheme.TEXT_SECONDARY);
        label.setFont(UITheme.FONT_BODY.deriveFont(28f));
        label.setPreferredSize(new Dimension(350, 58));
        label.setBorder(BorderFactory.createLineBorder(new Color(190, 196, 208), 1));
        return label;
    }

    /**
     * right column value cell in the stats grid
     *
     * @param text initial placeholder string
     */
    private JLabel makeStatValue(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(225, 230, 238));
        label.setForeground(UITheme.TEXT_PRIMARY);
        label.setFont(UITheme.FONT_BODY.deriveFont(26f));
        label.setPreferredSize(new Dimension(95, 58));
        label.setBorder(BorderFactory.createLineBorder(new Color(190, 196, 208), 1));
        return label;
    }
}
