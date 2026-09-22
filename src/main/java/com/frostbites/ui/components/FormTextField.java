package com.frostbites.ui.components;

import java.awt.Dimension;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.frostbites.ui.theme.UITheme;

/**
 * Plain {@link JTextField} styled to match our login/register forms — same font, width/height, and padding
 * as {@link FormPasswordField} so the inputs line up visually.
 */
public class FormTextField extends JTextField {
    /** Sets up the usual body font, 280×36-ish box, and a little inner padding. */
    public FormTextField() {
        setFont(UITheme.FONT_BODY);
        setPreferredSize(new Dimension(280, 36));
        setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM));
    }
}
