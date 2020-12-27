package ui.formReports.managers;

import bd.BDManager;
import bd.model.TableEventsYet;

import java.sql.Date;

public interface IManager {
    public String[] load();
    public void save();
    public void setDate(Date date);
}
