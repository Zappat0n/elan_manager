package utils;

import main.ApplicationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by angel on 4/05/17.
 */
public class Updater {
    private final static String TAG = Updater.class.getSimpleName();
    public static Integer version = null;
    private static String changes  = null;

    public static Integer getLatestVersion() throws IOException {
        String data = getData();
        if (data == null) return null;
        String[] lines = data.split("\n");
        for (String line : lines) {
            String[] values = line.split(":");
            if (values.length < 2) continue;
            String key = values[0].trim();
            String value = values[1].trim();
            if (key.equals(SettingsManager.VERSION)) version = Integer.parseInt(value);
            if (key.equals(SettingsManager.CHANGES)) changes = value;
        }
        return version;
    }
    public static String getWhatsNew() {
        return changes;
    }

    private static String getData() throws IOException {
        BufferedReader in;
        String readLine;
        StringBuilder result = new StringBuilder();
        String versionURL = ApplicationLoader.settingsManager.getValue(SettingsManager.VERSIONURL);
        if (versionURL != null) {
            URL url = new URL(versionURL);
            in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            while ((readLine = in.readLine()) != null) result.append(readLine).append("\n");
            return result.toString();
        } else return null;
    }
}
