package com.frostbites.ui.screens;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.frostbites.audio.AudioManager;
import com.frostbites.ui.ScreenManager;
import com.frostbites.ui.theme.UITheme;

/**
 * base for all the full-screen {@link JPanel} screens — sets up {@link BorderLayout}, {@link UITheme#BG_PRIMARY},
 * {@link UITheme#SCREEN_PADDING} border, and stash the {@link ScreenManager} so we can navigate and hit menu audio
 * concrete screens override {@link #initializeUI()} and usually call {@link #buildScreen()} from ctor
 * {@link #onShow()} / {@link #onHide()} are there if u need lifecycle stuff when {@link ScreenManager} swaps cards
 */
public abstract class BaseScreen extends JPanel {
    protected ScreenManager screenManager;

    /**
     * @param screenManager the shared coordinator (we dont pass null from our screens)
     */
    protected BaseScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setBorder(new EmptyBorder(UITheme.SCREEN_PADDING, UITheme.SCREEN_PADDING, UITheme.SCREEN_PADDING, UITheme.SCREEN_PADDING));
    }

    /**
     * call after {@code super(screenManager)} from subclass ctor — really just calls {@link #initializeUI()} lol
     */
    protected void buildScreen() {
        initializeUI();
    }

    /**
     * put your widgets here (north center south whatever) each screen has to implement this
     */
    protected abstract void initializeUI();

    /**
     * {@link ScreenManager} calls when this screen becomes visible — default does nothing override if u need
     * timers focus bgm whatever
     */
    public void onShow() {
    }

    /**
     * called when leaving this screen — default empty, override to tear down timers listeners etc
     */
    public void onHide() {
    }

    /**
     * wraps {@link ActionListener} so click sfx ({@link AudioManager#SFX_UI_CLICK}) runs then your code
     * saves us pasting the same boilerplate on every button
     *
     * @param inner your actual handler (nav validate etc)
     * @return thing u pass to {@link javax.swing.AbstractButton#addActionListener(ActionListener)}
     */
    protected final ActionListener withUiClick(ActionListener inner) {
        return e -> {
            screenManager.getMenuAudioManager().playSFX(AudioManager.SFX_UI_CLICK);
            inner.actionPerformed(e);
        };
    }
}
