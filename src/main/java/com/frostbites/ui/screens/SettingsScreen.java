package com.frostbites.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.ContentPanel;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.components.TitleLabel;
import com.frostbites.ui.theme.UITheme;

/**
 * audio prefs: {@link JSlider} and mute {@link JCheckBox} read/write {@link ScreenManager} master volume and mute flag
 */
public class SettingsScreen extends BaseScreen {
    private JSlider volumeSlider;
    private JCheckBox muteCheck;

    /**
     * @param screenManager passed to {@link BaseScreen}
     */
    public SettingsScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    /**
     * {@link ContentPanel} with volume slider (ticks/labels) bound to {@link ScreenManager#setMasterVolume(float)},
     * mute checkbox to {@link ScreenManager#setMuted(boolean)}, back to main menu
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

        TitleLabel title = new TitleLabel("Settings");
        gbc.gridy = 0;
        contentPanel.add(title, gbc);

        JLabel audioHeading = new JLabel("Audio");
        audioHeading.setFont(UITheme.FONT_SUBTITLE);
        audioHeading.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 1;
        contentPanel.add(audioHeading, gbc);

        volumeSlider = new JSlider(0, 100, (int) (screenManager.getMasterVolume() * 100));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.addChangeListener(e -> {
            float v = volumeSlider.getValue() / 100f;
            screenManager.setMasterVolume(v);
        });
        gbc.gridy = 2;
        contentPanel.add(volumeSlider, gbc);

        JLabel volumeCaption = new JLabel("Master volume");
        volumeCaption.setFont(UITheme.FONT_BODY);
        volumeCaption.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = 3;
        contentPanel.add(volumeCaption, gbc);

        muteCheck = new JCheckBox("Mute all audio");
        muteCheck.setOpaque(false);
        muteCheck.setFont(UITheme.FONT_BODY);
        muteCheck.setForeground(UITheme.TEXT_PRIMARY);
        muteCheck.setSelected(screenManager.isMuted());
        muteCheck.addActionListener(e -> screenManager.setMuted(muteCheck.isSelected()));
        gbc.gridy = 4;
        contentPanel.add(muteCheck, gbc);

        SecondaryButton backButton = new SecondaryButton("Back to Main Menu");
        backButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.MAIN_MENU)));
        gbc.gridy = 5;
        contentPanel.add(backButton, gbc);

        wrapper.add(contentPanel);
        add(wrapper, java.awt.BorderLayout.CENTER);
    }

    /** re-sync widgets from {@link ScreenManager} in case user changed volume elsewhere */
    @Override
    public void onShow() {
        volumeSlider.setValue((int) (screenManager.getMasterVolume() * 100));
        muteCheck.setSelected(screenManager.isMuted());
    }
}
