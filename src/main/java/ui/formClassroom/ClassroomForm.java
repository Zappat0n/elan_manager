package ui.formClassroom;

import bd.BDManager;
import main.ApplicationLoader;
import pdfs.models.Pdf_Planning;
import ui.components.DateLabelFormatter;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.sql.Connection;
import java.util.*;

public class ClassroomForm {
    private static final String TAG = ClassroomForm.class.getSimpleName();
    public ClassroomFormData formData;
    private static Connection co;

    public JPanel mainPanel;
    private JList<String> listClassrooms;
    public JTable tablePresentations;
    private JList<String> listStage;
    private JList<String> listArea;
    private JButton buttonCopy;
    private JTextField tFSearch;
    private UtilDateModel dateModel;
    public JDatePickerImpl datePicker;
    public JTable tablePlanning;
    private JButton buttonPrint;
    private JSplitPane mainSP;
    private JList<String> listSearch;
    ArrayList<Integer> areas;
    private LinkedHashMap<String, Integer[]> presentationsSearched;

    public static JPanel main() {
        ClassroomForm form = new ClassroomForm();
        form.mainSP.setDividerLocation(180);
        return form.mainPanel;
    }

    private void createUIComponents() {
        presentationsSearched = new LinkedHashMap<>();
        areas = new ArrayList<>();
        dateModel = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        dateModel.setValue(new Date());

        try {
            co = ApplicationLoader.bdManager.connect();
            formData = new ClassroomFormData(mainPanel);
            tablePresentations = new JTable(new MyTableModelPresentations(co, mainPanel, dateModel, formData)) {
                @Override
                public TableCellRenderer getCellRenderer(int row, int column) {
                    return new MyTablePresentationsRenderer(formData);
                }
            };
            tablePresentations.setRowSelectionAllowed ( false );
            tablePresentations.setCellSelectionEnabled ( true );
            tablePresentations.getTableHeader().setDefaultRenderer(new MyHeaderRenderer(formData));
            tablePresentations.addKeyListener(new ClipboardKeyAdapter(tablePresentations, tFSearch, formData));
            tablePresentations.setShowGrid(true);

            tablePlanning = new JTable(new MyTableModelPlanning(co, dateModel, mainPanel, formData)) {
                @Override
                public TableCellRenderer getCellRenderer(int row, int column) {
                    return new MyTablePlanningRenderer();
                }
            };
            tablePlanning.getModel().addTableModelListener(e -> updateRowHeight(tablePlanning.getRowCount()-1));
            tablePlanning.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
                @Override
                public void columnAdded(TableColumnModelEvent tableColumnModelEvent) { }

                @Override
                public void columnRemoved(TableColumnModelEvent tableColumnModelEvent) { }

                @Override
                public void columnMoved(TableColumnModelEvent tableColumnModelEvent) { }

                @Override
                public void columnMarginChanged(ChangeEvent changeEvent) {
                    updateRowHeight(tablePlanning.getRowCount()-1);
                }

                @Override
                public void columnSelectionChanged(ListSelectionEvent listSelectionEvent) { }
            });

            tablePresentations.addMouseListener(new MyMouseAdapter(this, co, new java.sql.Date(dateModel.getValue().getTime())));
            tablePlanning.addMouseListener(new MyMouseAdapter(this, co, new java.sql.Date(dateModel.getValue().getTime())));
            formData.setTables(tablePresentations);

