package com.frostbites.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import javax.swing.UIManager;

/**
 * Colors / spacing / fonts for the whole app. Most of this file is just public static finals.
 *
 * <p>The font loading at the bottom I did with Cursor — I kept getting wrong font or file-not-found
 * until it showed me the try-paths + registerFont thing.
 */
public final class UITheme {
    /** utility class — only static members */
    private UITheme() {
    }

    // Colors
    public static final Color BG_PRIMARY = new Color(245, 248, 252);
    public static final Color BG_PANEL = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    public static final Color TEXT_SECONDARY = new Color(95, 105, 115);
    public static final Color ACCENT = new Color(43, 108, 176);
    public static final Color ACCENT_HOVER = new Color(34, 88, 142);
    public static final Color SECONDARY_BUTTON = new Color(230, 236, 244);
    public static final Color SECONDARY_BUTTON_HOVER = new Color(214, 223, 236);
    public static final Color ERROR = new Color(196, 46, 46);

    // Fonts
    private static final Font BASE_FONT = UIManager.getFont("Label.font");
    private static final Font HEADER_FONT_FAMILY = loadFontFamily(
        "KiwiSoda.ttf",
        "assets/fonts/KiwiSoda.ttf",
        "data/fonts/KiwiSoda.ttf",
        "fonts/KiwiSoda.ttf"
    );
    private static final Font BODY_FONT_FAMILY = loadBodyFontFamily();

    public static final Font FONT_TITLE = HEADER_FONT_FAMILY.deriveFont(Font.BOLD, 52f);
    public static final Font FONT_SUBTITLE = HEADER_FONT_FAMILY.deriveFont(Font.BOLD, 30f);
    public static final Font FONT_BODY = BODY_FONT_FAMILY.deriveFont(Font.PLAIN, 16f);
    public static final Font FONT_BUTTON = BODY_FONT_FAMILY.deriveFont(Font.BOLD, 14f);

    // Spacing
    public static final int SPACE_XS = 6;
    public static final int SPACE_SM = 10;
    public static final int SPACE_MD = 16;
    public static final int SPACE_LG = 24;
    public static final int SPACE_XL = 36;
    public static final int SCREEN_PADDING = 50;

    /**
     * Loop filenames until one exists, load ttf, register with the OS so deriveFont works.
     * If nothing works fall back to whatever Label.font was (see class comment — Cursor helped here).
     */
    private static Font loadFontFamily(String... candidatePaths) {
        for (String path : candidatePaths) {
            File fontFile = new File(path);
            if (!fontFile.exists()) {
                continue;
            }
            try {
                Font loaded = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                // without this my buttons still looked like dialog font
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loaded);
                return loaded;
            } catch (IOException | java.awt.FontFormatException ignored) {
                // bad file, try next path
            }
        }
        return BASE_FONT;
    }

    /** JetBrains mono for body text — same idea as kiwi soda paths above */
    private static Font loadBodyFontFamily() {
        return loadFontFamily(
                "JetBrainsMono-Regular.ttf",
                "assets/fonts/JetBrainsMono-Regular.ttf",
                "data/fonts/JetBrainsMono-Regular.ttf",
                "fonts/JetBrainsMono-Regular.ttf");
    }
}
