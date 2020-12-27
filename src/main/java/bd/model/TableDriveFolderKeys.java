package bd.model;

import bd.MyTable;

public class TableDriveFolderKeys extends MyTable {
    public static final String table_name = "DriveFolderKeys";
    public static final String folder = "folder";
    public static final String secret = "secret";
    private static final String[] fields = {folder, secret};
    private static final String[] field_def = {"VARCHAR(45)", "VARCHAR(45)"};
    private static final String key = folder;


    public TableDriveFolderKeys() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
