package utils;

import main.ApplicationLoader;

import javax.swing.*;
import java.io.*;
import java.sql.Date;
import java.util.HashMap;

public class SettingsManager {
    private static final String TAG = SettingsManager.class.getSimpleName();
    public static final String ATTACH_DIR   = "ATTACH_DIR";
    public static final String CLASSROOM    = "CLASSROOM";
    public static final String YEARS        = "YEARS";
    private static final String TEACHER     = "TEACHER";
    public static final String REPORTS_DIR  = "REPORTS_DIR";
    public static final String LAST_DATE    = "LAST_DATE";
    public static final String VERSION      = "VERSION";
    public static final String CHANGES      = "CHANGES";
    public static final String CHECKUPDATE  = "CHECKUPDATE";
    public static final String SHOWDIALOG   = "SHOWDIALOG";
    public static final String LANGUAGE     = "LANGUAGE";
    public static final String START_OF_YEAR= "START_OF_YEAR";
    public static final String FIRST_TERM   = "FIRST_TERM";
    public static final String SECOND_TERM  = "SECOND_TERM";
    public static final String THIRD_TERM   = "THIRD_TERM";
    public static final String USER         = "USER";
    public static final String LASTDIR      = "LASTDIR";
    public static final String LINK         = "LINK";
    public static final String PWD          = "PWD";
    public static final String FILENAME     = "FILENAME";
    public static final String DOWNLOADURL  = "DOWNLOADURL";
    public static final String VERSIONURL   = "VERSIONURL";
    public static final String EMAIL        = "EMAIL";
    public static final String EMAIL_SERVER = "EMAIL_SERVER";
    public static final String EMAIL_SOCKET = "EMAIL_SOCKET";
    public static final String EMAIL_PWD    = "EMAIL_PWD";

    private final JFrame frame;
    private String dir;
    private final String separator;
    private final String filename           = "Settings.ini";
    private final HashMap<String, String> settings;
    public Integer teacher;
    public String user;
    public Integer language;
    public Date date_SY = Date.valueOf("2020-09-01");
    public Date date_FT = Date.valueOf("2020-12-31");
    public Date date_ST = Date.valueOf("2021-04-01");
    public Date date_TT = Date.valueOf("2021-07-01");


    public SettingsManager(JFrame frame) {
        this.frame = frame;
        dir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        separator = File.separator;
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");

        if ((dir.charAt(dir.length()-1)) == separator.charAt(0)) {
            dir = dir.substring(0, dir.lastIndexOf(separator));
        }
        dir = dir.substring(0, dir.lastIndexOf(separator)+1);
        settings = new HashMap<>();
        readFile();
    }

    private void readFile(){
        FileReader reader = null;
        try {
            reader = ApplicationLoader.fileManager.getReader(dir + filename, true);
            if (reader == null) return;
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            String[] fields = new String[2];
            while ((line = bufferedReader.readLine()) != null) {
                fields[0] = line.substring(0, line.indexOf("="));
                fields[1] = line.substring(line.indexOf("=")+1);
                settings.put(fields[0], fields[1]);
                if (fields[0].equals(TEACHER)) teacher = Integer.valueOf(fields[1]);
                if (fields[0].equals(USER)) user = fields[1];
            }
            if (getValue(REPORTS_DIR) == null) {
                File newDir = new File(dir + "reports"+separator);
                if (newDir.mkdir() || newDir.exists()) {
                    String dir = newDir.getAbsolutePath() + separator;
                    addValue(REPORTS_DIR, dir);
                    settings.put(REPORTS_DIR, dir);
                } else MyLogger.d(TAG, "ERROR CREANDO DIRECTORIO");
            }

            if (getValue(SHOWDIALOG) == null) addValue(SHOWDIALOG, "1");
            if (getValue(LANGUAGE) == null) {
                addValue(LANGUAGE, "0"); language = 0;
            } else language = Integer.valueOf(getValue(LANGUAGE));
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        } finally {
            try {
                assert reader != null;
                reader.close();
            } catch (IOException e) {
                MyLogger.e(TAG, e);
            }
        }
    }

    public String getValue(String property){
        return settings.get(property);
    }

    public void addValue(String key, String value){
        settings.put(key, value);
        writeFile();
    }

    public Boolean addValues(HashMap<String, String> values){
        Object[] keys = values.keySet().toArray();
        for (Object key : keys){
            settings.put((String) key, values.get(key));
        }
        return writeFile();
    }

    public Boolean writeFile(){
        try {
            FileWriter writer = new FileWriter(dir+filename);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (String key : settings.keySet()){
                String value = settings.get(key);
                if (value != null) {
                    bufferedWriter.write(key+"="+value);
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "SettingsManager.writeFile" + ":" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public String getDir() {
        return dir;
    }

    public String getSeparator() {
        return separator;
    }

    public void setDate_SY(String date_SY) {
        this.date_SY = Date.valueOf(date_SY);
    }

    public void setDate_FT(String date_FT) {
        this.date_FT = Date.valueOf(date_FT);
    }

    public void setDate_ST(String date_ST) {
        this.date_ST = Date.valueOf(date_ST);
    }

    public void setDate_TT(String date_TT) {
        this.date_TT = Date.valueOf(date_TT);
    }
}
