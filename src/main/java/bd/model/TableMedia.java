package bd.model;

import bd.MyTable;

public class TableMedia extends MyTable {
    public static final String table_name = "Media";
    public static final String id = "id";
    public static final String date = "date";
    public static final String student = "student";
    public static final String presentation = "presentation";
    public static final String presentation_sub = "presentation_sub";
    public static final String comment = "comment";
    public static final String fileId = "fileId";
    public static final String[] fields = {id, date, student, presentation, presentation_sub, comment, fileId};
    private static final String[] field_def = {"INT", "DATE", "INT", "INT", "INT", "LONGTEXT", "LONGTEXT"};
    private static final String key = id;

    public TableMedia() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
