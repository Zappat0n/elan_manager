package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableOutcomes extends MyTable {
    public static final String table_name = "Outcomes";
    public static final String id = "id";
    public static final String name = "name";
    public static final String nombre = "nombre";
    public static final String subarea = "subarea";
    public static final String start_month = "start_month";
    public static final String end_month = "end_month";
    private static final String[] fields = {id, name, nombre, subarea, start_month, end_month};
    private static final String[] field_def = {"INT", "VARCHAR(45)", "VARCHAR(45)", "INT", "INT", "INT"};
    private static final String key = id;

    public TableOutcomes() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
