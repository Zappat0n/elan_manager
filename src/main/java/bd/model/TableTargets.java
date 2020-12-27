package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableTargets extends MyTable {
    public static final String table_name = "Targets";
    public static final String id = "id";
    public static final String name = "name";
    public static final String nombre = "nombre";
    public static final String NC = "NC";
    public static final String subarea = "subarea";
    public static final String year = "year";
    private static final String[] fields = {id, name, nombre, NC, subarea, year};
    private static final String[] field_def = {"INT", "VARCHAR(45)", "VARCHAR(45)", "INT", "INT", "DOUBLE"};
    private static final String key = id;

    public TableTargets() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
