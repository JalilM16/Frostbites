package com.frostbites.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import backend.PlayerAccount;

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
 * login form: username/password fields, demo hint, submit routes to dashboard on success
 */
public class LoginScreen extends BaseScreen {
    private FormTextField usernameField;
    private FormPasswordField passwordField;
    private ErrorLabel errorLabel;

    /**
     * @param screenManager passed to {@link BaseScreen}
     */
    public LoginScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    /**
     * centered {@link ContentPanel} with {@link GridBagLayout}: title, demo line, fields, error label, submit,
     * create-account shortcut, back to main menu
     */
    @Override
    protected void initializeUI() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        ContentPanel contentPanel = new ContentPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(UITheme.SPACE_SM, 0, UITheme.SPACE_SM, 0);

        TitleLabel titleLabel = new TitleLabel("Log In");
        gbc.gridy = 0;
        contentPanel.add(titleLabel, gbc);

        JLabel demoHint = new JLabel("Demo account: username test, password 123", JLabel.CENTER);
        demoHint.setFont(UITheme.FONT_BODY.deriveFont(13f));
        demoHint.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = 1;
        contentPanel.add(demoHint, gbc);

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(UITheme.FONT_BODY);
        usernameLabel.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 2;
        contentPanel.add(usernameLabel, gbc);

        usernameField = new FormTextField();
        gbc.gridy = 3;
        contentPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UITheme.FONT_BODY);
        passwordLabel.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 4;
        contentPanel.add(passwordLabel, gbc);

        passwordField = new FormPasswordField();
        gbc.gridy = 5;
        contentPanel.add(passwordField, gbc);

        errorLabel = new ErrorLabel();
        gbc.gridy = 6;
        contentPanel.add(errorLabel, gbc);

        PrimaryButton loginButton = new PrimaryButton("Submit");
        loginButton.addActionListener(withUiClick(e -> onLogin()));
        gbc.gridy = 7;
        contentPanel.add(loginButton, gbc);

        SecondaryButton createAccountButton = new SecondaryButton("Create Account");
        createAccountButton.addActionListener(withUiClick(e -> onCreateAccount()));
        gbc.gridy = 8;
        contentPanel.add(createAccountButton, gbc);

        SecondaryButton backButton = new SecondaryButton("Back to Main Menu");
        backButton.addActionListener(withUiClick(e -> {
            clearForm();
            screenManager.showScreen(ScreenManager.MAIN_MENU);
        }));
        gbc.gridy = 9;
        contentPanel.add(backButton, gbc);

        wrapper.add(contentPanel);
        add(wrapper, java.awt.BorderLayout.CENTER);
    }

    /**
     * validates non-empty fields, {@link backend.AccountManager#verifyCredentials}, sets logged-in user on
     * {@link ScreenManager}, clears form, opens {@link ScreenManager#DASHBOARD}
     */
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.trim().isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        if (!screenManager.getAccountManager().verifyCredentials(username, password)) {
            showError("Invalid username or password.");
            return;
        }

        clearError();
        PlayerAccount acc = screenManager.getAccountManager().findAccount(username);
        screenManager.setLoggedInUser(acc.getUsername().trim());
        clearForm();
        screenManager.showScreen(ScreenManager.DASHBOARD);
    }

    /**
     * explains that only a manager can create accounts; optional dialog to open Manager's Office or stay here
     *
     * GENERATED WITH AI
     */
    private void onCreateAccount() {
        Object[] options = { "Go to Manager's Office", "Return to Login" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Only a manager (parent or teacher) can create new accounts.\n"
                        + "Please ask your manager to create an account for you\n"
                        + "through the Manager's Office.",
                "Account Creation Restricted",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (choice == JOptionPane.YES_OPTION) {
            clearForm();
            screenManager.showScreen(ScreenManager.MANAGERS_OFFICE);
        }
        // If the user chose "Return to Login" (or closed the dialog), do nothing.
    }

    /**
     * shows {@link #errorLabel} with the given validation or auth message
     *
     * @param message user-visible error text
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /** hides and clears {@link #errorLabel} */
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    /** wipes username/password fields and error state */
    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        clearError();
    }

    // (Removed old manager-only create account dialog; create account is now handled directly via backend)
}
