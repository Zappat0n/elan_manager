package ui.listeners;

import bd.BDManager;
import ui.AddDataForm;
import ui.models.MyListInsertedModel;
import ui.models.MyListItemsModel;
import ui.models.MyListStudentsModel;
import ui.models.MyListSubModel;
import utils.MyLogger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.util.Vector;

/**
 * Created by angel on 27/02/17.
 */
public class MyAddDataButtonsListener implements ActionListener {
    private static final String TAG = MyAddDataButtonsListener.class.getSimpleName();
    private final AddDataForm form;

    public MyAddDataButtonsListener(AddDataForm form) {
        this.form = form;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (form.listMontessori_NC.getSelectedIndex() == 0) {
            if (e.getSource() == form.button1) buttonPressed(1);
            else if (e.getSource() == form.button2) buttonPressed(6);
            else if (e.getSource() == form.button3) buttonPressed(7);
        } else if (form.listMontessori_NC.getSelectedIndex() == 1) {
            if (e.getSource() == form.button1) buttonPressed(9);
            else if (e.getSource() == form.button2) buttonPressed(10);
            else if (e.getSource() == form.button3) buttonPressed(11);
        }else if (form.listMontessori_NC.getSelectedIndex() == 2) {
            if (e.getSource() == form.button1) buttonPressed(4);
            else if (e.getSource() == form.button2) buttonPressed(2);
            else if (e.getSource() == form.button3) buttonPressed(5);
        } else if (form.listMontessori_NC.getSelectedIndex() == 3) buttonPressed(3);
    }

    private void buttonPressed(int event_type) {
        Connection co = null;
        try {
            co = AddDataForm.bdManager.connect();
            java.sql.Date date = java.sql.Date.valueOf(LocalDate.of(form.dateModel.getYear(), form.dateModel.getMonth() + 1, form.dateModel.getDay()));
            Object[][] students = ((MyListStudentsModel) form.listStudents.getModel()).getElementsAndIdsAt(form.listStudents.getSelectedIndices());
            Object[][] items = ((MyListItemsModel) form.listItems.getModel()).getElementsAndIdsAt(form.listItems.getSelectedIndices());
            Object[][] subs = ((MyListSubModel) form.listSub.getModel()).getElementsAndIdsAt(form.listSub.getSelectedIndices());
            if (students == null) {
                JOptionPane.showMessageDialog(AddDataForm.frame, "Error en el estudiante");
                return;
            }
            if (items == null) {
                JOptionPane.showMessageDialog(AddDataForm.frame, "Error en el objeto a añadir");
                return;
            }
            Vector<Integer> vector = new Vector<>();
            for (Object[] student : students) {
                for (Object[] item : items) {
                    for (int k = 0; k <= subs.length; k++) {
                        if (k < subs.length || k == 0) {
                            Integer id = AddDataForm.bdManager.addEvent(co, date,
                                    (Integer) student[1], event_type,
                                    (Integer) item[1],
                                    (k < subs.length) ? (Integer) subs[k][1] : null,
                                    form.taNotes.getText());
                            if (id != null) vector.add(id);
                        }
                    }
                }
            }
            int[] datos = new int[vector.size()];
            for (int i = 0; i < vector.size() ; i++) datos[i] = vector.get(i);
            for (int i = 0; i < vector.size() ; i++) datos[i] = vector.get(i);
            ((MyListInsertedModel)form.listInserted.getModel()).addData(co, datos, subs.length > 0);
        } catch (Exception e1) {
            MyLogger.e(TAG, e1);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

}
