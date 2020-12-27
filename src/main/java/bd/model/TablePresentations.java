package bd.model;

import bd.MyTable;

/**
 * Created by angel on 7/02/17.
 */
public class TablePresentations extends MyTable {
    //id, name, year, year_end, area, subarea, priority
    public static final String table_name = "Presentations";
    public static final String id = "id";
    public static final String name = "name";
    public static final String subarea = "subarea";
    public static final String year = "year";
    public static final String year_end = "year_end";
    public static final String priority = "priority";
    public static final String nombre = "nombre";
    public static final String description = "description";
    public static final String nc1 = "nc1";
    public static final String nc2 = "nc2";
    public static final String nc3 = "nc3";
    public static final String nc4 = "nc4";
    public static final String nc5 = "nc5";
    private static final String[] fields = {id, name, subarea, year, year_end, priority, nombre, description, nc1,
            nc2, nc3, nc4, nc5};
    private static final String[] field_def = {"INT", "VARCHAR(120)", "INT", "DOUBLE", "DOUBLE", "INT", "VARCHAR(120)",
            "TINYTEXT", "INT", "INT", "INT", "INT", "INT"};
    private static final String key = id;

    public TablePresentations() {
        super(table_name, new String[][]{fields, field_def}, key);
    }
}
