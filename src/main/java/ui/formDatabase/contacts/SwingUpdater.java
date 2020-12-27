package ui.formDatabase.contacts;

import bd.BDManager;
import bd.MySet;
import bd.model.TableContacts;
import bd.model.TableStudents;
import ui.formReports.ReportsForm;
import utils.CacheManager;
import utils.MyLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class SwingUpdater extends SwingWorker {
    private static final String TAG = SwingUpdater.class.getSimpleName();
    public static final Integer UPDATER_LOAD = 0;
    public static final Integer UPDATER_SAVE = 1;
    public static final Integer UPDATER_CLEAR = 2;
    final BDManager bdManager;
    final CacheManager cacheManager;
    final JTabbedPane tabbedPane;
    final JTable tableContacts;
    final Integer action; // 0 load, 1 save;
    final Integer student;
    ChildrenPanel panel;
    final MyTableModel tableModel;
    private ArrayList<Integer[]>brothers;


    public SwingUpdater(BDManager bdManager, CacheManager cacheManager, JTabbedPane tabbedPane, JTable tableContacts,
                        Integer action, Integer student) {
        this.bdManager = bdManager;
        this.cacheManager = cacheManager;
        this.tabbedPane = tabbedPane;
        this.action = action;
        this.student = student;
        this.tableContacts = tableContacts;
        tableModel = (MyTableModel) tableContacts.getModel();
    }

    @Override
    protected Object doInBackground() {
        if (action == null) return null;
        switch (action) {
            case 0 -> {
                clear();
                load();
                ReportsForm.yetChanged = false;
            }
            case 1 -> save();
            case 2 -> clear();
        }
        return null;
    }

    private void clear() {
        for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--) {
            tabbedPane.removeTabAt(i);
        }
        ((DefaultTableModel) tableContacts.getModel()).setRowCount(0);
    }

    private void load() {
        addTab(student, true);
        ArrayList<Integer> toadd = new ArrayList<>();
        for (Integer[] ids : brothers) {
            for (Integer id : ids) {
                if (!toadd.contains(id)) toadd.add(id);//addTab(id, false);
            }
        }
        toadd.remove(student);
        toadd.forEach(id -> addTab(id, false));

    }

    private void addTab(Integer studentId, Boolean doContactLoad){
        Connection co = null;
        try {
            co = bdManager.connect();
            MySet set = bdManager.getValues(co, BDManager.tableStudents, getCondition(studentId));
            while (set.next()) {
                panel = ChildrenPanel.main(cacheManager);
                String name = set.getString(TableStudents.name);
                panel.tFName.setText(name);
                java.sql.Date date = set.getDate(TableStudents.birth_date);
                if (date != null) panel.dMBirthday.setValue(new Date(date.getTime()));
                panel.cBClassroom.setSelectedIndex(set.getInt(TableStudents.classroom));
                panel.tFAddress.setText(set.getString(TableStudents.address));

                Boolean value = set.getBoolean(TableStudents.diseases);
                if (value != null) panel.cBChronicDiseases.setSelected(value);

                value = set.getBoolean(TableStudents.medical_treatment);
                if (value != null) panel.cBMedicalTreatment.setSelected(value);

                value = set.getBoolean(TableStudents.allergies);
                if (value != null) panel.cBAllergies.setSelected(value);

                value = set.getBoolean(TableStudents.special_needs);
                if (value != null) panel.cBSpecialNeeds.setSelected(value);

                value = set.getBoolean(TableStudents.taking_medications);
                if (value != null) panel.cBTakingMedications.setSelected(value);

                date = set.getDate(TableStudents.firstday_snails);
                if (date != null) panel.dMFirstDayComundi.setValue(new Date(date.getTime()));
                date = set.getDate(TableStudents.firstday_cdb);
                if (date != null) panel.dMFirstDayCDB.setValue(new Date(date.getTime()));
                date = set.getDate(TableStudents.firstday_primary);
                if (date != null) panel.dMFirstDayPrimary.setValue(new Date(date.getTime()));
                date = set.getDate(TableStudents.exit_date);
                if (date != null) panel.dMExitDay.setValue(new Date(date.getTime()));
                panel.tANotes.setText(set.getString(TableStudents.notes));
                tabbedPane.addTab(name, panel.mainChildrenPanel);
            }
            if (doContactLoad) loadContacts(co, studentId);
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    private void loadContacts(Connection co, Integer studentId) {
        // "name", "email", "mobile_phone", "job"
        String sql = "(" + TableContacts.student1 + " = " + studentId + " OR " + TableContacts.student2 + " = " + studentId +
                " OR " + TableContacts.student3 +" = " + studentId +" OR " + TableContacts.student4 + " = " + studentId +
                " OR " + TableContacts.student5 + " = " + studentId + ")";

        MySet set = bdManager.getValues(co, BDManager.tableContacts, sql);
        ArrayList<Integer> ids = new ArrayList<>();
        brothers = new ArrayList<>();
        while (set.next()) {
            Integer id = set.getInt(TableContacts.id);
            if (!ids.contains(id)) {
                tableModel.addRow(new String[] {set.getString(TableContacts.name),
                        set.getString(TableContacts.email), set.getString(TableContacts.mobile_phone),
                        set.getString(TableContacts.job)});
                brothers.add(new Integer[]{set.getInt(TableContacts.student1), set.getInt(TableContacts.student2),
                        set.getInt(TableContacts.student3), set.getInt(TableContacts.student4) ,
                        set.getInt(TableContacts.student5)});
                ids.add(id);
            }
        }
    }

    private void save(){
        Connection co = null;
        try {
            co = bdManager.connect();
            PreparedStatement preparedStmt = co.prepareStatement(getInsertString());
            preparedStmt.setString (1,  panel.tFName.getText());
            preparedStmt.setDate   (2,  new java.sql.Date(((Date)panel.dMBirthday.getValue()).getTime()));
            preparedStmt.setInt    (3,  panel.cBClassroom.getSelectedIndex());
            preparedStmt.setString (4,  panel.tFAddress.getText());
            preparedStmt.setBoolean(5,  panel.cBChronicDiseases.isSelected());
            preparedStmt.setBoolean(6,  panel.cBMedicalTreatment.isSelected());
            preparedStmt.setBoolean(7,  panel.cBAllergies.isSelected());
            preparedStmt.setBoolean(8,  panel.cBSpecialNeeds.isSelected());
            preparedStmt.setBoolean(9,  panel.cBTakingMedications.isSelected());
            preparedStmt.setDate   (10, new java.sql.Date(((Date)panel.dMFirstDayComundi.getValue()).getTime()));
            preparedStmt.setDate   (11, new java.sql.Date(((Date)panel.dMFirstDayCDB.getValue()).getTime()));
            preparedStmt.setDate   (12, new java.sql.Date(((Date)panel.dMFirstDayPrimary.getValue()).getTime()));
            preparedStmt.setDate   (13, new java.sql.Date(((Date)panel.dMExitDay.getValue()).getTime()));
            preparedStmt.setString (14, panel.tANotes.getText());
            preparedStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    private String getCondition(Integer studentId) {
        return TableStudents.id + "=" + studentId;
    }

    private String getInsertString() {
        return "INSERT INTO Students (`name`, `birth_date`, `classroom`, `address`, `chronic diseases`, " +
                "`medical treatment`, `allergies or dietary restrictions`, `special needs`, `taking medications`, " +
                "`firstday_snails`, `firstday_cdb`, `firstday_primary`, `exit_date`, `notes`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

}
