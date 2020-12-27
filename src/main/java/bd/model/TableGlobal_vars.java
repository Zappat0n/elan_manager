package bd.model;

import bd.MyTable;

public class TableGlobal_vars extends MyTable {
    public static final String table_name = "Global_vars";
    public static final String year = "year";
    public static final String name = "name";
    public static final String value = "value";
    private static final String[] fields = {year, name, value};
    private static final String[] field_def = {"INT", "VARCHAR(16)", "VARCHAR(32)"};
    private static final String key = year+","+name;

    public TableGlobal_vars() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
