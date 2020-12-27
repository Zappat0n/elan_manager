package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableEvents extends MyTable {
    public static final String table_name = "Events";
    public static final String id = "id";
    public static final String date = "date";
    public static final String student = "student";
    public static final String event_type = "event_type";
    public static final String event_id = "event_id";
    public static final String event_sub = "event_sub";
    public static final String notes = "notes";
    public static final String teacher = "teacher";
    public static final String[] fields = {id, date, student, event_type, event_id, event_sub, notes, teacher};
    private static final String[] field_def = {"INT", "DATE", "INT", "INT", "INT", "INT", "LONGTEXT", "INT"};
    private static final String key = id;

    public TableEvents() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}

