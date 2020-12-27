package ui.formClassroom;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MyHeaderRenderer extends JLabel implements TableCellRenderer {
    final ClassroomFormData formData;

    public MyHeaderRenderer(ClassroomFormData formData) {
        this.formData = formData;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        VTextIcon icon = new VTextIcon(this, getString(value));
        setIcon(icon);
        if (column > 0) {
            Double age = formData.dates.get(column-1);
            setBackground(getColor(age));
        } else setBackground(Color.lightGray);
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

    private Color getColor(Double age) {
        for (int i = 0; i < MyTablePresentationsRenderer.years.length; i++) {
            if (age < MyTablePresentationsRenderer.years[i]) return MyTablePresentationsRenderer.colors[i];
        }
        return Color.red.darker();
    }

}
