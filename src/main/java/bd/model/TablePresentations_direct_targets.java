package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TablePresentations_direct_targets extends MyTable {
    private static final String table_name = "Presentations_direct_targets";
    private static final String id = "id";
    private static final String presentation = "presentation";
    private static final String target = "target";
    private static final String[] fields = {id, presentation, target};
    private static final String[] field_def = {"INT", "INT", "INT"};
    private static final String key = id;

    public TablePresentations_direct_targets() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
