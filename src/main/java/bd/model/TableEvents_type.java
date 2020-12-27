package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableEvents_type extends MyTable {
    private static final String table_name = "Events_type";
    private static final String id = "id";
    public static final String name = "name";
    private static final String[] fields = {id, name};
    private static final String[] field_def = {"INT", "VARCHAR(45)"};
    private static final String key = id;

    public TableEvents_type() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
