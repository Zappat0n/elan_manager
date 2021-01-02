package bd.model;

import bd.MyTable;

public class TableLinks extends MyTable {
    public static final String table_name = "Links";
    public static final String presentation = "presentation";
    public static final String presentation_sub = "presentation_sub";
    public static final String outcomes = "Outcomes";
    public static final String targets = "Targets";
    public static final String comment = "Comment";
    private static final String[] fields = {presentation, presentation_sub, outcomes, targets, comment};
    private static final String[] field_def = {"INT", "INT", "INT", "INT", "LONGTEXT"};
    private static final String key = presentation + "," + presentation_sub + "," + outcomes + "," + targets;

    public TableLinks() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
