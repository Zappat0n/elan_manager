package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TablePresentations_areas extends MyTable {
    public static final String table_name = "Presentations_areas";
    public static final String id = "id";
    public static final String name = "name";
    public static final String nombre = "nombre";
    private static final String[] fields = {id, name, nombre};
    private static final String[] field_def = {"INT", "VARCHAR(45)", "VARCHAR(45)"};
    private static final String key = id;

    public TablePresentations_areas() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
