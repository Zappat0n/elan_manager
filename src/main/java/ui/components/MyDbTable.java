package ui.components;

import bd.MySet;
import bd.MyTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by angel on 7/02/17.
 */
public class MyDbTable extends JTable {
    private final MyModel model;

    public MyDbTable() {
        model = new MyModel();
        setModel(model);
    }

    public void fillTable(MyTable table, MySet set) {
        model.clear();
        model.addColumns(table.fields);
        Vector<Vector<String>> data = new Vector<>();
        while (set.next()) {
            Vector<String> line = new Vector<>();
            for (String column : table.fields) {
                Object o = set.getObject(column);
                if (o != null) line.add(o.toString());
                else line.add(null);
            }
            data.add(line);
        }

        model.addRows(data);
    }

    static class MyModel extends AbstractTableModel {
        private final Vector<String> columns;
        private final ArrayList<Vector<String>> data;
        private final ArrayList<Object> object;

        MyModel() {
            data = new ArrayList();
            columns = new Vector();
            object = new ArrayList<>();
        }

        public void clear() {
            columns.clear();
            data.clear();
            object.clear();
        }

        public Object getObject(int row) {
            return object.get(row);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        public void addColumn(String column) {
            columns.add(column);
        }

        void addColumns(String[] cols) {
            Collections.addAll(columns, cols);
            fireTableStructureChanged();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex).get(columnIndex);
        }

        public void addRow(Vector line, Object object) {
            data.add(line);
            this.object.add(object);
        }

        void addRows(Vector<Vector<String>> newData) {
            data.addAll(newData);
        }

        @Override
        public Class getColumnClass(int c) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return (col > 0);
        }
    }
}
