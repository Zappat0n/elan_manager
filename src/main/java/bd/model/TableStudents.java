package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TableStudents extends MyTable {
    //id, name, birth_date, classroom, address, chronic diseases, medical treatment, allergies or dietary restrictions, special needs, taking medications, firstday_snails, firstday_cdb, firstday_primary, exit_date, notes
    public static final String table_name = "Students";
    public static final String id = "id";
    public static final String name = "name";
    public static final String birth_date = "birth_date";
    public static final String classroom = "classroom";
    public static final String address = "address";
    public static final String diseases = "chronic diseases";
    public static final String medical_treatment = "medical treatment";
    public static final String allergies = "allergies or dietary restrictions";
    public static final String special_needs = "special needs";
    public static final String taking_medications = "taking medications";
    public static final String firstday_snails = "firstday_snails";
    public static final String firstday_cdb = "firstday_cdb";
    public static final String firstday_primary = "firstday_primary";
    public static final String exit_date = "exit_date";
    public static final String notes = "notes";
    public static final String drive_main = "drive_main";
    public static final String drive_documents = "drive_documents";
    public static final String drive_photos = "drive_photos";
    public static final String drive_reports = "drive_reports";
    private static final String[] fields = {id, name, birth_date, classroom, address, diseases, medical_treatment,
            allergies, special_needs, taking_medications, firstday_snails, firstday_cdb, firstday_primary, exit_date,
            notes, drive_main, drive_documents, drive_photos, drive_reports};
    private static final String[] field_def = {"INT", "VARCHAR(45)", "DATE", "INT", "VARCHAR(200)", "VARCHAR(45)",
            "VARCHAR(45)", "VARCHAR(45)", "VARCHAR(45)", "VARCHAR(45)", "DATE", "DATE", "DATE", "DATE", "LONGTEXT",
            "VARCHAR(64)", "VARCHAR(64)", "VARCHAR(64)", "VARCHAR(64)"};
    private static final String key = id;

    public TableStudents() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}