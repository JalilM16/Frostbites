package com.frostbites.ui.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import backend.PlayerStats;

import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.ContentPanel;
import com.frostbites.ui.components.ErrorLabel;
import com.frostbites.ui.components.FormPasswordField;
import com.frostbites.ui.components.FormTextField;
import com.frostbites.ui.components.PrimaryButton;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.components.TitleLabel;
import com.frostbites.ui.theme.UITheme;

/**
 * admin screen for parents/teachers: PIN first ({@link backend.DataManager} default 1234), then player stats,
 * password/stat resets, new accounts, global high-score reset
 */
public class ManagersOfficeScreen extends BaseScreen {

    // --- Player Management widgets ---
    private JComboBox<String> playerDropdown;
    private JLabel gamesPlayedValue;
    private JLabel bestWpmValue;
    private JLabel bestAccuracyValue;
    private JLabel avgAccuracyValue;
    private JLabel totalErrorsValue;
    private JLabel totalTimeValue;
    private ErrorLabel playerMgmtFeedback;

    // --- Create Account widgets ---
    private FormTextField newUsernameField;
    private FormPasswordField newPasswordField;
    private ErrorLabel createAccountFeedback;

    /**
     * @param screenManager passed to {@link BaseScreen}
     */
    public ManagersOfficeScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    // -------------------------------------------------------------------------
    // Screen construction
    // -------------------------------------------------------------------------

    /**
     * {@link ContentPanel} with title, vertical {@link BoxLayout} sections in a {@link JScrollPane} (transparent chrome),
     * back button fixed in south
     *
     * GENERATED WITH AI
     */
    @Override
    protected void initializeUI() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setLayout(new BorderLayout(UITheme.SPACE_MD, UITheme.SPACE_MD));
        contentPanel.add(new TitleLabel("Manager's Office"), BorderLayout.NORTH);

