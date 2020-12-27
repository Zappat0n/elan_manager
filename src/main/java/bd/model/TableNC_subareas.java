package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableNC_subareas extends MyTable {
    public static final String table_name = "NC_subareas";
    public static final String id = "id";
    public static final String name = "name";
    public static final String nombre = "nombre";
    public static final String area = "area";
    private static final String[] fields = {id, name, nombre, area};
    private static final String[] field_def = {"INT", "VARCHAR(120)", "VARCHAR(120)", "INT"};
    private static final String key = id;

    public TableNC_subareas() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
