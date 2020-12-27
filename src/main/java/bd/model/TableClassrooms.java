package bd.model;

import bd.MyTable;

/**
 * Created by angel on 6/02/17.
 */
public class TableClassrooms extends MyTable{
    public static final String table_name = "Classrooms";
    public static final String id = "id";
    public static final String name = "name";
    private static final String[] fields = {id, name};
    private static final String[] field_def = {"INT", "VARCHAR(45)"};
    private static final String key = id;

    public TableClassrooms() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
