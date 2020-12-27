package ui;

import main.*;
import bd.BDManager;
import utils.*;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Created by angel on 7/05/17.
 */
public class LoaderForm {
    private static final String TAG =LoaderForm.class.getSimpleName();
    private static JFrame frame;
    private JLabel labelAction;
    private JLabel labelError;
    private JPanel MainPanel;
    private JProgressBar pBar;
    private static JLabel lAction;
    private static JLabel lError;

    private void createUIComponents(){
        labelAction = new JLabel();
        labelError = new JLabel();
        lAction = labelAction;
        lError = labelError;
        JProgressBar pB = pBar;
    }

    private static void load(JFrame frame) {
        class MyWorker extends SwingWorker {
            @Override
            protected Object doInBackground()  {
                lAction.setText("Starting...");
                initApplication();
                lAction.setText("Looking for updates...");
                checkVersion();
                lAction.setText("Loading...");
                if (ApplicationLoader.bdManager.noData) {
                    frame.dispose();
                    return null;
                }
                try {
                    ApplicationLoader.cacheManager = new CacheManager(ApplicationLoader.bdManager,
                            ApplicationLoader.settingsManager, lAction);
                    MainForm.main(null);
                } catch (SQLException e) {
                    MyLogger.e(TAG, e);
                } finally {
                    frame.dispose();
                }
                return null;
            }
        }
        new MyWorker().execute();
    }

    public static void main(String[] args) {
        frame = new JFrame("Elan Manager");
        frame.setContentPane(new LoaderForm().MainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        load(frame);
    }

    private static void initApplication() {
        ApplicationLoader.fileManager = new FileManager(frame);
        ApplicationLoader.settingsManager = new SettingsManager(frame, ApplicationLoader.fileManager);
        ApplicationLoader.bdManager = new BDManager(ApplicationLoader.settingsManager);
    }

    private static void checkVersion() {
        try {
            String versionString = ApplicationLoader.settingsManager.getValue(SettingsManager.VERSION);
            int currentVersion = (versionString!=null && !versionString.equals("")) ? Integer.parseInt(versionString) : -1;
            Integer latestVersion = Updater.getLatestVersion();
            if (latestVersion == null) {
                return;
            }
            if (currentVersion == -1) {
                ApplicationLoader.settingsManager.addValue(SettingsManager.VERSION, String.valueOf(latestVersion));
                return;
            }
            if (latestVersion > currentVersion) {
                new UpdateInfoForm(frame, ApplicationLoader.settingsManager, Updater.getWhatsNew(), false);
            }
        } catch (Exception ex) {
            System.out.print(ex.toString());
        }
    }

}
