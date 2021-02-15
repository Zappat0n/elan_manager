package bd;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by angel on 28/02/16.
 * Respuesta personalizada de una pregunta a una base de datos
 */
public class MySet {
    public final ArrayList<HashMap<String, Object>> data;
    private final ResultSet rs;
    private final MyTable table;
    private String[] keys;
    private String[] types;
    private Boolean nextUsed = false;
    private int line = 0;

    public MySet(ResultSet rs, MyTable table, String[] keys) throws SQLException{
        data = new ArrayList<>();
        this.rs = rs;
        this.table = table;
        this.keys = keys;
        initKeys();
        processData();
    }

    private void initKeys() {
        if (keys == null) {
            keys = table.fields;
            types = table.field_def;
        } else {
            types = new String[keys.length];
            for (int i = 0; i < keys.length; i++) {
                for (int j = 0; j < table.fields.length; j++) {
                    if (keys[i].equals(table.fields[j])) {
                        types[i] = table.field_def[j];
                        break;
                    }
                }
            }
        }
    }

    private void processData() throws SQLException{
        while (rs.next()) {
            HashMap<String, Object> map = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], getValue(keys[i], types[i]));
            }
            data.add(map);
        }
        BDManager.closeQuietly(rs);
    }

    private Object getValue(String key, String type) throws SQLException {
        if (type.contains("VARCHAR")) type = "STRING";
        Object result;
        switch (type) {
            case "STRING" : case "LONGTEXT" : result = rs.getString(key); break;
            case "INT" : case "SMALLINT" : result = rs.getInt(key); break;
            case "BOOLEAN" : result = rs.getBoolean(key); break;
            case "BIGINT" : result = rs.getLong(key); break;
            case "TIMESTAMP" : result = rs.getTimestamp(key); break;
            case "BLOB" : result = rs.getBlob(key); break;
            case "DATE" : result = rs.getDate(key); break;
            case "DOUBLE" : result = rs.getDouble(key); break;
            default : result = null;
        }
        return result;
    }

    public Boolean next() {
        if (nextUsed) {
            line++;
            return line < data.size();
        } else {
            nextUsed = true;
            return data.size() > 0;
        }
    }

    public int getCount() {
        return data.size();
    }

    public Boolean isLast() {
        return line == data.size() - 1;
    }

    public void first() {
        line = 0;
    }

    public void initFirst() {
        line = -1;
    }

    public String getString(String key) {
        return (String)(data.get(line)).get(key);
    }

    public Integer getInt(String key) {
        return (Integer)(data.get(line)).get(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean)(data.get(line)).get(key);
    }

    public Timestamp getTimestamp(String key) {
        return (Timestamp)(data.get(line)).get(key);
    }

    public Long getLong(String key) {
        return (Long)(data.get(line)).get(key);
    }

    public Blob getBlob(String key) {
        return (Blob)(data.get(line)).get(key);
    }

    public Object getObject(String key) {
        return (data.get(line)).get(key);
    }

    public Date getDate(String key) {
        return (Date) data.get(line).get(key);
    }

    public Double getDouble(String key) {
        return (Double) data.get(line).get(key);
    }

    public int size() {
        return data.size();
    }
}

