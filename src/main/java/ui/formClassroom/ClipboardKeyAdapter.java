package ui.formClassroom;

import main.ApplicationLoader;
import utils.CacheManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ClipboardKeyAdapter extends KeyAdapter {

    public static final String LINE_BREAK = "\n";
    public static final String CELL_BREAK = "\t";
    public static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

    private final ClassroomFormData formData;
    private final JTable table;
    private final JTextField textField;


    public ClipboardKeyAdapter(JTable table, JTextField textField, ClassroomFormData formData) {
        this.table = table;
        this.textField = textField;
        this.formData = formData;
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getKeyCode()==KeyEvent.VK_C) { // Copy
                cancelEditing();
                copyToClipboard(false);
            } else if (event.getKeyCode()==KeyEvent.VK_X) { // Cut
                cancelEditing();
                copyToClipboard(true);
            } else if (event.getKeyCode()==KeyEvent.VK_V) { // Paste
                cancelEditing();
                pasteFromClipboard();
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                if (event.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                    if (textField == null || textField.getText() == null) return;
                    textField.setText(textField.getText() + event.getKeyChar());
                } else {
                    textField.setText(textField.getText().substring(0,
                            textField.getText().length()-1));
                }
                textField.requestFocus();
            });
        }
    }

    private void copyToClipboard(boolean isCut) {
        int numCols=table.getSelectedColumnCount();
        int numRows=table.getSelectedRowCount();
        int[] rowsSelected=table.getSelectedRows();
        int[] colsSelected=table.getSelectedColumns();
        if (numRows!=rowsSelected[rowsSelected.length-1]-rowsSelected[0]+1 || numRows!=rowsSelected.length ||
                numCols!=colsSelected[colsSelected.length-1]-colsSelected[0]+1 || numCols!=colsSelected.length) {

            JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder excelStr=new StringBuilder();
        excelStr.append(CELL_BREAK);
        Integer classroom = formData.classroom;
        if (classroom == null) return;
        for (int i=0; i<numCols-1; i++) {
            excelStr.append(escape(ApplicationLoader.cacheManager.students.get(
                    ApplicationLoader.cacheManager.studentsPerClassroom.get(classroom).get(i))[0]));
            excelStr.append(CELL_BREAK);

        }
        excelStr.append(LINE_BREAK);

        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                excelStr.append(escape(table.getValueAt(rowsSelected[i], colsSelected[j])));
                if (isCut) {
                    table.setValueAt(null, rowsSelected[i], colsSelected[j]);
                }
                if (j<numCols-1) {
                    excelStr.append(CELL_BREAK);
                }
            }
            excelStr.append(LINE_BREAK);
        }

        StringSelection sel  = new StringSelection(excelStr.toString());
        CLIPBOARD.setContents(sel, sel);
    }

    private void pasteFromClipboard() {
        int startRow=table.getSelectedRows()[0];
        int startCol=table.getSelectedColumns()[0];

        String pasteString;
        try {
            pasteString = (String)(CLIPBOARD.getContents(this).getTransferData(DataFlavor.stringFlavor));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid Paste Type", "Invalid Paste Type", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] lines = pasteString.split(LINE_BREAK);
        for (int i=0 ; i<lines.length; i++) {
            String[] cells = lines[i].split(CELL_BREAK);
            for (int j=0 ; j<cells.length; j++) {
                if (table.getRowCount()>startRow+i && table.getColumnCount()>startCol+j) {
                    table.setValueAt(cells[j], startRow+i, startCol+j);
                }
            }
        }
    }

    private void cancelEditing() {
        if (table.getCellEditor() != null) {
            table.getCellEditor().cancelCellEditing();
        }
    }

    private String escape(Object cell) {
        return cell.toString().replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
    }
}

