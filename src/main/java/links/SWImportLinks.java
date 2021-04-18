package links;

import bd.BDManager;
import main.ApplicationLoader;
import ui.ProgressDialog;
import utils.MyLogger;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SWImportLinks extends SwingWorker<Boolean, Integer> {
    private static final String TAG = SWImportLinks.class.getSimpleName();
    private final File file;
    private final ProgressDialog dialog;
    private final Boolean outcomes;
    private BufferedReader reader;
    private Statement st;
    int position = 0;

    public SWImportLinks(File file, Boolean outcomes) {
        this.file = file;
        this.outcomes = outcomes;
        dialog = new ProgressDialog("Loading file.");
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            initialize();
            String line = reader.readLine();
            while (line != null) {
                String[] items = line.split("\t");
                if (items.length > 0) {
                    if (!processLine(items)) return false;
                }
                line = reader.readLine();
                dialog.pb.setValue(++position);
            }
            reader.close();
            ApplicationLoader.bdManager.executeBatch(st);
            MyLogger.d(TAG, "--- IMPORT COMPLETED ---");
        } catch (IOException ex) {
            MyLogger.e(TAG, ex);
            return false;
        } finally {
            BDManager.closeQuietly(st);
            ApplicationLoader.bdManager.closeQuietlyConnection();
            dialog.dispose();
        }
        return true;
    }

    private Boolean processLine(String[] items) throws SQLException {
        for (int i = 1; i < items.length; i++) {
            if (items[i].trim().equals("")) {continue;}
            if (!items[i].contains(".")) {
                if (!items[i].contains("-")) {
                    addBatch(items[i], "0", items[0]);
                } else {
                    addMultiplePresentations(items[i], items[0]);
                }
            } else {
                String[] values = items[i].split("\\.");
                if (values.length < 2) {
                    logError(values[0], null);
                    return false;
                }
                if (values[1].equals("*")) addAsterisk(values[0], items[0]);
                else {
                    if (values[1].contains("-")) {
                        String[] subs = values[1].split("-");
                        return addMultipleSubs(values[0], subs[0], subs[1], items[0]);
                    } else addBatch(values[0], values[1], items[0]);
                }
            }
        }
        return true;
    }

    private void initialize() throws IOException {
        reader = new BufferedReader(new FileReader(file));
        dialog.pb.setMaximum((int) Files.lines(file.toPath()).count());
        st = ApplicationLoader.bdManager.prepareBatch();
    }

    private void addMultiplePresentations(String presentations, String nc) throws SQLException {
        String[] range = presentations.split("-");
        for(int presentation = Integer.parseInt(range[0]); presentation <= Integer.parseInt(range[1]); presentation++) {
            addBatch(String.valueOf(presentation), "0", nc);
        }
    }

    private void addBatch(String presentation, String presentation_sub, String nc) throws SQLException {
        String sql = "INSERT INTO `Links` (`presentation`,`presentation_sub`,`Outcomes`,`Targets`) VALUES("
                + presentation + "," + presentation_sub + "," + (outcomes ? nc : "0") + "," +
                (outcomes ? "0" : nc) + ")";
        st.addBatch(sql);
    }

    private void addAsterisk(String presentation, String nc) throws SQLException {
        for (Integer sub : ApplicationLoader.cacheManager.presentationsSubPerPresentation.get(Integer.parseInt(presentation))) {
            addBatch(presentation, sub.toString(), nc);
        }
    }

    private Boolean addMultipleSubs(String presentation, String _start, String _end, String nc) throws SQLException {
        int start, end;
        try {
            start = Integer.parseInt(_start);
            end = Integer.parseInt(_end);
        } catch (Exception ex) {
            logError(nc, null);
            return false;
        }

        ArrayList<Integer> subs = ApplicationLoader.cacheManager.presentationsSubPerPresentation.get(Integer.parseInt(presentation));
        if (subs != null) {
            for (Integer sub : subs) {
                if (sub >= start && sub <= end) addBatch(presentation, sub.toString(), nc);
            }
            return true;
        }   else {
            logError(nc, presentation);
            return false;
        }
    }

    private void logError(String nc, String presentation) {
        String ncText = (outcomes ? "Outcome: " : "Target: ") + nc;
        String description = presentation != null ? "Presentation " + presentation + " does not have subs" : "Syntax error";
        MyLogger.d("Error with " + ncText, description);
        MyLogger.d(TAG, "--- IMPORT ABORTED ---");

    }
}