        // Wrap all sections in a vertical panel inside a scroll pane so the
        // screen doesn't overflow on smaller displays.
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        mainPanel.add(buildPlayerManagementSection());
        mainPanel.add(Box.createVerticalStrut(UITheme.SPACE_XL));
        mainPanel.add(buildCreateAccountSection());
        mainPanel.add(Box.createVerticalStrut(UITheme.SPACE_XL));
        mainPanel.add(buildGlobalActionsSection());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Back button sits outside the scroll area so it is always visible.
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_SM, 0));
        southPanel.setOpaque(false);
        SecondaryButton backButton = new SecondaryButton("Back to Main Menu");
        backButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.MAIN_MENU)));
        southPanel.add(backButton);
        contentPanel.add(southPanel, BorderLayout.SOUTH);

        wrapper.add(contentPanel, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    /**
     * player dropdown + refresh, stat value grid, section feedback, reset password / reset stats buttons
     *
     * @return panel for the scroll stack
     */
    private JPanel buildPlayerManagementSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(UITheme.SPACE_XS, 0, UITheme.SPACE_XS, 0);

        // Section heading
        JLabel heading = new JLabel("Player Management");
        heading.setFont(UITheme.FONT_SUBTITLE);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 0;
        panel.add(heading, gbc);

        // Player selector label
        JLabel selectLabel = new JLabel("Select Player");
        selectLabel.setFont(UITheme.FONT_BODY);
        selectLabel.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 1;
        panel.add(selectLabel, gbc);

        // Dropdown + Refresh in one row
        JPanel dropdownRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACE_SM, 0));
        dropdownRow.setOpaque(false);
        playerDropdown = new JComboBox<>();
        playerDropdown.addActionListener(e -> refreshStats());
        dropdownRow.add(playerDropdown);

        PrimaryButton refreshButton = new PrimaryButton("Refresh List");
        refreshButton.addActionListener(withUiClick(e -> refreshPlayers()));
        dropdownRow.add(refreshButton);

        gbc.gridy = 2;
        panel.add(dropdownRow, gbc);

        // Stats grid
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setOpaque(false);
        GridBagConstraints s = new GridBagConstraints();
        s.gridx = 0;
        s.gridy = 0;
        s.insets = new Insets(UITheme.SPACE_XS, 0, UITheme.SPACE_XS, UITheme.SPACE_MD);
        s.anchor = GridBagConstraints.WEST;

        gamesPlayedValue  = addStatRow(statsPanel, s, "Games Played");
        bestWpmValue      = addStatRow(statsPanel, s, "Best WPM");
        bestAccuracyValue = addStatRow(statsPanel, s, "Best Accuracy (%)");
        avgAccuracyValue  = addStatRow(statsPanel, s, "Average Accuracy (%)");
        totalErrorsValue  = addStatRow(statsPanel, s, "Total Errors");
        totalTimeValue    = addStatRow(statsPanel, s, "Total Time Played (min)");

        gbc.gridy = 3;
        panel.add(statsPanel, gbc);

        // Feedback label for this section
        playerMgmtFeedback = new ErrorLabel();
        playerMgmtFeedback.setVisible(false);
        gbc.gridy = 4;
        panel.add(playerMgmtFeedback, gbc);

        // Action buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACE_SM, 0));
        buttonRow.setOpaque(false);

        PrimaryButton resetPasswordButton = new PrimaryButton("Reset Password");
        resetPasswordButton.addActionListener(withUiClick(e -> onResetPassword()));
        buttonRow.add(resetPasswordButton);

        SecondaryButton resetStatsButton = new SecondaryButton("Reset Statistics");
        resetStatsButton.addActionListener(withUiClick(e -> onResetStats()));
        buttonRow.add(resetStatsButton);

        gbc.gridy = 5;
        panel.add(buttonRow, gbc);

        return panel;
    }

    /**
     * new username/password fields, feedback label, create button wired to {@link #onCreateAccount()}
     *
     * @return panel for the scroll stack
     */
    private JPanel buildCreateAccountSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(UITheme.SPACE_XS, 0, UITheme.SPACE_XS, 0);

        JLabel heading = new JLabel("Create New Account");
        heading.setFont(UITheme.FONT_SUBTITLE);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 0;
        panel.add(heading, gbc);

        JLabel usernameLabel = new JLabel("New Username");
        usernameLabel.setFont(UITheme.FONT_BODY);
        usernameLabel.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 1;
        panel.add(usernameLabel, gbc);

        newUsernameField = new FormTextField();
        gbc.gridy = 2;
        panel.add(newUsernameField, gbc);

        JLabel passwordLabel = new JLabel("New Password");
        passwordLabel.setFont(UITheme.FONT_BODY);
        passwordLabel.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 3;
        panel.add(passwordLabel, gbc);

        newPasswordField = new FormPasswordField();
        gbc.gridy = 4;
        panel.add(newPasswordField, gbc);

        createAccountFeedback = new ErrorLabel();
        createAccountFeedback.setVisible(false);
        gbc.gridy = 5;
        panel.add(createAccountFeedback, gbc);

        PrimaryButton createButton = new PrimaryButton("Create Account");
        createButton.addActionListener(withUiClick(e -> onCreateAccount()));
        gbc.gridy = 6;
        panel.add(createButton, gbc);

        return panel;
    }

    /**
     * description + button that calls {@link #onResetHighScores()}
     *
     * @return panel for the scroll stack
     */
    private JPanel buildGlobalActionsSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(UITheme.SPACE_XS, 0, UITheme.SPACE_XS, 0);

        JLabel heading = new JLabel("Global Actions");
        heading.setFont(UITheme.FONT_SUBTITLE);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 0;
        panel.add(heading, gbc);

        JLabel description = new JLabel("Reset all players' high scores back to zero.");
        description.setFont(UITheme.FONT_BODY);
        description.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = 1;
        panel.add(description, gbc);

        SecondaryButton resetHighScoresButton = new SecondaryButton("Reset High Score Table");
        resetHighScoresButton.addActionListener(withUiClick(e -> onResetHighScores()));
        gbc.gridy = 2;
        panel.add(resetHighScoresButton, gbc);

        return panel;
    }

    // -------------------------------------------------------------------------
    // PIN authentication
    // -------------------------------------------------------------------------

    /**
     * prompts {@link #promptForPin()}; on failure returns to main menu, else {@link #refreshPlayers()} and clears create form
     */
    @Override
    public void onShow() {
        boolean authenticated = promptForPin();
        if (!authenticated) {
            screenManager.showScreen(ScreenManager.MAIN_MENU);
            return;
        }
        refreshPlayers();
        clearCreateAccountForm();
    }

    /**
     * one {@link JOptionPane#showInputDialog} then {@link backend.AccountManager#verifyAdminPin(String)}
     *
     * @return true if PIN ok, false on cancel or mismatch
     */
    private boolean promptForPin() {
        String enteredPin = JOptionPane.showInputDialog(this, "Enter Manager PIN:", "Manager Access", JOptionPane.PLAIN_MESSAGE);
        if (enteredPin == null) {
            return false;
        }
        if (screenManager.getAccountManager().verifyAdminPin(enteredPin)) {
            return true;
        }
        JOptionPane.showMessageDialog(this, "Incorrect PIN. Access denied.", "Access Denied", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // -------------------------------------------------------------------------
    // Action handlers
    // -------------------------------------------------------------------------

    /**
     * confirm dialog with embedded {@link JPasswordField}, then {@link backend.AccountManager#resetPassword}
     *
     * GENERATED WITH AI
     */
    private void onResetPassword() {
        String username = getSelectedUsername();
        if (username == null) {
            showPlayerMgmtFeedback("Please select a player first.");
            return;
        }

        JPasswordField newPasswordField = new JPasswordField();
        int choice = JOptionPane.showConfirmDialog(
                this,
                newPasswordField,
                "Enter new password for \"" + username + "\":",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String newPassword = new String(newPasswordField.getPassword()).trim();
        if (newPassword.isEmpty()) {
            showPlayerMgmtFeedback("Password cannot be blank.");
            return;
        }

        boolean success = screenManager.getAccountManager().resetPassword(username, newPassword);
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Password for \"" + username + "\" has been updated.",
                    "Password Reset",
                    JOptionPane.INFORMATION_MESSAGE
            );
            clearPlayerMgmtFeedback();
        } else {
            showPlayerMgmtFeedback("Failed to reset password. Player not found.");
        }
    }

    /**
     * yes/no confirm then {@link backend.AccountManager#resetStats(String)} and refresh labels
     *
     * GENERATED WITH AI
     */
    private void onResetStats() {
        String username = getSelectedUsername();
        if (username == null) {
            showPlayerMgmtFeedback("Please select a player first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset all statistics for \"" + username + "\"?\n"
                        + "This will also clear their high score. This cannot be undone.",
                "Confirm Reset Statistics",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = screenManager.getAccountManager().resetStats(username);
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Statistics for \"" + username + "\" have been reset.",
                    "Statistics Reset",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshStats();
            clearPlayerMgmtFeedback();
        } else {
            showPlayerMgmtFeedback("Failed to reset statistics. Player not found.");
        }
    }

    /**
     * trims fields, {@link backend.AccountManager#registerPlayer}, shows success or error on {@link #createAccountFeedback}
     */
    private void onCreateAccount() {
        String username = newUsernameField.getText().trim();
        String password = new String(newPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showCreateAccountFeedback("Please enter both a username and a password.");
            return;
        }

        boolean created = screenManager.getAccountManager().registerPlayer(username, password);
        if (!created) {
            showCreateAccountFeedback("Username already taken or invalid. Try a different name.");
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Account for \"" + username + "\" has been created successfully.",
                "Account Created",
                JOptionPane.INFORMATION_MESSAGE
        );
        clearCreateAccountForm();
        refreshPlayers();
    }

    /**
     * confirm then {@link backend.AccountManager#resetAllHighScores()} and {@link #refreshStats()}
     *
     * GENERATED WITH AI
     */
    private void onResetHighScores() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset the high score table?\n"
                        + "All players' high scores will be set to 0. This cannot be undone.",
                "Confirm Reset High Score Table",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        screenManager.getAccountManager().resetAllHighScores();
        JOptionPane.showMessageDialog(
                this,
                "The high score table has been reset.",
                "High Scores Reset",
                JOptionPane.INFORMATION_MESSAGE
        );
        refreshStats();
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * one key/value row in the stats grid; bumps {@code gbc.gridy}
     *
     * @param panel target grid
     * @param gbc   shared constraints (mutated)
     * @param label left column text
     * @return the value label on the right
     */
    private JLabel addStatRow(JPanel panel, GridBagConstraints gbc, String label) {
        JLabel key = new JLabel(label);
        key.setFont(UITheme.FONT_BUTTON);
        key.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridx = 0;
        panel.add(key, gbc);

        JLabel value = new JLabel("-");
        value.setFont(UITheme.FONT_BODY);
        value.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx = 1;
        panel.add(value, gbc);

        gbc.gridy += 1;
        return value;
    }

    /** repopulates {@link #playerDropdown} from {@link backend.AccountManager#getAllUsernames()} then {@link #refreshStats()} */
    private void refreshPlayers() {
        List<String> usernames = screenManager.getAccountManager().getAllUsernames();
        playerDropdown.removeAllItems();
        for (String name : usernames) {
            playerDropdown.addItem(name);
        }
        refreshStats();
    }

    /** fills stat labels from {@link backend.AccountManager#getStatsFor(String)} or dashes if none selected */
    private void refreshStats() {
        String username = getSelectedUsername();
        if (username == null) {
            setAllStats("-");
            return;
        }

        PlayerStats stats = screenManager.getAccountManager().getStatsFor(username);
        if (stats == null) {
            setAllStats("-");
            return;
        }

        gamesPlayedValue.setText(String.valueOf(stats.getGamesPlayed()));
        bestWpmValue.setText(String.format("%.1f", stats.getBestWpm()));
        bestAccuracyValue.setText(String.format("%.1f", stats.getBestAccuracyPct()));
        avgAccuracyValue.setText(String.format("%.1f", stats.getAverageAccuracyPct()));
        totalErrorsValue.setText(String.valueOf(stats.getTotalErrorCount()));
        totalTimeValue.setText(String.format("%.1f", stats.getTotalTimeMinutes()));
    }

    /**
     * @param value placeholder for every stat value label (e.g. "-")
     */
    private void setAllStats(String value) {
        gamesPlayedValue.setText(value);
        bestWpmValue.setText(value);
        bestAccuracyValue.setText(value);
        avgAccuracyValue.setText(value);
        totalErrorsValue.setText(value);
        totalTimeValue.setText(value);
    }

    /**
     * @return dropdown selection string or null
     */
    private String getSelectedUsername() {
        Object selected = playerDropdown.getSelectedItem();
        if (selected == null) {
            return null;
        }
        return selected.toString();
    }

    /**
     * @param message shown on {@link #playerMgmtFeedback}
     */
    private void showPlayerMgmtFeedback(String message) {
        playerMgmtFeedback.setText(message);
        playerMgmtFeedback.setVisible(true);
    }

    /** clears text and hides {@link #playerMgmtFeedback} */
    private void clearPlayerMgmtFeedback() {
        playerMgmtFeedback.setText("");
        playerMgmtFeedback.setVisible(false);
    }

    /**
     * @param message shown on {@link #createAccountFeedback}
     */
    private void showCreateAccountFeedback(String message) {
        createAccountFeedback.setText(message);
        createAccountFeedback.setVisible(true);
    }

    /** clears new-account fields and feedback */
    private void clearCreateAccountForm() {
        newUsernameField.setText("");
        newPasswordField.setText("");
        createAccountFeedback.setText("");
        createAccountFeedback.setVisible(false);
    }
}
