package com.frostbites.ui.screens;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.PrimaryButton;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.theme.UITheme;

/**
 * landing screen: logo column + nav buttons on the left, hero art on the right, login state toggles continue vs log in
 */
public class MainMenuScreen extends BaseScreen {
    private JLabel loginStatusLabel;
    private PrimaryButton loginButton;
    private SecondaryButton logoutButton;

    /**
     * @param screenManager passed to {@link BaseScreen}
     */
    public MainMenuScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    /**
     * two-column {@link GridBagLayout}: narrow west column (logo, status, buttons), wide east {@link #createBackgroundImagePanel}
     * then {@link #refreshAuthActions()}
     */
    @Override
    protected void initializeUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        outer.insets = new Insets(0, 0, 0, UITheme.SPACE_LG);
        outer.anchor = GridBagConstraints.NORTHWEST;
        outer.fill = GridBagConstraints.BOTH;
        outer.weightx = 0.175;
        outer.weighty = 1;

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JPanel logoPanel = createLogoPanel(298, 169);
        gbc.gridy = 0;
        gbc.insets = new Insets(UITheme.SPACE_SM, 0, UITheme.SPACE_MD, 0);
        leftPanel.add(logoPanel, gbc);

        loginStatusLabel = new JLabel("Not logged in", SwingConstants.CENTER);
        loginStatusLabel.setFont(UITheme.FONT_BODY);
        loginStatusLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(UITheme.SPACE_MD, 0, UITheme.SPACE_SM, 0);
        leftPanel.add(loginStatusLabel, gbc);

        loginButton = new PrimaryButton("Log In");
        gbc.gridy = 2;
        gbc.insets = new Insets(UITheme.SPACE_SM, 0, UITheme.SPACE_MD, 0);
        leftPanel.add(loginButton, gbc);

        logoutButton = new SecondaryButton("Log Out");
        logoutButton.addActionListener(withUiClick(e -> {
            screenManager.logout();
            refreshAuthActions();
        }));
        gbc.gridy = 3;
        gbc.insets = new Insets(UITheme.SPACE_XL, 0, UITheme.SPACE_MD, 0);
        leftPanel.add(logoutButton, gbc);

