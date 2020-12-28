package ui.formClassroom;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

public class MyTablePlanningRenderer extends JList<String> implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            String[] values = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                values[i] = list.get(i).toString();
            }
            setListData(values);
        }

        if (isSelected) {
            setBackground(UIManager.getColor("Table.selectionBackground"));
        } else {
            setBackground(UIManager.getColor("Table.background"));
        }

        return this;
    }

}
