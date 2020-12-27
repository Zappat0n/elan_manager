package bd;

import java.util.ArrayList;

/**
 * Created by angel on 30/10/15.
 */
public class MyTable {
    private static final String[] numeric = {"INT", "BIGINT", "BOOLEAN"};


    private final String name;
    public final String[] fields;
    public final String[] field_def;
    private final String key;

    protected MyTable(String table_name, String[][] fields, String key) {
        name = table_name;
        this.fields      = fields[0];
        this.field_def   = fields[1];
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getSQLStringCreateTable(){
        if (fields.length == 0) return null;
        StringBuilder query = new StringBuilder("CREATE  TABLE " + name + " (");
        for (int i = 0; i < fields.length; i++) {
            query.append(fields[i]).append(" ").append(field_def[i]).append(", \n");
        }

        query.append("PRIMARY KEY (").append(key).append("))");
        return query.toString();
    }

    public String getValues(String keys, String condition, String order){
        String query = "SELECT " + keys + " FROM " + name ;
        if (condition != null) query += " WHERE " + condition;
        if (order != null) query += " " + order;
        return query;
    }

    public String getValues(String keys, String condition){
        String query = "SELECT " + keys + " FROM " + name ;
        if (condition != null) query += " WHERE " + condition;
        return query;
    }

    public String getValues(String condition){
        String query = "SELECT * FROM " + name ;
        if (condition != null) query += " WHERE " + condition;
        return query;
    }

    public String removeValue(String condition){
        if (condition != null) return  "DELETE FROM " + name + " WHERE " + condition;
        else return  "DELETE FROM " + name;
    }

    public String addValues(String[] keys, ArrayList<String[]> values){
        if (keys == null || values == null ) return null;
        if (keys.length == 0 || values.size() == 0 ) return null;

        StringBuilder query = new StringBuilder("INSERT INTO " + name);

        if (key != null){
            query.append(" (").append(keys[0]);
            for (int i = 1; i < keys.length; i++) query.append(",").append(keys[i]);
            query.append(")");
        }

        query.append(" VALUES ").append(getSQLValues(keys, values.get(0)));

        String value;
        for (int i = 1; i < values.size(); i++) {
            value = getSQLValues(keys, values.get(i));
            if (value != null) query.append(",").append(value);
            else return null;
        }
        return query.toString();
    }

    public String updateValue(String[] keys, String[] value){
        if (value.length == 0 ) return null;
        String query = "UPDATE " + name + " SET ";
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            if (!keys[i].equals(key)) lines.add(keys[i] + "=" + getSQLValue(keys[i], value[i]));
        }
        String condition = getConditionPrimaryKey(keys, value);
        if (condition != null) return query + getStringWithCommas(lines) + " WHERE " + condition;
        else {
            return null;
        }
    }

    public String updateValue(String[] keys, String[] value, String condition){
        if (value.length == 0 ) return null;
        String query = "UPDATE " + name + " SET ";
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            if (!keys[i].equals(key)) lines.add(keys[i] + "=" + getSQLValue(keys[i], value[i]));
        }
        return query + getStringWithCommas(lines) + " WHERE " + condition;
    }

    public String addValue(String[] key, String[] value){
        if (value.length == 0 ) return null;
        StringBuilder query = new StringBuilder("INSERT INTO " + name);
        int length = Math.min(key.length, value.length);
        if (length > 0){
            query.append(" (").append(key[0]);
            for (int i = 1; i < length; i++) query.append(",").append(key[i]);
            query.append(")");
        }
        query.append(" VALUES ").append(getSQLValues(key, value));
        return query.toString();
    }

    private String getStringWithCommas(ArrayList<String> lines){
        StringBuilder res = new StringBuilder(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            res.append(",").append(lines.get(i));
        }
        return res.toString();
    }

    private String getConditionPrimaryKey(String[] keys, String[] value){
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) return key + "=" + getSQLValue(key, value[i]);
        }
        return null;
    }

    private String getSQLValue(String key, String value){
        int col = fieldIndex(key);
        if (col != -1){
            if (isFieldNumeric(field_def[col], value)) return value;
            else if (value.contains("'"))
                return "\"" + value + "\"";
            else if (value.contains("\""))
                return "'" + value + "'";
            else return "'" + value + "'";
        } else return null;
    }

    private String getSQLValues(String[] key, String[] item){
        int length = Math.min(key.length, item.length);
        if (length==0) return null;

        int col = fieldIndex(key[0]);
        StringBuilder values = new StringBuilder("(" + (isFieldNumeric(field_def[col], item[0]) ? item[0] : "'" + item[0] + "'"));
        for (int i = 1; i < length; i++) {
            if (isFieldNumeric(field_def[fieldIndex(key[i])], item[i])) values.append(",").append(!item[i].equals("") ? item[i] : "NULL");
                    else values.append(",").append(!item[i].equals("") ? "'" + item[i] + "'" : "NULL");
        }
        return values + ")";
    }

    private Boolean isFieldNumeric(String field, String value){
        if (value == null) return true;
        for (String aNumeric : numeric) {
            if (field.startsWith(aNumeric)) return true;
        }
        return false;
    }

    private int fieldIndex(String value) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals(value)) return i;
        }
        return -1;
    }
}
