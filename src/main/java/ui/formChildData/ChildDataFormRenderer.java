package ui.formChildData;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

/**
 * Created by angel on 28/03/17.
 */
public class ChildDataFormRenderer extends JPanel implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ChildDataFormListItem item = (ChildDataFormListItem) value;
        FontMetrics fm = item.getFontMetrics(item.getLabel().getFont());
        int height = fm.getHeight() * (item.label.getLineCount()+2);
        item.setPreferredSize(new Dimension(list.getParent().getWidth(), height));

        Date date = item.getDate();
        if (date != null) {
            if (date.after(ChildDataForm.endOfTerm)) {
                item.setBackground(Color.yellow);
            }
        }
        return item;
    }
}
