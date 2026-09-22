package com.frostbites;

import javax.swing.SwingUtilities;

import com.frostbites.ui.MainFrame;

/** Entry point: EDT-safe {@link MainFrame} launch */
public final class FrostBitesApp {

    /** not instantiable */
    private FrostBitesApp() {
    }

    /**
     * @param args unused
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            /** builds and shows the main window on the EDT */
            @Override
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            }
        });
    }
}
