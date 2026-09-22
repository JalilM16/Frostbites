package com.frostbites.ui.screens;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.components.SecondaryButton;
import com.frostbites.ui.components.TitleLabel;
import com.frostbites.ui.theme.UITheme;

/**
 * In-game help: first slide is full cover art, then cards with a PNG on top and HTML text in a scroll area.
 * Images load from disk in dev or from the classpath when the game is packaged.
 */
public class TutorialScreen extends BaseScreen {

    private static final String TUTORIAL_DIR = "assets/images/tutorial/";
    private static final String TUTORIAL_RESOURCE_PREFIX = "/assets/images/tutorial/";

    private static final String[] SLIDE_HTML = {
        "<html><body style='margin:0;padding:0'>"
            + "<p style='margin-top:0'>You run a dessert shop. Customers line up with orders. Your job is to type the words "
            + "that appear on the screen to move each order along!</p>"
            + "<p>Use the <b>Next</b> button to read more, or <b>Back to Main Menu</b> anytime.</p>"
            + "</body></html>",

        "<html><body style='margin:0;padding:0'>"
            + "<p style='margin-top:0'>Every order goes through three steps: order, build, and serve. "
            + "Type each word completely to finish the step and move on!</p>"
            + "</body></html>",

        "<html><body style='margin:0;padding:0'>"
            + "<p style='margin-top:0'><b>Typing</b></p>"
            + "<p>Look at the <b>current word</b> on the gameplay screen. Press the keys on your keyboard "
            + "in order. Letters you typed correctly are highlighted in <b>green</b>.</p>"
            + "<p>If you press the wrong letter, you may lose health (hearts) after several mistakes — "
            + "so go carefully. Try to finish each word before the <b>timer</b> runs out!</p>"
            + "</body></html>",

        "<html><body style='margin:0;padding:0'>"
            + "<p style='margin-top:0'><b>Power-ups</b></p>"
            + "<p>During a level you have two helpers at the bottom of the screen:</p>"
            + "<ul>"
            + "<li><b>Freeze time</b> — pauses the countdown for a few seconds so you can catch your breath.</li>"
            + "<li><b>Auto-clean</b> — helps finish typing the current word.</li>"
            + "</ul>"
            + "<p>Click the buttons to use them when you need help.</p>"
            + "</body></html>",

        "<html><body style='margin:0;padding:0'>"
            + "<p style='margin-top:0'><b>Hearts, score, and finishing</b></p>"
            + "<p>You start with <b>hearts</b>. Making too many mistakes "
            + "can cost hearts. If you run out, the game ends.</p>"
            + "<p>Earn points by typing words and bonus <b>tips</b> for perfect orders. Reach the "
            + "<b>target score</b> before the level clock hits zero to beat the level.</p>"
            + "<p>When you are ready, go back to the main menu, log in if you have an account, and "
            + "<b>pick a level</b> on your dashboard. Have fun!</p>"
            + "</body></html>",

        "<html><body style='margin:0;padding:0'>"
            + "<p style='margin-top:0'><b>Pausing</b></p>"
            + "<p>While you play, you can open the pause menu using the <b>Pause</b> button in the "
            + "<b>top right</b> of the screen, or by pressing the <b>Esc</b> key on your keyboard.</p>"
            + "<p>Press <b>Esc</b> again (or <b>Return</b> in the menu) when you want to keep playing.</p>"
            + "</body></html>"
    };

    private static final int SLIDE_COUNT = 1 + SLIDE_HTML.length;
    private static final int TEXT_BOX_HEIGHT = 150;
    private static final int TEXT_PADDING = 12;
    private static final float TEXT_FONT_SIZE = 18f;

    private int slideIndex;

    private final CardLayout cards = new CardLayout();
    private final JPanel slideDeck = new JPanel(cards);

    private JLabel coverLabel;
    private JLabel textImageLabel;

    private JLabel bodyLabel;
    private JLabel pageLabel;
    private SecondaryButton prevButton;
    private SecondaryButton nextButton;

    /**
     * @param screenManager app screen router (passed to {@link BaseScreen})
     */
    public TutorialScreen(ScreenManager screenManager) {
        super(screenManager);
        buildScreen();
    }

    /**
     * Resets to the first slide and redraws whenever the tutorial screen is shown.
     */
    @Override
    public void onShow() {
        slideIndex = 0;
        refreshSlide();
    }

