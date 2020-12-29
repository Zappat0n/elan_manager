package ui.formChildData;

import ui.components.DateLabelFormatter;
import utils.SettingsManager;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

class ChildDataFormWritetodbDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel labelStudent;
    private JLabel labelTarget;
    private JLabel labelType;
    private UtilDateModel dateModel;
    private JDatePickerImpl datePicker;
    private JLabel labelAction;
    private final Integer studentId;
    private final int index;
    private final ChildDataFormListItem item;
    private final SettingsManager settingsManager;
    final ArrayList<ChildDataFormListItem> subitems;
    private final Boolean add;

    private ChildDataFormWritetodbDialog(SettingsManager settingsManager, Byte type, int studentId,
                                         String studentName, int index, ChildDataFormListItem item,
                                         ArrayList<ChildDataFormListItem> subitems, Boolean add) {
        this.settingsManager = settingsManager;
        this.studentId = studentId;
        this.index = index;
        this.item = item;
        this.subitems = subitems;
        this.add = add;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        labelStudent.setText(studentName);
        labelTarget.setText(item.getText());
        String[] typesMontessori = {"Presentado", "Realizado correctamente", "Realizado voluntariamente"};
        String[] typesNC = {"Cercano", "Alcanzado", "Sobrepasado"};
        labelType.setText(type == 2 ? typesMontessori[index] : typesNC[index]);
        if (add) {
            setTitle("Add");
            labelAction.setText("Add event to student history");
        } else {
            setTitle("Remove");
            labelAction.setText("Remove event from student history");
        }
    }

    private void onOK() {
        if (add)
            ChildDataForm.addToBd(studentId, index, item, java.sql.Date.valueOf(LocalDate.of(dateModel.getYear(),
                dateModel.getMonth() + 1, dateModel.getDay())), subitems);
        else ChildDataForm.removeFromBd(item, studentId, index, subitems);
        settingsManager.addValue(SettingsManager.LAST_DATE, String.valueOf(dateModel.getValue().getTime()));
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(SettingsManager settingsManager, Byte type, int studentId, String studentName,
                            int index, Integer[] events, ChildDataFormListItem item,
                            ArrayList<ChildDataFormListItem> subitems, Boolean add) {
        ChildDataFormWritetodbDialog dialog = new ChildDataFormWritetodbDialog(settingsManager, type, studentId,
                studentName, index, item, subitems, add);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        dateModel = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        String dateStr = settingsManager.getValue(SettingsManager.LAST_DATE);
        Date date = (dateStr == null) ? new Date() : new Date(Long.parseLong(dateStr));

        dateModel.setValue(date);

        labelAction = new JLabel();
        labelAction.setFont(labelAction.getFont().deriveFont(Font.BOLD, labelAction.getFont().getSize() + 2));
        labelStudent = new JLabel();
        labelTarget = new JLabel();
        labelType = new JLabel();
    }

}
