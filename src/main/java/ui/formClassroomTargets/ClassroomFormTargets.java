package ui.formClassroomTargets;

import bd.BDManager;
import main.ApplicationLoader;
import ui.MainForm;
import ui.components.DateLabelFormatter;
import utils.CacheManager;
import utils.SettingsManager;
import utils.data.RawData;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class ClassroomFormTargets {
    public static BDManager bdManager;
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;
    private JPanel mainPanel;
    private JTable tableTargets;
    private JList<String> listClassrooms;
    private JList<String> listStages;
    private JList<String> listAreas;
    private UtilDateModel dateModel;
    private JDatePickerImpl datePicker;
    private JButton buttonPrint;
    private JSplitPane mainSP;

    public static JPanel main(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager) {
        ClassroomFormTargets.bdManager = bdManager;
        ClassroomFormTargets.settingsManager = settingsManager;
        ClassroomFormTargets.cacheManager = cacheManager;
        ClassroomFormTargets form = new ClassroomFormTargets();
        form.mainSP.setDividerLocation(180);
        return form.mainPanel;
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

        tableTargets = new JTable(new MyTableModelTargets(settingsManager, cacheManager, bdManager, mainPanel, dateModel)) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {return new MyTableTargetsRenderer(cacheManager);}
        };

        tableTargets.setRowSelectionAllowed ( false );
        tableTargets.setCellSelectionEnabled ( true );
        tableTargets.setShowGrid(true);
        tableTargets.getTableHeader().setDefaultRenderer(new MyHeaderRenderer());
        tableTargets.addMouseListener(new MyMouseAdapter(bdManager, settingsManager, cacheManager,
                new java.sql.Date(dateModel.getValue().getTime())));

        listClassrooms = new JList<>(RawData.classrooms);
        listStages = new JList<>(RawData.stagesNC);
        listAreas = new JList<>(new DefaultListModel<>());
        listStages.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = listStages.getSelectedIndex();
                if (index != -1) {
                    DefaultListModel<String> model = (DefaultListModel<String>) listAreas.getModel();
                    model.clear();
                    for (Integer area : ApplicationLoader.cacheManager.areasTargetPerStage.get(RawData.yearsNC[index])) {
                        model.addElement(cacheManager.areasTarget.get(area)[settingsManager.language]);
                    }
                }
            }
        });
        listAreas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadTable();
            }
        });

        buttonPrint = new JButton();
        buttonPrint.addActionListener(e -> {
            try {
                if (!tableTargets.print()) {
                    System.err.println("User cancelled printing");
                }
            } catch (java.awt.print.PrinterException ex) {
                System.err.format("Cannot print %s%n", ex.getMessage());
            }
        });
    }

    private void loadTable() {
        if (listAreas.getSelectedIndex() == -1) return;
        int classroom = listClassrooms.getSelectedIndex() + 1;
        int stage = listStages.getSelectedIndex();


        if (classroom != 0 && stage != -1) {
            ArrayList<Integer> areas = ApplicationLoader.cacheManager.areasTargetPerStage.get(RawData.yearsNC[stage]);
            Integer area = areas.get(listAreas.getSelectedIndex());
            if (area != -1) {
                SwingUtilities.invokeLater(() -> {
                    ((MyTableModelTargets) tableTargets.getModel()).loadData(classroom, stage, area);
                    resizeColumns(tableTargets);
                    updateRowHeights(tableTargets);
                });
            }
        } else {
            JOptionPane.showMessageDialog(MainForm.frame, "Please select a classroom", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void resizeColumns(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(406);
        for (int column = 1; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth;
            TableCellRenderer rend = table.getTableHeader().getDefaultRenderer();
            TableCellRenderer rendCol = tableColumn.getHeaderRenderer();
            if (rendCol == null) rendCol = rend;
            Component header = rendCol.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, 0, column);
            maxWidth = header.getPreferredSize().width;

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
                if (preferredWidth <= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }
            tableColumn.setPreferredWidth(preferredWidth + 6);
        }
    }

    private void updateRowHeights(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = table.getRowHeight();
            Component comp = table.prepareRenderer(table.getCellRenderer(row, 0), row, 0);
            rowHeight = Math.max(rowHeight, (int)Math.round(comp.getPreferredSize().height *0.35));

            table.setRowHeight(row, rowHeight);
        }
    }
}
