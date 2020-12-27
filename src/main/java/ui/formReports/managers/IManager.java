package ui.formReports.managers;

import java.sql.Date;

public interface IManager {
    String[] load();
    void save();
    void setDate(Date date);
}
