package ui.formDatabase.Contacts;

import bd.BDManager;
import drive.DriveGovernor;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DBContactsForm {
    private static final String TAG = DBContactsForm.class.getSimpleName();

    private static BDManager bdManager;
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;

    private JPanel mainPanel;
    private JList listClassrooms;
    private JTable tableContacts;
    private JList listStudents;
    private JComboBox cBClassroom;
    private JButton buttonSave;
    private JButton buttonUpload;
    private JButton buttonAddContact;
    private JTabbedPane tabbedPane;
    private JButton buttonAddChildren;
    private Integer student;
    private MyTableModel tableModel;
    private ChildrenPanel childrenPanel;

    public static JPanel main(SettingsManager settingsManager, BDManager bdManager, CacheManager cacheManager) {
        DBContactsForm.bdManager = bdManager;
        DBContactsForm.settingsManager = settingsManager;
        DBContactsForm.cacheManager = cacheManager;
        DBContactsForm form = new DBContactsForm();
        return form.mainPanel;
    }

    private void createUIComponents() {
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        student = null;
        tabbedPane = new JTabbedPane();

        tableModel = new MyTableModel();
        tableModel.setColumnIdentifiers(new String[]{"name", "email", "mobile_phone", "job"});
        tableContacts = new JTable(tableModel);

        listStudents = new JList();
        listClassrooms = new JList(cacheManager.getClassroomsListModel());

        listClassrooms.addListSelectionListener(listSelectionEvent ->
            SwingUtilities.invokeLater(() -> {
                if (listSelectionEvent.getValueIsAdjusting()) return;
                listStudents.setModel(cacheManager.getStudentsListModel(listClassrooms.getSelectedIndex()+1));
            }));

        listStudents.addListSelectionListener(listSelectionEvent -> {
            if (listSelectionEvent.getValueIsAdjusting() || listStudents.getSelectedIndex() == -1) return;
            student = cacheManager.studentsperclassroom.get(
                    listClassrooms.getSelectedIndex()+1).get(listStudents.getSelectedIndex());

            SwingUpdater updater = new SwingUpdater(bdManager, cacheManager, tabbedPane, tableContacts,
                    SwingUpdater.UPDATER_LOAD, student);
            updater.execute();
        });

        buttonUpload = new JButton();
        buttonUpload.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(mainPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                DriveGovernor governor = new DriveGovernor(bdManager, cacheManager, null);
                String folderId = governor.getStudentFolder(DriveGovernor.STUDENTS_FOLDER_DOCUMENTS, student);
                File file = fc.getSelectedFile();
                try {
                    governor.uploadFile(file, folderId);
                } catch (IOException ioException) {
                    MyLogger.e(TAG, ioException);
                }
            }
        });

        buttonSave = new JButton();
        JButton buttonNew = new JButton();
        buttonAddContact = new JButton();
        buttonAddChildren = new JButton();

        buttonAddChildren.addActionListener(e ->
                tabbedPane.addTab("Unknown", ChildrenPanel.main(cacheManager).mainChildrenPanel));

        buttonAddContact.addActionListener(e -> tableModel.addEmptyRow());

    }

}
