package ui.formClassroomTargets;

import ui.formClassroom.VTextIcon;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MyHeaderRenderer extends JLabel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        if (value == null) return new JLabel();
        VTextIcon icon = new VTextIcon(this, getString(value));
        setIcon(icon);

        if (Math.floorMod(column, 2) == 1) {
            setOpaque(true);
            setBackground(new Color(255, 230, 230));
        } else {
            setBackground(Color.white);
        }
        return this;
    }

    private String getString(Object value) {
        String[] words = ((String) value).split(" ");
        StringBuilder res = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            res.append(" ").append(words[i], 0, 1).append(".");
        }
        return res.toString();
    }
}
