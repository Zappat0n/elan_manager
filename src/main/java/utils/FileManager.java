package utils;

import javax.swing.*;
import java.io.*;


/**
 * Created by angel on 2/06/16.
 * Gestor de archivos
 */
public class FileManager {
    private static final String TAG = FileManager.class.getSimpleName();
    private final JFrame frame;

    public FileManager(JFrame frame) {
        this.frame = frame;
    }

    public JFileChooser showFileChooser(String dirPath, Boolean isOpen) {
        //String dir = ApplicationLoader.settingsManager.getValue(SettingsManager.ATTACH_DIR);
        JFileChooser fc = new JFileChooser(dirPath);
        int result;
        if (isOpen) result = fc.showOpenDialog(frame);
        else  result = fc.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) return fc;
        else return null;
    }

    public FileReader getReader(String pathname, Boolean createIfNeeded) {
        try {
            File file = new File(pathname);
            if (!file.isFile()) {
                if (!createIfNeeded) return null;
                if (!file.createNewFile()) {
                    JOptionPane.showMessageDialog(frame, "Error creando archivo", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }

            return new FileReader(file);
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        }
        return null;
    }
}

