package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableEventsEoY extends MyTable {
    public static final String table_name = "EventsEoYReport";
    public static final String id = "id";
    public static final String date = "date";
    public static final String student = "student";
    public static final String event_id = "event_id";
    public static final String notes = "notes";
    public static final String teacher = "teacher";
    public static final String[] fields = {id, date, student, event_id, notes, teacher};
    private static final String[] field_def = {"INT", "DATE", "INT", "INT", "LONGTEXT", "INT"};
    private static final String key = id;

    public TableEventsEoY() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}

