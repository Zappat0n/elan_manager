package bd.model;

import bd.MyTable;

/**
 * Created by angel on 23/02/17.
 */
public class TableObservations extends MyTable {
    //id, date, student, teacher, type, notes
    public static final String table_name = "Observations";
    public static final String id = "id";
    public static final String name = "name";
    private static final String[] fields = {id, name};
    private static final String[] field_def = {"INT", "VARCHAR(45)"};
    private static final String key = id;

    public TableObservations() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
