package bd.model;

import bd.MyTable;

/**
 * Created by angel on 17/04/17.
 */
public class TableContacts extends MyTable {
    public static final String table_name = "Contacts";
    public static final String id = "id";
    public static final String name = "name";
    public static final String email = "email";
    public static final String mobile_phone = "mobile_phone";
    public static final String job = "job";
    public static final String student1 = "student1";
    public static final String student2 = "student2";
    public static final String student3 = "student3";
    public static final String student4 = "student4";
    public static final String student5 = "student5";
    public static final String notes = "notes";

    private static final String[] fields = {id, name, email, mobile_phone, job, student1, student2,
            student3, student4, student5, notes};
    private static final String[] field_def = {"INT", "VARCHAR(45)", "VARCHAR(45)", "VARCHAR(45)",
            "VARCHAR(45)", "INT", "INT", "INT", "INT", "INT", "LONGTEXT"};
    private static final String key = id;

    public TableContacts() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}