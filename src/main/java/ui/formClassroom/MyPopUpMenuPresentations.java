package ui.formClassroom;

import bd.BDManager;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MyPopUpMenuPresentations extends JPopupMenu implements ActionListener {
    private final static String TAG = MyPopUpMenuPresentations.class.getSimpleName();
    private final BDManager bdManager;
    public static final String[] planning_values = {"M", "T", "W", "Th", "F"};
    final ClassroomFormData formData;
    final JTable tablePresentations;
    final JTable tablePlanning;
    final Date date;
    final SettingsManager settingsManager;
    final CacheManager cacheManager;

    public MyPopUpMenuPresentations(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager,
                                    JTable tablePresentations, JTable tablePlanning, Date date, ClassroomFormData formData) {
        this.formData = formData;
        this.bdManager = bdManager;
        this.settingsManager = settingsManager;
        this.cacheManager = cacheManager;
        this.tablePresentations = tablePresentations;
        this.tablePlanning = tablePlanning;
        this.date = date;
        String[] presentations_values = {" ", "/", "Λ", "Δ"};
        for (String presentations_value : presentations_values) {
            JMenuItem item = new JMenuItem(presentations_value);
            item.addActionListener(this);
            add(item);
        }
        JMenu planning = new JMenu("Planning");
        for (String planning_value : planning_values) {
            JMenuItem item = new JMenuItem(planning_value);
            item.addActionListener(this);
            planning.add(item);
        }

        JMenuItem item = new JMenuItem("Clear");
        item.addActionListener(this);
        planning.add(item);
        add(planning);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Integer newvalue = null;
        JMenuItem item = (JMenuItem)e.getSource();
        int[] rows = tablePresentations.getSelectedRows();
        int[] columns = tablePresentations.getSelectedColumns();

        switch (item.getText()) {
            case " " : newvalue = 0; break;
            case "/" : newvalue = 1; break;
            case "Λ" : newvalue = 2; break;
            case "Δ" : newvalue = 3; break;
            case "M" : newvalue = 4; break;
            case "T" : newvalue = 5; break;
            case "W" : newvalue = 6; break;
            case "Th": newvalue = 7; break;
            case "F" : newvalue = 8; break;
            case "Clear": newvalue = null;
        }

        try {
            SWDBUpdater updater = new SWDBUpdater(bdManager, settingsManager, cacheManager, tablePresentations,
                    tablePlanning, rows, columns, newvalue, date, formData);
            updater.doInBackground();
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        }
    }
}

