package ui.formClassroom;

import bd.BDManager;
import bd.model.TableEvents;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Calendar;

public class MyPopUpMenuPlanning extends JPopupMenu implements ActionListener {
    private final static String TAG = MyPopUpMenuPlanning.class.getSimpleName();
    private static BDManager bdManager;
    final int student;
    final String event;
    final Integer event_id;
    final Integer event_sub;
    int oldvalue;
    final JTable tablePresentations;
    final JTable tablePlanning;
    final int row;
    final int column;
    final Date date;
    private final ClassroomFormData formData;

    public MyPopUpMenuPlanning(BDManager bdManager, int student, String event, JTable tablePresentations,
                               JTable tablePlanning, int row, int column, Date date, ClassroomFormData formData) {
        this.formData = formData;
        MyPopUpMenuPlanning.bdManager = bdManager;
        this.student = student;
        this.event = event;
        this.tablePresentations = tablePresentations;
        this.tablePlanning = tablePlanning;
        this.row = row;
        this.column = column;
        this.date = date;
        String[] ev = event.split("[.]");
        event_id = (ev[0].equals("")) ? null: Integer.valueOf(ev[0]);
        event_sub = (event_id == null || ev[1].equals("0")) ? null : Integer.valueOf(ev[1]);
        String[] values = {"Delete"};
        for (int i = 0; i < values.length; i++) {
            JMenuItem item = new JMenuItem(values[i]);
            item.addActionListener(this);
            add(item);
            if (oldvalue == i) item.setSelected(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        removeValue(student, event_id, event_sub, date, MyPopUpMenuPresentations.planning_values[column-1]);
        tablePlanning.getModel().setValueAt(null, row, column);
        String id = event_id + "." + ((event_sub != null) ? event_sub : 0);
        int row = formData.presentations.indexOf(id);
        if (row != -1) {
            int col = formData.students.indexOf(student)+1;
            tablePresentations.setValueAt(0, row, col);
        }
    }

    public static void removeValue(Integer student, Integer event_id, Integer event_sub, Date date, String planning_value) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        java.sql.Date monday = new java.sql.Date(cal.getTime().getTime());
        Connection connection = null;
        try {
            connection = bdManager.connect();
            bdManager.removeValue(connection, BDManager.tableEvents,
                    TableEvents.student + "=" + student + " AND " +
                            TableEvents.event_id + "=" + event_id + " AND " +
                            (event_sub!=null?TableEvents.event_sub + "=" + event_sub:TableEvents.event_sub + "IS NULL") + " AND " +
                            TableEvents.date + " >= '" + monday + "' AND " +
                            TableEvents.event_type + " = 13 AND INSTR(notes, '" +
                            planning_value + "')"
                    , false);
        } finally {
            BDManager.closeQuietly(connection);
        }
    }
}
