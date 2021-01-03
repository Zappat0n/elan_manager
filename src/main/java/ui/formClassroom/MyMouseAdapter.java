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
    final ClassroomForm form;
    final Connection co;
    final Date date;

    public MyMouseAdapter(ClassroomForm form, Connection co, Date date) {
        this.form = form;
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

                JPopupMenu menu = new MyPopUpMenuPresentations(form, date);
                menu.show(e.getComponent(), e.getX(), e.getY());
            } else {
                String event = ((MyTableModelPlanning)table.getModel()).events[row][column-1];
                JPopupMenu menu = new MyPopUpMenuPlanning(form, form.formData.students.get(row),
                        event, row, column, date);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        super.mousePressed(e);
    }

    private Boolean validSelection(int[] selectedRows, int[] selectedColumns, int row, int column) {
        boolean validRow = false;
        boolean validColumn = false;
        for (int sRow: selectedRows) {
            if (sRow == row) {
                validRow = true;
                break;
            }
        }
        for (int sColumn: selectedColumns) {
            if (sColumn == column) {
                validColumn = true;
                break;
            }
        }
        return validRow && validColumn;
    }
}
