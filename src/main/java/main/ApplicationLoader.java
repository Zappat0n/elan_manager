package main;

import links.LinkManager;
import ui.LoaderForm;

import utils.CacheManager;
import utils.SettingsManager;
import bd.BDManager;
import utils.FileManager;
import utils.MyLogger;

import javax.swing.*;

/**
 * Created by angel on 2/02/17.
 */
public class ApplicationLoader {
    private static final String TAG = ApplicationLoader.class.getSimpleName();
    public static FileManager fileManager;
    public static SettingsManager settingsManager;
    public static BDManager bdManager;
    public static CacheManager cacheManager;
    public static LinkManager linkManager;

    public static void main(String[] args) {
        launchUI();
    }

    private static void launchUI(){
        SwingUtilities.invokeLater(() -> {
            try {
                LoaderForm.main(null);
                //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } catch (Exception e) {
                MyLogger.e(TAG, e);
            }
        });
    }

}
