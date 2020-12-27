package ui.formChild;

import ui.formChildData.ChildDataFormListItem;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by angel on 29/03/17.
 */
public class ChildFormListMouseAdapter extends MouseAdapter {
    private final ChildForm form;
    final ArrayList<ChildDataFormListItem> subs = new ArrayList<>();


    public ChildFormListMouseAdapter(ChildForm form) {
        this.form = form;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        subs.clear();
        JList<ChildDataFormListItem> list = (JList<ChildDataFormListItem>) e.getSource();
        int index = list.locationToIndex(e.getPoint());
        ChildDataFormListItem item = list.getModel().getElementAt(index);
        if (index-- > 0) {
            int id = item.getId();
            ChildDataFormListItem i= list.getModel().getElementAt(index);

            while (index > 0 && i.getId() != null && i.getId() == id) {
                index--;
                if (i.getSubid() != null) subs.add(0, i);
            }
        }

        int xpos = e.getX() + item.getX();
        if (-7 > xpos && xpos > -20) index = 2;
        else if (-30 > xpos && xpos > -43) index = 1;
        else if (-52 > xpos && xpos > -66) index = 0;
        else return;
        checkIndex(index, item);
        item.boxChecked(form.getStudentId(), form.getStudentName(), index, subs);
        list.repaint();
    }

    private void checkIndex(int index, ChildDataFormListItem item) {
        Boolean add = switch (index) {
            case 0 -> !item.getBox1();
            case 1 -> !item.getBox2();
            case 2 -> !item.getBox3();
            default -> false;
        };

        for (int i = subs.size() - 1; i > -1; i--) {
            item = subs.get(i);
            switch (index) {
                case 0: if ((add && item.getBox1()) || (!add && !item.getBox1())) subs.remove(item);
                    break;
                case 1: if ((add && item.getBox2()) || (!add && !item.getBox2())) subs.remove(item);
                    break;
                case 2: if ((add && item.getBox3()) || (!add && !item.getBox3())) subs.remove(item);
                    break;
            }
        }
    }
}
