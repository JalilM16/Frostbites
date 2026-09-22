package com.frostbites.ui.components;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.frostbites.ui.theme.UITheme;

/**
 * Big centered screen title — uses {@link UITheme#FONT_TITLE} and the primary text color so every
 * page header looks the same.
 */
public class TitleLabel extends JLabel {
    /**
     * @param text what to show (usually the screen name)
     */
    public TitleLabel(String text) {
        super(text, SwingConstants.CENTER);
        setFont(UITheme.FONT_TITLE);
        setForeground(UITheme.TEXT_PRIMARY);
    }
}
