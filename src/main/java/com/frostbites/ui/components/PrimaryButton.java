package com.frostbites.ui.components;

import com.frostbites.ui.theme.UITheme;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 * The “main” action button — filled accent color, light text, hand cursor. Same footprint as
 * {@link SecondaryButton} so you can stack them in forms without weird size jumps.
 */
public class PrimaryButton extends JButton {
    /**
     * @param text label shown on the button (e.g. “Log in”, “Start”)
     */
    public PrimaryButton(String text) {
        super(text);
        setFocusPainted(false);
        setFont(UITheme.FONT_BUTTON);
        setBackground(UITheme.ACCENT);
        setForeground(UITheme.BG_PANEL);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_MD, UITheme.SPACE_SM, UITheme.SPACE_MD));
        setOpaque(true);
        setPreferredSize(new Dimension(220, 42));
    }
}
