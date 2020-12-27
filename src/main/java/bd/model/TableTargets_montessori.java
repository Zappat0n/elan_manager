package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableTargets_montessori extends MyTable {
    private static final String table_name = "Targets_montessori";
    private static final String id = "id";
    private static final String name = "name";
    private static final String age = "age";
    private static final String[] fields = {id, name, age};
    private static final String[] field_def = {"INT", "VARCHAR(45)", "INT2"};
    private static final String key = id;

    public TableTargets_montessori() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
