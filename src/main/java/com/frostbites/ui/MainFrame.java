package com.frostbites.ui;

import javax.swing.JFrame;

import backend.AccountManager;

import com.frostbites.ui.screens.DashboardScreen;
import com.frostbites.ui.screens.GameplayScreen;
import com.frostbites.ui.screens.HighScoresScreen;
import com.frostbites.ui.screens.LoginScreen;
import com.frostbites.ui.screens.MainMenuScreen;
import com.frostbites.ui.screens.ManagersOfficeScreen;
import com.frostbites.ui.screens.ResultsScreen;
import com.frostbites.ui.screens.SettingsScreen;
import com.frostbites.ui.screens.TutorialScreen;

/**
 * Top-level window: builds {@link AccountManager}, {@link ScreenManager}, registers every {@link com.frostbites.ui.screens.BaseScreen},
 * sets content pane to the card container, opens main menu
 */
public class MainFrame extends JFrame {

    /** default title {@code FrostBites}, size 1000×700, wires all screens */
    public MainFrame() {
        super("FrostBites");

        AccountManager accountManager = new AccountManager();
        ScreenManager sm = new ScreenManager(accountManager);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(getSize());
        setLocationRelativeTo(null);

        sm.registerScreen(ScreenManager.MAIN_MENU, new MainMenuScreen(sm));
        sm.registerScreen(ScreenManager.SETTINGS, new SettingsScreen(sm));
        sm.registerScreen(ScreenManager.LOGIN, new LoginScreen(sm));
        sm.registerScreen(ScreenManager.DASHBOARD, new DashboardScreen(sm));
        sm.registerScreen(ScreenManager.TUTORIAL, new TutorialScreen(sm));
        sm.registerScreen(ScreenManager.HIGH_SCORES, new HighScoresScreen(sm));
        sm.registerScreen(ScreenManager.MANAGERS_OFFICE, new ManagersOfficeScreen(sm));
        sm.registerScreen(ScreenManager.GAMEPLAY, new GameplayScreen(sm));
        sm.registerScreen(ScreenManager.RESULTS, new ResultsScreen(sm));

        setContentPane(sm.getContainer());
        sm.showScreen(ScreenManager.MAIN_MENU);
    }
}
