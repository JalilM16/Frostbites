package com.frostbites.ui.screens;

import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.ContentPanel;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.components.TitleLabel;
import com.frostbites.ui.theme.UITheme;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * tiny stub screen: title + subtitle + main menu; optional fake “start gameplay” button for wiring tests
 */
public abstract class PlaceholderScreen extends BaseScreen {
    private final String title;
    private final String subtitle;
    private final boolean includeGameplayActions;

    /**
     * @param screenManager            passed to {@link BaseScreen}
     * @param title                    {@link TitleLabel} text
     * @param subtitle                 secondary line under the title
     * @param includeGameplayActions   if true adds a placeholder button that opens {@link ScreenManager#GAMEPLAY}
     */
    protected PlaceholderScreen(
        ScreenManager screenManager,
        String title,
        String subtitle,
        boolean includeGameplayActions
    ) {
        super(screenManager);
        this.title = title;
        this.subtitle = subtitle;
        this.includeGameplayActions = includeGameplayActions;
        buildScreen();
    }

    /**
     * centered {@link ContentPanel} with title, subtitle, optional gameplay button, then back to main menu
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

        TitleLabel titleLabel = new TitleLabel(title);
        gbc.gridy = 0;
        contentPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel(subtitle, JLabel.CENTER);
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = 1;
        contentPanel.add(subtitleLabel, gbc);

        if (includeGameplayActions) {
            SecondaryButton startGameButton = new SecondaryButton("Start Gameplay (Placeholder)");
            startGameButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.GAMEPLAY)));
            gbc.gridy = 2;
            contentPanel.add(startGameButton, gbc);
        }

        SecondaryButton mainMenuButton = new SecondaryButton("Back to Main Menu");
        mainMenuButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.MAIN_MENU)));
        gbc.gridy = includeGameplayActions ? 3 : 2;
        contentPanel.add(mainMenuButton, gbc);

        wrapper.add(contentPanel);
        add(wrapper, BorderLayout.CENTER);
    }
}
