package ui.formDatabase.Contacts;

import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel {
    Integer rowEditable;

    public void addEmptyRow() {
        addRow(new String[]{null, null, null, null});
        setRowEditable(getRowCount() - 1);
    }

    private void setRowEditable(Integer row) {
        rowEditable = row;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (row == rowEditable) return true;
        else return false;
    }
}