            listClassrooms = new JList<>();
            listClassrooms.addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) return;
                listStage.clearSelection();
                listArea.clearSelection();
                formData.loadStudents(listClassrooms.getSelectedIndex()+1);
                MyTableModelPlanning model = (MyTableModelPlanning)tablePlanning.getModel();
                model.resetTable();
                model.loadData();
            });

            listStage = new JList<>();
            listStage.addListSelectionListener(e -> {
                int index = listStage.getSelectedIndex();
                if (index == -1) return;

                Set<Integer> _areas = ApplicationLoader.cacheManager.stageAreaSubareaMontessori.get(index).keySet();
                areas.clear();
                Vector<String> areas_names = new Vector<>();
                for (int area: _areas) {
                    areas.add(area);
                    areas_names.add(ApplicationLoader.cacheManager.areasMontessori.get(area)[ApplicationLoader.settingsManager.language]);
                }
                listArea.setListData(areas_names);
                formData.area = null;
            });

            listArea = new JList<>();
            listArea.addListSelectionListener(e -> loadData());

            buttonCopy = new JButton();
            buttonCopy.addActionListener(e -> {
                tablePresentations.selectAll();
                copyToClipboard(tablePresentations);
            });

            buttonPrint = new JButton();
            buttonPrint.addActionListener(e -> {
                Pdf_Planning planning = new Pdf_Planning(tablePlanning, listClassrooms.getSelectedIndex()+1,
                        dateModel.getValue());
                planning.createDocument();
            });

            tFSearch = new JTextField();
            tFSearch.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent documentEvent) { updateSearch(); }

                @Override
                public void removeUpdate(DocumentEvent documentEvent) { updateSearch(); }

                @Override
                public void changedUpdate(DocumentEvent documentEvent){ updateSearch(); }
            });

            listSearch = new JList<>(new DefaultListModel<>());
            listSearch.addListSelectionListener(e -> {
                if (listSearch.getSelectedIndex() == -1) return;
                Integer[] datos = presentationsSearched.get(listSearch.getSelectedValue()); //id, subarea
                Integer area = (Integer) ApplicationLoader.cacheManager.subareasMontessori.get(datos[1])[
                        ApplicationLoader.settingsManager.language]; //name, nombre, area
                SwingUtilities.invokeLater(() -> {
                    listArea.setSelectedIndex(areas.indexOf(area));
                    int position = (formData.presentations.indexOf(datos[0]+".0"));
                    if (position != -1) {
                        tablePresentations.changeSelection(position,0,false,false);
                        tablePresentations.scrollRectToVisible(tablePresentations.getCellRect(position,0,true));
                    }

                });
            });
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    private void updateRowHeight(Integer last){
        int rowHeight = tablePlanning.getFontMetrics(tablePlanning.getFont()).getHeight()+2;
        for (int row = 0; row < last; row++) {
            int rows = 0;
            for (int column = 0; column < tablePlanning.getColumnCount(); column++) {
                ArrayList value = ((MyTableModelPlanning)tablePlanning.getModel()).getValueAt(row, column);
                int x = (value != null) ? value.size() : 0;
                rows = Math.max(rows, x);
            }
            tablePlanning.setRowHeight(row, rowHeight * rows);
        }
    }

    private void loadData() {
        if (listArea.getSelectedIndex() == -1) return;
        int classroom = listClassrooms.getSelectedIndex() + 1;
        Integer stage = listStage.getSelectedIndex();
        Integer area = areas.get(listArea.getSelectedIndex());

        if (area != -1 && classroom != -1 && stage != -1) {
            SwingUtilities.invokeLater(() -> {
                if (ApplicationLoader.cacheManager.stageAreaSubareaMontessori.get(stage).containsKey(area)) {
                    formData.getData(classroom, stage, area);
                    resizeColumns(tablePresentations);
                }
            });
        }
    }

    public static void resizeColumns(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF );

        for (int column = 0; column < table.getColumnCount(); column++){
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth;
            TableCellRenderer rend = table.getTableHeader().getDefaultRenderer();
            TableCellRenderer rendCol = tableColumn.getHeaderRenderer();
            if (rendCol == null) rendCol = rend;
            Component header = rendCol.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, 0, column);
            maxWidth = header.getPreferredSize().width;

            for (int row = 0; row < table.getRowCount(); row++){
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
                if (preferredWidth <= maxWidth){
                    preferredWidth = maxWidth;
                    break;
                }
            }
            tableColumn.setPreferredWidth(preferredWidth + 6);
        }
    }

    private void copyToClipboard(JTable table) {
        int numCols= table.getSelectedColumnCount();
        int numRows= table.getSelectedRowCount();
        if (numCols == 0 || numRows == 0) return;

        int[] rowsSelected= table.getSelectedRows();
        int[] colsSelected= table.getSelectedColumns();
        if (numRows!=rowsSelected[rowsSelected.length-1]-rowsSelected[0]+1 || numRows!=rowsSelected.length ||
                numCols!=colsSelected[colsSelected.length-1]-colsSelected[0]+1 || numCols!=colsSelected.length) {

            JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder excelStr=new StringBuilder();
        excelStr.append(ClipboardKeyAdapter.CELL_BREAK);
        Integer classroom = formData.classroom;
        if (classroom == null) return;
        for (int i=0; i<numCols-1; i++) {
            excelStr.append(escape((ApplicationLoader.cacheManager.students.get(formData.students.get(i))[0])));
            excelStr.append(ClipboardKeyAdapter.CELL_BREAK);

        }
        excelStr.append(ClipboardKeyAdapter.LINE_BREAK);

        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                excelStr.append(escape(table.getValueAt(rowsSelected[i], colsSelected[j])));
                if (j<numCols-1) {
                    excelStr.append(ClipboardKeyAdapter.CELL_BREAK);
                }
            }
            excelStr.append(ClipboardKeyAdapter.LINE_BREAK);
        }

        StringSelection sel  = new StringSelection(excelStr.toString());
        ClipboardKeyAdapter.CLIPBOARD.setContents(sel, sel);
    }

    private String escape(Object cell) {
        return cell.toString().replace(ClipboardKeyAdapter.LINE_BREAK, " ").replace(ClipboardKeyAdapter.CELL_BREAK, " ");
    }

    private void updateSearch(){
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) listSearch.getModel();
            String text = tFSearch.getText();
            model.clear();
            if (text.length() < 3 || listStage.getSelectedIndex() == 0) return;
            // Name -> Id, subarea
            presentationsSearched = ApplicationLoader.cacheManager.searchPresentationWithText(text, listStage.getSelectedIndex());
            for (String name: presentationsSearched.keySet()) {
                model.addElement(name);
            }
        });
    }

    public void paintValue(Integer event_id, Integer event_sub, Integer student, int newValue) {
        String id = event_id + "." + (event_sub != null ? event_sub : "0");
        int row = formData.presentations.indexOf(id);
        int col = formData.students.indexOf(student)+1;
        if (row != -1) tablePresentations.setValueAt(newValue, row, col);
        else MyLogger.d(TAG, "Data point lost");
        if (newValue > 3) {
            MyTableModelPlanning model = (MyTableModelPlanning) tablePlanning.getModel();
            model.setValue(event_id, event_sub, col-1, newValue - 3);
        }
    }


}
