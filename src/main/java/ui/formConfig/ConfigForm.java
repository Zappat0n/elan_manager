package ui.formConfig;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import bd.BDManager;
import drive.DriveGovernor;
import ui.MainForm;
import utils.CacheManager;
import utils.MyLogger;
import utils.PlanningManager;
import utils.SettingsManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static drive.DriveGovernor.*;

public class ConfigForm {
    private static final String TAG = ConfigForm.class.getSimpleName();
    private static SettingsManager settingsManager;
    private static BDManager bdManager;
    private static CacheManager cacheManager;
    private JPanel mainPanel;
    private JRadioButton rBEnglish;
    private JRadioButton rBSpanish;
    private JList<String> listGoogleLog;
    private JButton buttonFoldersCheck;
    private JButton button1;

    public ConfigForm() {
        button1.addActionListener(e -> {
            PlanningManager planner = new PlanningManager(112, new Date(2020,8,5), new Date(2020,11,23));
            JOptionPane.showMessageDialog(MainForm.frame, planner.name + ": " + planner.age);
        });
    }

    public static JPanel main(SettingsManager settingsManager, BDManager bdManager, CacheManager cacheManager) {
        ConfigForm.settingsManager = settingsManager;
        ConfigForm.bdManager = bdManager;
        ConfigForm.cacheManager = cacheManager;
        return new ConfigForm().mainPanel;
    }

    private void createUIComponents() {
        rBEnglish = new JRadioButton();
        rBSpanish = new JRadioButton();
        rBEnglish.addActionListener(actionEvent -> {
            settingsManager.language = 0;
            settingsManager.addValue(SettingsManager.LANGUAGE, "0");
        });
        rBSpanish.addActionListener(actionEvent -> {
            settingsManager.language = 1;
            settingsManager.addValue(SettingsManager.LANGUAGE, "1");
        });

        if (settingsManager.language == 0) rBEnglish.setSelected(true);
        else rBSpanish.setSelected(true);

        buttonFoldersCheck = new JButton();
        buttonFoldersCheck.addActionListener(actionEvent -> {
            SWcheckFolders worker = new SWcheckFolders();
            worker.execute();
        });
        listGoogleLog = new JList<>(new DefaultListModel<>());
    }

    public void insertLog(String text) {
        ((DefaultListModel<String>) listGoogleLog.getModel()).insertElementAt(text, 0);
    }


    class SWcheckFolders extends SwingWorker<Object, Object> {
        DriveGovernor governor;
        FileList folders;
        Connection co;
        final Boolean[] updates = {false, false, false, false};

        @Override
        protected Object doInBackground() throws Exception {
            initializeGoogle();

            for (Integer id : cacheManager.students.keySet()) {
                Object[] data = cacheManager.students.get(id); //name, birthday, drive_main, drive_documents, drive_photos, drive_reports
                insertLog("Processing student:" + id + " : " + data[0]);
                String mainDriveId = (String) data[2];
                String documentsDriveId = (String) data[3];
                String photosDriveId = (String) data[4];
                String reportsDriveId = (String) data[5];

                if (mainDriveId == null) {
                    updates[0] = true;
                    mainDriveId = getMainFolder(id);
                }

                if (documentsDriveId == null) {
                    updates[1] = true;
                    FileList list = governor.manager.getFolderContent(mainDriveId, STUDENTS_FOLDER_DOCUMENTS);
                    documentsDriveId = (list.getFiles().size() > 0) ? list.getFiles().get(0).getId() :
                            governor.manager.createFolder(mainDriveId, STUDENTS_FOLDER_DOCUMENTS);
                }

                if (photosDriveId == null) {
                    updates[2] = true;
                    FileList list = governor.manager.getFolderContent(mainDriveId, STUDENTS_FOLDER_PHOTOS);
                    photosDriveId = (list.getFiles().size() > 0) ? list.getFiles().get(0).getId() :
                            governor.manager.createFolder(mainDriveId, STUDENTS_FOLDER_PHOTOS);
                }

                if (reportsDriveId == null) {
                    updates[3] = true;
                    FileList list = governor.manager.getFolderContent(mainDriveId, STUDENTS_FOLDER_REPORTS);
                    reportsDriveId = (list.getFiles().size() > 0) ? list.getFiles().get(0).getId() :
                            governor.manager.createFolder(mainDriveId, STUDENTS_FOLDER_REPORTS);
                }

                saveFoldersToBd(id, mainDriveId, documentsDriveId, photosDriveId, reportsDriveId);
                resetUpdateArray();
            }
            BDManager.closeQuietly(co);
            return null;
        }

        private void saveFoldersToBd(int id, String main, String documents, String photos, String reports) {
            try {
                //id, drive_main, drive_documents, drive_photos, drive_reports
                if (updates[0] || updates[1] || updates[2] || updates[3]) {
                    String query = "UPDATE Students SET drive_main = ?, drive_documents = ?, drive_photos = ?, " +
                            "drive_reports = ? WHERE id = ?";

                    // create the mysql insert preparedstatement
                    PreparedStatement preparedStmt = co.prepareStatement(query);
                    preparedStmt.setString(1, main);
                    preparedStmt.setString(2, documents);
                    preparedStmt.setString(3, photos);
                    preparedStmt.setString(4, reports);
                    preparedStmt.setInt(5, id);
                    preparedStmt.execute();
                    insertLog("Database updated for student");
                }
            } catch (SQLException e) {
                MyLogger.e(TAG, e);
            }
        }

        private void resetUpdateArray() {
            for (int i = 0; i < 4; i++) {
                updates[i] = false;
            }
        }

        private void initializeGoogle() throws IOException {
            governor = new DriveGovernor(bdManager, cacheManager, null);
            folders = governor.manager.getDriveContent(STUDENTS_SHARED_FOLDER);
            co = bdManager.connect();
        }

        private String getMainFolder(int id) {
            for (File f : folders.getFiles()) {
                if (f.getName().contains(String.format("%04d", id))) return f.getId();
            }
            insertLog("Creating main folder");
            return governor.createFolder(STUDENTS_SHARED_FOLDER, String.format("%04d", id) + " " +
                    cacheManager.students.get(id)[0]);
        }
    }

}
