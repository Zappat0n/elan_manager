package ui.formClassroomTargets;

import bd.BDManager;
import utils.CacheManager;
import utils.SettingsManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;

public class MyMouseAdapter extends MouseAdapter {
    private final BDManager bdManager;
    private final SettingsManager settingsManager;
    private final CacheManager cacheManager;
    final Date date;

    public MyMouseAdapter(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager, Date date) {
        this.bdManager = bdManager;
        this.settingsManager = settingsManager;
        this.cacheManager = cacheManager;
        this.date = date;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            JTable table = (JTable) e.getSource();
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if (col == 0) return;

            if (!validSelection(table.getSelectedRows(), table.getSelectedColumns(), row, col)) {
                table.setRowSelectionInterval(row, row);
                table.setColumnSelectionInterval(col, col);
            }

            JPopupMenu menu = new MyPopUpMenuTargets(bdManager, settingsManager, cacheManager, table, date);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
        super.mousePressed(e);
    }

    private Boolean validSelection(int[] selectedRows, int[] selectedColumns, int row, int column) {
        boolean validRow = false;
        boolean validColumn = false;
        for (int sRow: selectedRows) {
            if (sRow == row) validRow = true;
        }
        for (int sColumn: selectedColumns) {
            if (sColumn == column) validColumn = true;
        }
        return validRow && validColumn;
    }
}
