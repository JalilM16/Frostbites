package com.frostbites.ui.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JOptionPane;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import backend.PlayerAccount;

import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.ContentPanel;
import com.frostbites.ui.components.PrimaryButton;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.components.TitleLabel;
import com.frostbites.ui.theme.UITheme;

/**
 * read-only leaderboard table sorted by global high score, with refresh and navigation back to dashboard or main menu
 */
public class HighScoresScreen extends BaseScreen {
    private DefaultTableModel tableModel;

    /**
     * @param screenManager passed to {@link BaseScreen}
     */
    public HighScoresScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    /**
     * builds {@link ContentPanel} with title, non-editable {@link JTable} in {@link JScrollPane}, and action row
     * (refresh, back to dashboard with login check, main menu) then runs first {@link #refreshLeaderboard()}
     */
    @Override
    protected void initializeUI() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setLayout(new BorderLayout(UITheme.SPACE_MD, UITheme.SPACE_MD));

        TitleLabel title = new TitleLabel("Leaderboard");
        contentPanel.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[] { "Rank", "Username", "High Score", "Best WPM", "Best Acc (%)", "Games" },
            0
        ) {
            /** leaderboard is read-only */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(UITheme.FONT_BODY);
        table.getTableHeader().setFont(UITheme.FONT_BUTTON);

        JScrollPane scrollPane = new JScrollPane(table);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_SM, 0));
        actions.setOpaque(false);

        PrimaryButton refreshButton = new PrimaryButton("Refresh");
        refreshButton.addActionListener(withUiClick(e -> refreshLeaderboard()));
        actions.add(refreshButton);

        SecondaryButton levelSelectButton = new SecondaryButton("Back to Dashboard");
        levelSelectButton.addActionListener(withUiClick(e -> {
            if (!screenManager.isLoggedIn()) {
                JOptionPane.showMessageDialog(
                    this,
                    "You must be logged in to access the dashboard.\nPlease log in first.",
                    "Not Logged In",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            screenManager.showScreen(ScreenManager.DASHBOARD);
        }));
        actions.add(levelSelectButton);

        SecondaryButton backButton = new SecondaryButton("Back to Main Menu");
        backButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.MAIN_MENU)));
        actions.add(backButton);

        contentPanel.add(actions, BorderLayout.SOUTH);

        wrapper.add(contentPanel, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        refreshLeaderboard();
    }

    /**
     * reloads rows whenever the screen becomes visible so scores stay current
     */
    @Override
    public void onShow() {
        refreshLeaderboard();
    }

    /**
     * clears the model then fills from {@link backend.AccountManager#getLeaderboardByHighScore()} with rank, username,
     * high score, best WPM, best accuracy, games played (skips blank usernames)
     */
    private void refreshLeaderboard() {
        tableModel.setRowCount(0);
        List<PlayerAccount> leaderboard = screenManager.getAccountManager().getLeaderboardByHighScore();
        int rank = 1;
        for (PlayerAccount account : leaderboard) {
            if (account == null) continue;
            String username = account.getUsername();
            if (username == null || username.trim().isEmpty()) continue;

            double bestWpm = account.getStats().getBestWpm();
            double bestAcc = account.getStats().getBestAccuracyPct();

            tableModel.addRow(new Object[] {
                rank++,
                username,
                account.getHighScore(),
                String.format("%.1f", bestWpm),
                String.format("%.1f", bestAcc),
                account.getStats().getGamesPlayed()
            });
        }
    }
}