    /**
     * builds cover + text {@link CardLayout} deck, HTML body in {@link JScrollPane}, prev/next/back nav in {@link GridBagLayout}
     *
     * GENERATED WITH AI
     */
    @Override
    protected void initializeUI() {
        setLayout(new BorderLayout(0, UITheme.SPACE_MD));
        add(new TitleLabel("How to Run the Shop"), BorderLayout.NORTH);

        bodyLabel = new JLabel("", SwingConstants.LEFT);
        bodyLabel.setFont(UITheme.FONT_BODY.deriveFont(TEXT_FONT_SIZE));
        bodyLabel.setForeground(UITheme.TEXT_PRIMARY);
        bodyLabel.setVerticalAlignment(SwingConstants.TOP);

        JPanel scrollBody = new JPanel(new BorderLayout());
        scrollBody.setOpaque(true);
        scrollBody.setBackground(UITheme.BG_PANEL);
        scrollBody.setBorder(BorderFactory.createEmptyBorder(TEXT_PADDING, TEXT_PADDING, TEXT_PADDING, TEXT_PADDING));
        scrollBody.add(bodyLabel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(scrollBody);
        scroll.getViewport().setBackground(UITheme.BG_PANEL);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 223), 1));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(560, TEXT_BOX_HEIGHT));
        scroll.setMinimumSize(new Dimension(0, TEXT_BOX_HEIGHT));

        textImageLabel = new JLabel("", SwingConstants.CENTER);
        textImageLabel.setOpaque(true);
        textImageLabel.setBackground(new Color(245, 248, 252));
        textImageLabel.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 223), 1));

        JPanel textCard = new JPanel(new BorderLayout(0, UITheme.SPACE_SM));
        textCard.setBackground(UITheme.BG_PANEL);
        textCard.setBorder(BorderFactory.createEmptyBorder(
                UITheme.SPACE_MD, UITheme.SPACE_MD, UITheme.SPACE_MD, UITheme.SPACE_MD));
        textCard.add(textImageLabel, BorderLayout.CENTER);
        textCard.add(scroll, BorderLayout.SOUTH);

        coverLabel = new JLabel("", SwingConstants.CENTER);
        coverLabel.setOpaque(true);
        coverLabel.setBackground(UITheme.BG_PANEL);
        JPanel coverCard = new JPanel(new BorderLayout());
        coverCard.setBackground(UITheme.BG_PANEL);
        coverCard.add(coverLabel, BorderLayout.CENTER);

        slideDeck.setBackground(UITheme.BG_PANEL);
        slideDeck.setPreferredSize(new Dimension(560, 320));
        slideDeck.add(coverCard, "cover");
        slideDeck.add(textCard, "text");

        JPanel framed = new JPanel(new BorderLayout());
        framed.setBackground(UITheme.BG_PANEL);
        framed.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 223), 1));
        framed.add(slideDeck, BorderLayout.CENTER);

        pageLabel = new JLabel("", SwingConstants.CENTER);
        pageLabel.setFont(UITheme.FONT_BODY.deriveFont(13f));
        pageLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel center = new JPanel(new BorderLayout(0, UITheme.SPACE_SM));
        center.setOpaque(false);
        center.add(framed, BorderLayout.CENTER);
        center.add(pageLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        prevButton = new SecondaryButton("Previous");
        nextButton = new SecondaryButton("Next");
        SecondaryButton backButton = new SecondaryButton("Back to Main Menu");

        prevButton.addActionListener(withUiClick(e -> {
            if (slideIndex > 0) {
                slideIndex--;
                refreshSlide();
            }
        }));
        nextButton.addActionListener(withUiClick(e -> {
            if (slideIndex < SLIDE_COUNT - 1) {
                slideIndex++;
                refreshSlide();
            } else {
                screenManager.showScreen(ScreenManager.MAIN_MENU);
            }
        }));
        backButton.addActionListener(withUiClick(e -> screenManager.showScreen(ScreenManager.MAIN_MENU)));

        JPanel nav = new JPanel(new GridBagLayout());
        nav.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, UITheme.SPACE_SM, 0, UITheme.SPACE_SM);
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        nav.add(prevButton, c);
        c.gridx = 1;
        c.weightx = 0;
        c.anchor = GridBagConstraints.CENTER;
        JPanel nextWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.SPACE_SM, 0));
        nextWrap.setOpaque(false);
        nextWrap.add(nextButton);
        nav.add(nextWrap, c);
        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 1;
        nav.add(backButton, c);
        add(nav, BorderLayout.SOUTH);

        refreshSlide();
    }

    /**
     * switches {@link #cards} cover vs text, applies {@link #SLIDE_HTML}, loads matching png, updates page label and button states
     *
     * GENERATED WITH AI
     */
    private void refreshSlide() {
        if (slideIndex == 0) {
            cards.show(slideDeck, "cover");
            Image img = loadPng("slide_1.png");
            if (img != null) {
                coverLabel.setIcon(new ImageIcon(img));
            } else {
                coverLabel.setText("Could not load cover image");
            }
        } else {
            cards.show(slideDeck, "text");
            bodyLabel.setText(SLIDE_HTML[slideIndex - 1]);
            Image img = loadPng("slide_" + (slideIndex + 1) + ".png");
            if (img != null) {
                textImageLabel.setIcon(new ImageIcon(img));
            } else {
                textImageLabel.setIcon(null);
            }
        }
        pageLabel.setText("Slide " + (slideIndex + 1) + " of " + SLIDE_COUNT);
        prevButton.setEnabled(slideIndex > 0);
        nextButton.setText(slideIndex < SLIDE_COUNT - 1 ? "Next" : "Finish");
    }

    /**
     * Tries {@code assets/images/tutorial/} on disk first; if missing, loads the same path as a classpath resource.
     *
     * @param name file name only, for example {@code slide_1.png}
     * @return decoded image, or {@code null} if the file was not found either way
     */
    private Image loadPng(String name) {
        String path = TUTORIAL_DIR + name;
        if (new File(path).isFile()) {
            return new ImageIcon(path).getImage();
        }
        URL url = TutorialScreen.class.getResource(TUTORIAL_RESOURCE_PREFIX + name);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }
        return null;
    }

}
