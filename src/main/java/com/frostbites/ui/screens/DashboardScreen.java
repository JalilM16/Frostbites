package com.frostbites.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import backend.AccountManager;
import backend.PlayerAccount;
import backend.PlayerStats;

import com.frostbites.audio.AudioManager;
import com.frostbites.game.Level;
import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.theme.UITheme;

/**
 * post-login home: welcome row, stat tiles on the left, level grid on the right, logout
 */
public class DashboardScreen extends BaseScreen {
    private JLabel playerNameLabel;
    private JLabel statAvgWpmValue;
    private JLabel statPeakWpmValue;
    private JLabel statAccuracyValue;
    private JLabel statErrorCountValue;
    private JLabel statWordsTypedValue;
    private JLabel statHighScoreValue;
    private JLabel statTotalTimeValue;
    private JLabel statHighestLevelValue;
    private JPanel levelsGrid;

    /**
     * wires {@link BaseScreen} then {@link #buildScreen()} so {@link #initializeUI()} runs
     *
     * @param screenManager same as {@link BaseScreen}
     */
    public DashboardScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    /**
     * builds header (welcome + logout), two-column body with stats panel and levels panel
     */
    @Override
    protected void initializeUI() {
        setLayout(new BorderLayout(UITheme.SPACE_MD, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.SECONDARY_BUTTON);
        header.setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_MD, UITheme.SPACE_SM, UITheme.SPACE_MD));

        playerNameLabel = new JLabel("Welcome, Player Name");
        playerNameLabel.setFont(UITheme.FONT_BODY);
        header.add(playerNameLabel, BorderLayout.WEST);

        SecondaryButton logoutButton = new SecondaryButton("Log Out");
        logoutButton.addActionListener(withUiClick(e -> {
            screenManager.logout();
            screenManager.showScreen(ScreenManager.MAIN_MENU);
        }));
        header.add(logoutButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        int gapUnderHeader = UITheme.SPACE_MD + 10;
        int gapTitleToStuff = UITheme.SPACE_MD + 10;

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(gapUnderHeader, 0, 0, 0));

        JPanel twoCols = new JPanel(new GridLayout(1, 2, UITheme.SPACE_MD, 0));
        twoCols.setOpaque(false);

        JPanel leftCol = new JPanel(new BorderLayout(0, gapTitleToStuff));
        leftCol.setOpaque(false);
        JLabel statsHeading = new JLabel("Player Stats");
        statsHeading.setFont(UITheme.FONT_SUBTITLE.deriveFont(28f));
        statsHeading.setForeground(UITheme.TEXT_PRIMARY);
        JPanel leftTitleRow = new JPanel(new BorderLayout());
        leftTitleRow.setOpaque(false);
        leftTitleRow.setBorder(new EmptyBorder(0, UITheme.SPACE_SM, 0, 0));
        leftTitleRow.add(statsHeading, BorderLayout.WEST);
        leftCol.add(leftTitleRow, BorderLayout.NORTH);
        leftCol.add(buildStatsPanel(), BorderLayout.CENTER);

        JPanel rightCol = new JPanel(new BorderLayout(0, gapTitleToStuff));
        rightCol.setOpaque(false);
        JLabel levelHeading = new JLabel("Select Level");
        levelHeading.setFont(UITheme.FONT_SUBTITLE.deriveFont(28f));
        levelHeading.setForeground(UITheme.TEXT_PRIMARY);
        JPanel rightTitleRow = new JPanel(new BorderLayout());
        rightTitleRow.setOpaque(false);
        rightTitleRow.setBorder(new EmptyBorder(0, UITheme.SPACE_MD, 0, 0));
        rightTitleRow.add(levelHeading, BorderLayout.WEST);
        rightCol.add(rightTitleRow, BorderLayout.NORTH);
        rightCol.add(buildLevelsPanel(), BorderLayout.CENTER);

