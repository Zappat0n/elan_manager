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
            case "Δ" : newvalue = 3;
        }
        try {
            SWDBUTargetsUpdater updater = new SWDBUTargetsUpdater(bdManager, settingsManager, cacheManager, tableTargets,
                    rows, columns, newvalue, date);
            updater.doInBackground();
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        }
    }

}
