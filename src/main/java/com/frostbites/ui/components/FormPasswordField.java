package com.frostbites.ui.components;

import java.awt.Dimension;

import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import com.frostbites.ui.theme.UITheme;

/**
 * Themed {@link JPasswordField} for login/register-style forms: {@link UITheme#FONT_BODY}, fixed
 * preferred size ({@code 280×36}), and inner padding via {@link javax.swing.border.EmptyBorder}
 * with {@link UITheme#SPACE_SM}.
 */
public class FormPasswordField extends JPasswordField {
    /**
     * Creates a password field with the shared FrostBites form styling (echo char remains the default bullet).
     */
    public FormPasswordField() {
        setFont(UITheme.FONT_BODY);
        setPreferredSize(new Dimension(280, 36));
        setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM));
    }
}