        twoCols.add(leftCol);
        twoCols.add(rightCol);
        body.add(twoCols, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    /**
     * whenever we land here refresh the name label, pull stats from {@link AccountManager}, rebuild level unlock grid
     */
    @Override
    public void onShow() {
        String username = screenManager.getLoggedInUsername();
        if (username == null || username.isBlank()) {
            playerNameLabel.setText("Welcome, Player");
        } else {
            playerNameLabel.setText("Welcome, " + username);
        }
        refreshStatsFromData();
        refreshLevelsGrid();
    }

    /**
     * fills the stat value labels from the logged-in {@link PlayerAccount} or clears them if nobody logged in
     */
    private void refreshStatsFromData() {
        if (statAvgWpmValue == null) {
            return;
        }
        String username = screenManager.getLoggedInUsername();
        if (username == null || username.isBlank()) {
            clearStatLabels();
            return;
        }

        AccountManager accounts = screenManager.getAccountManager();
        PlayerAccount acc = accounts.findAccount(username);
        if (acc == null) {
            clearStatLabels();
            return;
        }

        PlayerStats st = acc.getStats();
        int games = st.getGamesPlayed();
        int typed = st.getTotalTypedChars();
        boolean hasHistory = games > 0 || typed > 0;

        statHighScoreValue.setText(String.valueOf(acc.getHighScore()));
        statTotalTimeValue.setText(formatTotalTimePlayed(st));

        if (!hasHistory) {
            statAvgWpmValue.setText("No Data");
            statPeakWpmValue.setText("0");
            statAccuracyValue.setText("No Data");
            statErrorCountValue.setText("0");
            statWordsTypedValue.setText("0");
            statHighestLevelValue.setText("No Data");
            return;
        }

        statAvgWpmValue.setText(String.format("%.1f", st.getAverageWpm()));
        statPeakWpmValue.setText(String.format("%.1f", st.getPeakWpm()));
        statAccuracyValue.setText(String.format("%.1f%%", st.getAccuracyPercentage()));
        statErrorCountValue.setText(String.valueOf(st.getErrorCount()));
        statWordsTypedValue.setText(String.valueOf(st.getWordsTyped()));

        String hi = acc.getHighestLevelReached();
        if (hi == null || hi.isBlank()) {
            statHighestLevelValue.setText("No Data");
        } else {
            statHighestLevelValue.setText(hi);
        }
    }

    /**
     * shows total play time as minutes with one decimal, or {@code 0.0 min} if nothing recorded
     *
     * @param st stats bag from the account
     * @return short string for the label
     */
    private static String formatTotalTimePlayed(PlayerStats st) {
        double minutes = st.getTotalTimeMinutes();
        if (minutes <= 0 && st.getTotalTimeSeconds() <= 0) {
            return "0.0 min";
        }
        return String.format("%.1f min", minutes);
    }

    /**
     * sets every stat value to {@code No Data} (or 0 where we did that for empty history) when user missing
     */
    private void clearStatLabels() {
        statAvgWpmValue.setText("No Data");
        statPeakWpmValue.setText("No Data");
        statAccuracyValue.setText("No Data");
        statErrorCountValue.setText("No Data");
        statWordsTypedValue.setText("No Data");
        if (statHighScoreValue != null) {
            statHighScoreValue.setText("No Data");
        }
        if (statTotalTimeValue != null) {
            statTotalTimeValue.setText("No Data");
        }
        statHighestLevelValue.setText("No Data");
    }

    /**
     * left column: stacked stat rows plus high scores + main menu buttons
     *
     * @return panel to drop in the west side of the dashboard body
     */
    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, UITheme.SPACE_SM, 0, UITheme.SPACE_SM));

        JPanel rows = new JPanel(new GridLayout(10, 1, 0, UITheme.SPACE_SM));
        rows.setOpaque(false);

        statAvgWpmValue = addStatRow(rows, "Average WPM");
        statPeakWpmValue = addStatRow(rows, "Peak WPM");
        statAccuracyValue = addStatRow(rows, "Accuracy %");
        statErrorCountValue = addStatRow(rows, "Error Count");
        statWordsTypedValue = addStatRow(rows, "Total Words Typed");
        statHighScoreValue = addStatRow(rows, "High Score");
        statTotalTimeValue = addStatRow(rows, "Total Time Played");
        statHighestLevelValue = addStatRow(rows, "Highest Level Reached");

        SecondaryButton highScoresButton = new SecondaryButton("View High Scores");
        highScoresButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.HIGH_SCORES)));
        rows.add(highScoresButton);

        SecondaryButton mainMenuButton = new SecondaryButton("Back to Main Menu");
        mainMenuButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.MAIN_MENU)));
        rows.add(mainMenuButton);

        panel.add(rows, BorderLayout.CENTER);
        return panel;
    }

    /**
     * one row: label cell + value cell, adds to {@code rows} and returns the value label so we can update it later
     *
     * @param rows      vertical grid to append to
     * @param labelText left column caption
     * @return the right-hand {@link JLabel} for that row
     */
    private JLabel addStatRow(JPanel rows, String labelText) {
        JPanel row = new JPanel(new GridLayout(1, 2, UITheme.SPACE_SM, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(470, 38));

        JLabel label = new JLabel(labelText, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setFont(UITheme.FONT_BODY);
        label.setBackground(UITheme.BG_PANEL);
        label.setBorder(BorderFactory.createLineBorder(UITheme.SECONDARY_BUTTON_HOVER));

        JLabel value = new JLabel("—", SwingConstants.CENTER);
        value.setOpaque(true);
        value.setFont(UITheme.FONT_BODY);
        value.setBackground(UITheme.SECONDARY_BUTTON);
        value.setBorder(BorderFactory.createLineBorder(UITheme.SECONDARY_BUTTON_HOVER));

        row.add(label);
        row.add(value);
        rows.add(row);
        return value;
    }

    /**
     * right column wrapper: holds the 3×2 {@link #levelsGrid} with fixed preferred height
     *
     * @return panel for the east side
     */
    private JPanel buildLevelsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        int gap = UITheme.SPACE_SM;
        int rowH = 92;
        int gridH = rowH * 3 + gap * 2;

        levelsGrid = new JPanel(new GridLayout(3, 2, gap, gap));
        levelsGrid.setOpaque(false);
        levelsGrid.setPreferredSize(new Dimension(520, gridH));

        panel.add(levelsGrid, BorderLayout.CENTER);
        return panel;
    }

    /**
     * clears and refills the six slots: real levels 1–3 unlock by {@link PlayerAccount#rankForLevel(String)},
     * rest are “coming soon” locks
     */
    private void refreshLevelsGrid() {
        if (levelsGrid == null) {
            return;
        }
        levelsGrid.removeAll();

        int slotH = 72;
        String[] levelSubtitles = {
            "Ice Cream Shop (Easy)",
            "Busy Mall (Medium)",
            "Mega Dessert Factory (Hard)"
        };

        // Check what the player has beaten to determine unlock state
        int highestRank = 0;
        String username = screenManager.getLoggedInUsername();
        if (username != null && !username.isBlank()) {
            PlayerAccount acc = screenManager.getAccountManager().findAccount(username);
            if (acc != null) {
                highestRank = PlayerAccount.rankForLevel(acc.getHighestLevelReached());
            }
        }

        for (int i = 1; i <= 6; i++) {
            if (i == 1) {
                levelsGrid.add(makePlayableLevelSlot(i, slotH, levelSubtitles[0]));
            } else if (i == 2) {
                if (highestRank >= 1) {
                    levelsGrid.add(makePlayableLevelSlot(i, slotH, levelSubtitles[1]));
                } else {
                    levelsGrid.add(makeLockedLevelSlot(i, slotH, "Complete Level 1 to unlock"));
                }
            } else if (i == 3) {
                if (highestRank >= 2) {
                    levelsGrid.add(makePlayableLevelSlot(i, slotH, levelSubtitles[2]));
                } else {
                    levelsGrid.add(makeLockedLevelSlot(i, slotH, "Complete Level 2 to unlock"));
                }
            } else {
                levelsGrid.add(makeLockedLevelSlot(i, slotH, "coming soon"));
            }
        }

        levelsGrid.revalidate();
        levelsGrid.repaint();
    }

    /**
     * clickable level card: accent colors, subtitle in html so it wraps, centered stack via {@link BoxLayout} +
     * {@link GridBagLayout}, click sets selected {@link Level} and opens gameplay
     *
     * GENERATED WITH AI
     */
    private JPanel makePlayableLevelSlot(int levelNumber, int height, String subtitle) {
        JPanel slot = new JPanel(new BorderLayout(0, 2));
        slot.setOpaque(true);
        slot.setBackground(UITheme.ACCENT);
        slot.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_HOVER, 1));
        slot.setPreferredSize(new Dimension(240, height));
        slot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        slot.addMouseListener(new MouseAdapter() {
            /** picks level for this slot and opens gameplay */
            @Override
            public void mouseClicked(MouseEvent e) {
                screenManager.getMenuAudioManager().playSFX(AudioManager.SFX_UI_CLICK);
                Level level = Level.forDashboardSlot(levelNumber);
                screenManager.setSelectedGameplayLevel(level);
                screenManager.showScreen(ScreenManager.GAMEPLAY);
            }
        });

        JLabel title = new JLabel("Level " + levelNumber, SwingConstants.CENTER);
        title.setFont(UITheme.FONT_BUTTON);
        title.setForeground(UITheme.BG_PANEL);

        JLabel sub = new JLabel(
            "<html><div style='text-align:center;width:220px'>" + subtitle + "</div></html>",
            SwingConstants.CENTER
        );
        sub.setFont(UITheme.FONT_BODY.deriveFont(Font.PLAIN, 11f));
        sub.setForeground(new Color(220, 235, 255));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(title);
        stack.add(Box.createVerticalStrut(3));
        stack.add(sub);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1;
        c.weighty = 1;
        center.add(stack, c);
        slot.add(center, BorderLayout.CENTER);

        return slot;
    }

    /**
     * non-clickable tile: muted gray look, same centered title + message stack as playable slots but no listener
     *
     * @param levelNumber shown as “Level n”
     * @param height      row height hint
     * @param message     lock reason or “coming soon”
     */
    private JPanel makeLockedLevelSlot(int levelNumber, int height, String message) {
        Color bg = new Color(188, 196, 208);
        Color edge = new Color(155, 163, 175);

        JPanel slot = new JPanel(new BorderLayout(0, 2));
        slot.setOpaque(true);
        slot.setBackground(bg);
        slot.setBorder(BorderFactory.createLineBorder(edge, 1));
        slot.setPreferredSize(new Dimension(240, height));
        slot.setCursor(Cursor.getDefaultCursor());

        JLabel title = new JLabel("Level " + levelNumber, SwingConstants.CENTER);
        title.setFont(UITheme.FONT_BUTTON);
        title.setForeground(new Color(95, 100, 110));

        JLabel soon = new JLabel(message, SwingConstants.CENTER);
        soon.setFont(UITheme.FONT_BODY.deriveFont(Font.PLAIN, 11f));
        soon.setForeground(UITheme.TEXT_SECONDARY);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        soon.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(title);
        stack.add(Box.createVerticalStrut(3));
        stack.add(soon);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1;
        c.weighty = 1;
        center.add(stack, c);
        slot.add(center, BorderLayout.CENTER);

        return slot;
    }
}
