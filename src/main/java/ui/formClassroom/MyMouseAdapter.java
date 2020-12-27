package ui.formClassroom;

import bd.BDManager;
import utils.CacheManager;
import utils.SettingsManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Date;

public class MyMouseAdapter extends MouseAdapter {
    private final JTable tablePlanning;
    private final JTable tablePresentations;
    final ClassroomFormData formData;
    final BDManager bdManager;
    final SettingsManager settingsManager;
    final CacheManager cacheManager;
    final Connection co;
    final Date date;

    public MyMouseAdapter(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager,
                          Connection co, Date date, JTable tablePresentations, JTable tablePlanning, ClassroomFormData formData) {
        this.formData = formData;
        this.settingsManager = settingsManager;
        this.bdManager = bdManager;
        this.cacheManager = cacheManager;
        this.tablePlanning = tablePlanning;
        this.tablePresentations = tablePresentations;
        this.co = co;
        this.date = date;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            JTable table = (JTable) e.getSource();
            int row = table.rowAtPoint(e.getPoint());
            int column = table.columnAtPoint(e.getPoint());
            if (column == 0) return;

            if (table.getModel() instanceof MyTableModelPresentations ) {
                if (!validSelection(table.getSelectedRows(), table.getSelectedColumns(), row, column)) {
                    table.setRowSelectionInterval(row, row);
                    table.setColumnSelectionInterval(column, column);
                }

                JPopupMenu menu = new MyPopUpMenuPresentations(bdManager, settingsManager, cacheManager, table,
                        tablePlanning, date, formData);
                //menu.setSelected(menu.getComponent(0));
                //menu.setLabel(row +":"+column);
                menu.show(e.getComponent(), e.getX(), e.getY());
            } else {
                String event = ((MyTableModelPlanning)table.getModel()).events[row][column-1];
                JPopupMenu menu = new MyPopUpMenuPlanning(bdManager, formData.students.get(row),
                        event, tablePresentations, tablePlanning, row, column, date, formData);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
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
