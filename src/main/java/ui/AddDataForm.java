package ui;

import ui.components.DateLabelFormatter;
import ui.listeners.MyAddDataButtonsListener;
import ui.models.*;
import bd.BDManager;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class AddDataForm {
    private static final String TAG = AddDataForm.class.getSimpleName();
    public static JFrame frame;
    public JPanel panel1;
    private JList listClassrooms;
    public JList listMontessori_NC;
    public JList listAreas;
    private JDatePickerImpl datePicker;
    public JList listStudents;
    public JList listInserted;
    public JList listItems;
    private JList listYears;
    public JButton button1;
    public JButton button2;
    public JButton button3;
    private JButton buttonRemove;
    public JTextArea taNotes;
    private JPanel panelButtons;
    public JList listSub;
    private JScrollPane scrollPanePresentations;
    private JSplitPane splitPaneItems;
    private JSplitPane splitPaneNotes;

    public UtilDateModel dateModel;
    public static BDManager bdManager;
    private static SettingsManager settingsManager;
    public static CacheManager cacheManager;

    public static void main(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager) {
        AddDataForm.bdManager = bdManager;
        AddDataForm.settingsManager = settingsManager;
        AddDataForm.cacheManager = cacheManager;
        frame = new JFrame("AddDataForm");
        frame.setContentPane(new AddDataForm().panel1);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        dateModel = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        dateModel.setValue(new Date());
        listClassrooms = new JList(cacheManager.getClasroomsName());
        listClassrooms.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            ListModel m = listClassrooms.getModel();
            String classroom = (String) m.getElementAt(listClassrooms.getSelectedIndex());
            MyListStudentsModel studentsModel = (MyListStudentsModel) listStudents.getModel();
            studentsModel.addData(cacheManager, classroom);
        });

        listYears = new JList();
        listMontessori_NC = new JList();
        listYears.addListSelectionListener(this::selectionStageOrTypeChanged);
        listMontessori_NC.addListSelectionListener(this::selectionStageOrTypeChanged);

        splitPaneItems = new JSplitPane();
        splitPaneNotes = new JSplitPane();
        listStudents = new JList(new MyListStudentsModel());
        listAreas = new JList(new MyListAreasModel(settingsManager));
        scrollPanePresentations = new JScrollPane();
        listItems = new JList(new MyListItemsModel(this));
        listSub = new JList(new MyListSubModel(settingsManager, this));
        listAreas.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            MyListItemsModel m = (MyListItemsModel)listItems.getModel();
            int selection = listAreas.getSelectedIndex();
            if (selection>listAreas.getModel().getSize()) return;
            Integer area = (Integer)((MyListAreasModel)listAreas.getModel()).getElementAndIdAt(selection)[1];
            if (area!=null) m.addData(listMontessori_NC.getSelectedIndex(), listYears.getSelectedIndex(), area);
        });
        listItems.addListSelectionListener(e -> {
            if (listMontessori_NC.getSelectedIndex() != 0) {
                setListPresentationsVisible(false);
                frame.revalidate();
            } else {
                MyListSubModel sub = (MyListSubModel) listSub.getModel();
                sub.clear();
                if (sub.addData(listItems.getSelectedIndex()))  {
                    setListPresentationsVisible(true);
                    splitPaneItems.setDividerLocation(splitPaneNotes.getDividerLocation());
                    frame.revalidate();
                } else {
                    setListPresentationsVisible(false);
                    frame.revalidate();
                }
            }
        });
        listInserted = new JList(new MyListInsertedModel(this));

        panelButtons = new JPanel();
        MyAddDataButtonsListener myAddDataButtonsListener = new MyAddDataButtonsListener(this);
        button1 = new JButton();
        button1.addActionListener(myAddDataButtonsListener);
        button2 = new JButton();
        button2.addActionListener(myAddDataButtonsListener);
        button3 = new JButton();
        button3.addActionListener(myAddDataButtonsListener);

        buttonRemove = new JButton();
        buttonRemove.addActionListener(e -> {
                    MyListInsertedModel m = (MyListInsertedModel) listInserted.getModel();
                    Connection co = null;
                    try {
                        co = bdManager.connect();
                        int[] values = listInserted.getSelectedIndices();
                        boolean confirm = true;
                        for (int i = values.length - 1; i > -1; i--) {
                            String index = m.getElementAt(values[i]).split(";")[0];
                            bdManager.removeValue(co, BDManager.tableEvents, "id=" + index, confirm);
                            if (confirm) confirm = false;
                            m.remove(values[i]);
                        }
                    } catch (Exception ex) {
                        MyLogger.e(TAG, ex);
                    } finally {
                        BDManager.closeQuietly(co);
                    }
                }
        );

        String classroom = settingsManager.getValue(SettingsManager.CLASSROOM);
        String years = settingsManager.getValue(SettingsManager.YEARS);
        if (classroom != null && !classroom.equals(""))
            listClassrooms.setSelectedIndex(Integer.parseInt(classroom));
        if (years != null && !years.equals(""))
            listYears.setSelectedIndex(Integer.parseInt(years));

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                settingsManager.addValue(SettingsManager.CLASSROOM,
                        String.valueOf(listClassrooms.getSelectedIndex()));
                settingsManager.addValue(SettingsManager.YEARS,
                        String.valueOf(listYears.getSelectedIndex()));
            }
        });
    }

    private void selectionStageOrTypeChanged(ListSelectionEvent e){
        if (e.getValueIsAdjusting()) return;
        MyListItemsModel l = (MyListItemsModel)listItems.getModel();
        l.clear();
        MyListAreasModel m = (MyListAreasModel)listAreas.getModel();
        m.clear();

        int yearIndex = listYears.getSelectedIndex();
        int typeIndex = listMontessori_NC.getSelectedIndex();

        if (yearIndex==-1 || typeIndex==-1) return;

        m.addData(cacheManager, typeIndex, yearIndex);
        if (typeIndex == 0) {
            setMontessoriLook();
        } else if (typeIndex == 1 || typeIndex == 2) setNCLook();
        else if (typeIndex == 3) {
            l.addData(typeIndex, 0, -1);
            setObservationsLook();
        }
    }

    private void setMontessoriLook() {
        SwingUtilities.invokeLater(() -> {
            ((TitledBorder)panelButtons.getBorder()).setTitle("Montessori");
            ((TitledBorder)panelButtons.getBorder()).setTitleJustification(TitledBorder.CENTER);
            panelButtons.repaint();
            button1.setText(" Presentado  ");
            button2.setText("Hecho");
            button2.setEnabled(true);
            button3.setText("Repetido");
            button3.setEnabled(true);
        });
    }

    private void setListPresentationsVisible(Boolean visible) {
        scrollPanePresentations.setVisible(visible);
    }

    private void setNCLook() {
        SwingUtilities.invokeLater(() -> {
            ((TitledBorder)panelButtons.getBorder()).setTitle("NC");
            ((TitledBorder)panelButtons.getBorder()).setTitleJustification(TitledBorder.CENTER);
            panelButtons.repaint();

            button1.setText("Cercano");
            button2.setText("Alcanzado");
            button2.setEnabled(true);
            button3.setText("Sobrepasado");
            button3.setEnabled(true);
        });
    }

    private void setObservationsLook() {
        SwingUtilities.invokeLater(() -> {
            ((TitledBorder)panelButtons.getBorder()).setTitle("Observaciones");
            ((TitledBorder)panelButtons.getBorder()).setTitleJustification(TitledBorder.CENTER);
            panelButtons.repaint();
            button1.setText("Añadir obser.");
            button2.setText("--");
            button2.setEnabled(false);
            button3.setText("--");
            button3.setEnabled(false);
        });
    }
 }
