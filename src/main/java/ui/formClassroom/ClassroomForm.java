package ui.formClassroom;

import bd.BDManager;
import pdfs.models.Pdf_Planning;
import ui.components.DateLabelFormatter;
import utils.CacheManager;
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
    public static BDManager bdManager;
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;
    private static Connection co;

    public JPanel mainPanel;
    private JList listClassrooms;
    private JTable tablePresentations;
    private JList listStage;
    private JList listArea;
    private JButton buttonCopy;
    private JTextField tFSearch;
    private UtilDateModel dateModel;
    public JDatePickerImpl datePicker;
    private JLabel labelFound;
    private JTable tablePlanning;
    private JButton buttonPrint;
    private JSplitPane mainSP;
    private JList listSearch;
    Integer currentSearch = null;
    ArrayList<Integer> areas;
    private LinkedHashMap<String, Integer[]> presentationssearched;


    public static JPanel main(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager) {
        ClassroomForm.bdManager = bdManager;
        ClassroomForm.settingsManager = settingsManager;
        ClassroomForm.cacheManager = cacheManager;
        ClassroomForm form = new ClassroomForm();
        form.mainSP.setDividerLocation(180);
        return form.mainPanel;
    }

    private void createUIComponents() {
        presentationssearched = new LinkedHashMap<>();
        areas = new ArrayList<>();
        dateModel = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        dateModel.setValue(new Date());

        co = bdManager.connect();
        formData = new ClassroomFormData(mainPanel, cacheManager, bdManager);
        tablePresentations = new JTable(new MyTableModelPresentations(co, settingsManager, cacheManager, bdManager, mainPanel, dateModel, formData)) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return new MyTablePresentationsRenderer(cacheManager, formData);
            }
        };
        tablePresentations.setRowSelectionAllowed ( false );
        tablePresentations.setCellSelectionEnabled ( true );
        tablePresentations.getTableHeader().setDefaultRenderer(new MyHeaderRenderer(formData));
        tablePresentations.addKeyListener(new ClipboardKeyAdapter(mainPanel, tablePresentations, tFSearch, cacheManager, formData));
        tablePresentations.setShowGrid(true);

        tablePlanning = new JTable(new MyTableModelPlanning(bdManager, settingsManager, co, cacheManager, dateModel, mainPanel, formData)) {
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

        tablePresentations.addMouseListener(new MyMouseAdapter(bdManager, settingsManager, cacheManager, co,
                new java.sql.Date(dateModel.getValue().getTime()), tablePresentations, tablePlanning, formData));
        tablePlanning.addMouseListener(new MyMouseAdapter(bdManager, settingsManager, cacheManager, co,
                new java.sql.Date(dateModel.getValue().getTime()), tablePresentations, tablePlanning, formData));
        formData.setTables(tablePresentations, tablePlanning);

        listClassrooms = new JList();
        listClassrooms.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            listStage.clearSelection();
            listArea.clearSelection();
            formData.loadStudents(listClassrooms.getSelectedIndex()+1);
            MyTableModelPlanning model = (MyTableModelPlanning)tablePlanning.getModel();
            model.resetTable();
            model.loadData();
        });

        listStage = new JList();
        listStage.addListSelectionListener(e -> {
            int index = listStage.getSelectedIndex();
            if (index == -1) return;

            Set<Integer> _areas = cacheManager.stageAreaSubareaMontessori.get(index).keySet();
            areas.clear();
            Vector areas_names = new Vector();
            for (int area: _areas) {
                areas.add(area);
                areas_names.add(cacheManager.areasMontessori.get(area)[settingsManager.language]);
            }
            listArea.setListData(areas_names);
            formData.area = null;
        });

        listArea = new JList();
        listArea.addListSelectionListener(e -> loadData());

        buttonCopy = new JButton();
        buttonCopy.addActionListener(e -> {
            tablePresentations.selectAll();
            copyToClipboard(tablePresentations);
        });

        buttonPrint = new JButton();
        buttonPrint.addActionListener(e -> {
            Pdf_Planning planning = new Pdf_Planning(bdManager, cacheManager, settingsManager, tablePlanning,
                    listClassrooms.getSelectedIndex()+1, dateModel.getValue());
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

        listSearch = new JList(new DefaultListModel());
        listSearch.addListSelectionListener(e -> {
            if (listSearch.getSelectedIndex() == -1) return;
            Integer[] datos = presentationssearched.get(listSearch.getSelectedValue()); //id, subarea
            Object[] datos_subarea = cacheManager.subareasMontessori.get(datos[1]); //name, nombre, area
            SwingUtilities.invokeLater(() -> {
                listArea.setSelectedIndex(areas.indexOf(datos_subarea[2]));
                int position = (formData.presentations.indexOf(datos[0]+".0"));
                if (position != -1) {
                    tablePresentations.changeSelection(position,0,false,false);
                    tablePresentations.scrollRectToVisible(tablePresentations.getCellRect(position,0,true));
                }

            });
        });

        BDManager.closeQuietly(co);
    }

    private void updateRowHeight(Integer last){
        int rowHeight = tablePlanning.getFontMetrics(tablePlanning.getFont()).getHeight()+2;
        for (int row = 0; row < last; row++) {
            int rows = 0;
            for (int column = 0; column < tablePlanning.getColumnCount(); column++) {
                ArrayList<String> value = (ArrayList) tablePlanning.getValueAt(row, column);
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
                if (cacheManager.stageAreaSubareaMontessori.get(stage).containsKey(area)) {
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
            excelStr.append(escape((cacheManager.students.get(formData.students.get(i))[0])));
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
            DefaultListModel model = (DefaultListModel) listSearch.getModel();
            String text = tFSearch.getText();
            model.clear();
            if (text.length() < 3 || listStage.getSelectedIndex() == 0) return;
            // Name -> Id, subarea
            presentationssearched = cacheManager.searchPresentationWithText(text, listStage.getSelectedIndex());
            for (String name: presentationssearched.keySet()) {
                model.addElement(name);
            }
        });
    }
}
