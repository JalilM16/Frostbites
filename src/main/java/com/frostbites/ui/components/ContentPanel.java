package com.frostbites.ui.components;

import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.frostbites.ui.theme.UITheme;

/**
 * Standard content container for FrostBites screens: {@link java.awt.GridBagLayout} for flexible placement,
 * panel background from {@link UITheme#BG_PANEL}, and uniform padding via {@link javax.swing.border.EmptyBorder}
 * using {@link UITheme#SPACE_XL}.
 */
public class ContentPanel extends JPanel {
    /**
     * Builds an empty panel ready for {@link GridBagLayout} constraints; subclasses or callers add children.
     */
    public ContentPanel() {
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_PANEL);
        setBorder(new EmptyBorder(UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL));
    }
}
