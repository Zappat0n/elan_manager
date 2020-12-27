package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableTeachers extends MyTable {
    public static final String table_name = "Teachers";
    public static final String id = "id";
    public static final String name = "name";
    public static final String nick = "nick";
    public static final String classroom = "classroom";
    private static final String[] fields = {id, name, nick, classroom};
    private static final String[] field_def = {"INT", "VARCHAR(45)", "VARCHAR(45)", "INT"};
    private static final String key = id;

    public TableTeachers() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
