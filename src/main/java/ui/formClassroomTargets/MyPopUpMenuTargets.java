package ui.formClassroomTargets;

import bd.BDManager;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MyPopUpMenuTargets extends JPopupMenu implements ActionListener {
    private final static String TAG = MyPopUpMenuTargets.class.getSimpleName();
    private final BDManager bdManager;
    private final SettingsManager settingsManager;
    private final CacheManager cacheManager;
    Integer target;
    int oldvalue;
    int newvalue;
    Boolean first = true;
    final JTable tableTargets;
    final Date date;
    Connection co;
    //Integer[] types;

    public MyPopUpMenuTargets(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager,
                              JTable tableTargets, Date date) {
        this.bdManager = bdManager;
        this.settingsManager = settingsManager;
        this.cacheManager = cacheManager;
        this.tableTargets = tableTargets;
        this.date = date;
        ButtonGroup group = new ButtonGroup();
        String[] targets_values = {" ", "/", "Λ", "Δ"};
        for (int i = 0; i < targets_values.length; i++) {
            JMenuItem item = new JMenuItem(targets_values[i]);
            item.addActionListener(this);
            group.add(item);
            add(item);
            if (oldvalue == i) item.setSelected(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem item = (JMenuItem)e.getSource();
        int[] rows = tableTargets.getSelectedRows();
        int[] columns = tableTargets.getSelectedColumns();
        //types = (row < ((MyTableModelTargets) tableTargets.getModel()).outcomes.size()) ? RawData.ncOutcomes_Types : RawData.ncTargets_Types;

        switch (item.getText()) {
            case " " : newvalue = 0; break;
            case "/" : newvalue = 1; break;
            case "Λ" : newvalue = 2; break;
            case "Δ" : newvalue = 3; break;
        }
        try {
            SWDBUTargetsUpdater updater = new SWDBUTargetsUpdater(bdManager, settingsManager, cacheManager, tableTargets,
                    rows, columns, newvalue, date);
            updater.doInBackground();
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        }
    }

/*
    private Boolean writetoBd(String text) {
        try {
            co = bdManager.connect();
            if (newvalue > oldvalue) addEvent(types[newvalue-1], null);
            else {
                if (newvalue == 0) {
                    if (removeUpperValues(0)) tableTargets.setValueAt(newvalue, row, column);
                } else {
                    MySet set = bdManager.getValues(co, BDManager.tableEvents, getBasicCondition());
                    Boolean neweventvalue = false;
                    while (set.next()) {
                        if (set.getInt(TableEvents.event_type) == types[newvalue - 1])
                            neweventvalue = true;
                        }
                    Boolean result = removeUpperValues(newvalue);
                    if (!neweventvalue) addEvent(types[newvalue-1], null);
                    else if (result) tableTargets.setValueAt(newvalue, row, column);
                }
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
            JOptionPane.showMessageDialog(tableTargets, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        } finally {
            BDManager.closeQuietly(co);
        }
        return false;
    }

    private void addEvent(Integer type, String notes) {
        Integer id = bdManager.addEvent(co, date, student, type, target, null, notes);
        try {
            if (id != null) tableTargets.setValueAt(newvalue, row, column);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
            JOptionPane.showMessageDialog(tableTargets, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Boolean removeUpperValues(int start) {
        int rs = 0;
        for (int i = start; i < 3; i++) {
            rs += bdManager.removeValue(co, BDManager.tableEvents,
                    getBasicCondition() + " AND " + TableEvents.event_type + " = " + types[i], false);
        }
        return rs > 0;
    }

    private String getBasicCondition() {
        return TableEvents.student + "=" + student + " AND " + TableEvents.event_id + "=" + target;
    }*/
}
