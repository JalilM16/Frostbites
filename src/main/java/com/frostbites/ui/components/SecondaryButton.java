package com.frostbites.ui.components;

import com.frostbites.ui.theme.UITheme;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 * Lighter/outline-style actions next to a {@link PrimaryButton} — muted background, dark text,
 * still the same 220×42 preferred size and padding so the row lines up.
 */
public class SecondaryButton extends JButton {
    /**
     * @param text label shown on the button (e.g. “Back”, “Cancel”)
     */
    public SecondaryButton(String text) {
        super(text);
        setFocusPainted(false);
        setFont(UITheme.FONT_BUTTON);
        setBackground(UITheme.SECONDARY_BUTTON);
        setForeground(UITheme.TEXT_PRIMARY);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_MD, UITheme.SPACE_SM, UITheme.SPACE_MD));
        setOpaque(true);
        setPreferredSize(new Dimension(220, 42));
    }
}
