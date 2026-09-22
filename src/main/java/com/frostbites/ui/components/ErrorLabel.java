package com.frostbites.ui.components;

import javax.swing.JLabel;

import com.frostbites.ui.theme.UITheme;

/**
 * Small {@link JLabel} for inline validation or error text on forms (login, register, etc.).
 * Uses {@link UITheme#FONT_BODY} and {@link UITheme#ERROR}; starts {@linkplain #setVisible(boolean) invisible}
 * until the screen sets a message and shows it.
 */
public class ErrorLabel extends JLabel {
    /**
     * Creates an empty error label (no text, not visible). Call {@link #setText(String)} and
     * {@link #setVisible(boolean)} when validation fails.
     */
    public ErrorLabel() {
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.ERROR);
        setVisible(false);
    }
}
