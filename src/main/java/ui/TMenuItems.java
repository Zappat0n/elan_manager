package ui;

import main.ApplicationLoader;
import utils.SettingsManager;
import bd.BDManager;

import javax.swing.*;
import java.sql.Connection;

/**
 * Created by angel on 5/02/17.
 */
class TMenuItems {
    private static final String TAG = TMenuItems.class.getSimpleName();

    public static JMenuItem addData() {
        JMenuItem item = new JMenuItem("Añadir datos");
        item.setToolTipText("Añade eventos de alumnos");
        item.addActionListener(e -> SwingUtilities.invokeLater(() -> AddDataForm.main(ApplicationLoader.bdManager,
                ApplicationLoader.settingsManager, ApplicationLoader.cacheManager)));
        return item;
    }

    public static JMenuItem addClassroom() {
        JMenuItem item = new JMenuItem("Añadir clase");
        item.setToolTipText("Añade una clase a la base de datos");
        item.addActionListener(e -> {
            String classroom = JOptionPane.showInputDialog("¿Nombre de la clase?");
            ApplicationLoader.bdManager.addValue(BDManager.tableClassrooms, new String[]{"name"}, new String[]{classroom});
        });
        return item;
    }

    public static JMenuItem importFile() {
        JMenuItem item = new JMenuItem("Importar fichero");
        item.setToolTipText("Recupera los datos del colegio de un fichero");
        item.addActionListener(e -> {
            String dirPath = ApplicationLoader.settingsManager.getValue(SettingsManager.ATTACH_DIR);
            JFileChooser fc = ApplicationLoader.fileManager.showFileChooser(dirPath, true);
            if (fc != null) ApplicationLoader.bdManager.importFile(fc.getSelectedFile());
        });
        return item;
    }

    public static JMenuItem execSql() {
        JMenuItem item = new JMenuItem("Ejecutar Sql");
        item.addActionListener(e -> {
            Connection co = null;
            try {
                co = ApplicationLoader.bdManager.connect();
                String query = JOptionPane.showInputDialog("Sql order:");
                ApplicationLoader.bdManager.executeQueryUpdate(co, query);
            } finally {
                BDManager.closeQuietly(co);
            }
        });
        return item;
    }


}