        SecondaryButton instructionsButton = new SecondaryButton("Tutorial");
        instructionsButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.TUTORIAL)));
        gbc.gridy = 4;
        gbc.insets = new Insets(UITheme.SPACE_MD, 0, UITheme.SPACE_MD, 0);
        leftPanel.add(instructionsButton, gbc);

        SecondaryButton highScoresButton = new SecondaryButton("High Scores");
        highScoresButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.HIGH_SCORES)));
        gbc.gridy = 5;
        leftPanel.add(highScoresButton, gbc);

        SecondaryButton settingsButton = new SecondaryButton("Settings");
        settingsButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.SETTINGS)));
        gbc.gridy = 6;
        leftPanel.add(settingsButton, gbc);

        SecondaryButton managerButton = new SecondaryButton("Manager's Office");
        managerButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.MANAGERS_OFFICE)));
        gbc.gridy = 7;
        leftPanel.add(managerButton, gbc);

        SecondaryButton exitButton = new SecondaryButton("Close Game");
        exitButton.addActionListener(withUiClick(e -> {
            Window window = SwingUtilities.getWindowAncestor((Component) e.getSource());
            if (window != null) {
                window.dispose();
            }
        }));
        gbc.gridy = 8;
        leftPanel.add(exitButton, gbc);

        root.add(leftPanel, outer);

        outer.gridx = 1;
        outer.insets = new Insets(0, 0, 0, 0);
        outer.fill = GridBagConstraints.BOTH;
        outer.weightx = 0.825;

        JPanel heroPanel = createBackgroundImagePanel(457, 516);
        root.add(heroPanel, outer);

        add(root, java.awt.BorderLayout.CENTER);
        refreshAuthActions();
    }

    /** sync login button label / target and status text whenever menu is shown */
    @Override
    public void onShow() {
        refreshAuthActions();
    }

    /**
     * scales image to max box using min of width and height ratios (contain-style)
     *
     * @param src   loaded bitmap
     * @param maxW  max width
     * @param maxH  max height
     * @return scaled {@link ImageIcon}
     *
     * GENERATED WITH AI
     */
    private static ImageIcon fitImage(Image src, int maxW, int maxH) {
        int iw = src.getWidth(null);
        int ih = src.getHeight(null);
        if (iw <= 0 || ih <= 0 || maxW <= 0 || maxH <= 0) {
            return new ImageIcon(src);
        }
        double scale = Math.min(maxW / (double) iw, maxH / (double) ih);
        int nw = Math.max(1, (int) Math.round(iw * scale));
        int nh = Math.max(1, (int) Math.round(ih * scale));
        return new ImageIcon(src.getScaledInstance(nw, nh, Image.SCALE_SMOOTH));
    }

    /**
     * shows {@code assets/images/FrostBites_logo.png} scaled with {@link #fitImage} or a missing-file label
     *
     * @param maxWidth  preferred box width
     * @param maxHeight preferred box height
     */
    private JPanel createLogoPanel(int maxWidth, int maxHeight) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(maxWidth, maxHeight));

        File imageFile = new File("assets/images/FrostBites_logo.png");
        if (imageFile.exists()) {
            Image src = new ImageIcon(imageFile.getPath()).getImage();
            JLabel label = new JLabel(fitImage(src, maxWidth, maxHeight));
            panel.add(label);
        } else {
            JLabel missingLabel = new JLabel("Missing: FrostBites_logo.png");
            missingLabel.setFont(UITheme.FONT_BODY);
            missingLabel.setForeground(UITheme.TEXT_SECONDARY);
            panel.add(missingLabel);
        }
        return panel;
    }

    /**
     * right-side hero from {@code assets/images/frostbites_bg.png}; resizes icon on {@link java.awt.event.ComponentAdapter}
     * and initial {@link SwingUtilities#invokeLater} so insets and size are valid
     *
     * @param prefWidth  preferred width hint
     * @param prefHeight preferred height hint
     *
     * GENERATED WITH AI
     */
    private JPanel createBackgroundImagePanel(int prefWidth, int prefHeight) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(prefWidth, prefHeight));

        File imageFile = new File("assets/images/frostbites_bg.png");
        if (imageFile.exists()) {
            final Image src = new ImageIcon(imageFile.getPath()).getImage();
            final JLabel label = new JLabel();
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);
            panel.add(label);

            Runnable refit = () -> {
                Insets ins = panel.getInsets();
                int w = panel.getWidth() - ins.left - ins.right;
                int h = panel.getHeight() - ins.top - ins.bottom;
                if (w >= 2 && h >= 2) {
                    label.setIcon(fitImage(src, w, h));
                }
            };
            panel.addComponentListener(new ComponentAdapter() {
                /** rescales hero image when the welcome panel changes size */
                @Override
                public void componentResized(ComponentEvent e) {
                    refit.run();
                }
            });
            SwingUtilities.invokeLater(refit);
        } else {
            JLabel missingLabel = new JLabel("Missing: frostbites_bg.png");
            missingLabel.setFont(UITheme.FONT_BODY);
            missingLabel.setForeground(UITheme.TEXT_SECONDARY);
            panel.add(missingLabel);
        }
        return panel;
    }

    /**
     * updates status label, swaps login button text (Log In vs Continue), rewires login action to LOGIN or DASHBOARD,
     * enables logout only when logged in (clears old action listeners first)
     *
     * GENERATED WITH AI
     */
    private void refreshAuthActions() {
        if (loginButton == null || logoutButton == null) {
            return;
        }
        boolean loggedIn = screenManager.isLoggedIn();
        if (loginStatusLabel != null) {
            if (loggedIn) {
                String username = screenManager.getLoggedInUsername();
                if (username != null && !username.isBlank()) {
                    loginStatusLabel.setText("Logged in as " + username);
                } else {
                    loginStatusLabel.setText("Logged in");
                }
            } else {
                loginStatusLabel.setText("Not logged in");
            }
        }
        loginButton.setText(loggedIn ? "Continue" : "Log In");
        for (java.awt.event.ActionListener al : loginButton.getActionListeners()) {
            loginButton.removeActionListener(al);
        }
        loginButton.addActionListener(withUiClick(e ->
                screenManager.showScreen(loggedIn ? ScreenManager.DASHBOARD : ScreenManager.LOGIN)));
        logoutButton.setEnabled(loggedIn);
    }
}
