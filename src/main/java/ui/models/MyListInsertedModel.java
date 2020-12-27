package ui.models;

import bd.model.TablePresentations_sub;
import ui.AddDataForm;
import bd.BDManager;
import bd.model.TableEvents;
import bd.model.TableEvents_type;
import bd.model.TableStudents;
import utils.MyLogger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by robot on 24/02/17.
 */
public class MyListInsertedModel extends DefaultListModel<String> {
    private static final String TAG = MyListInsertedModel.class.getSimpleName();
    private final AddDataForm form;
    //INSERT INTO `mbsm`.`Events` (`id`, `date`, `student`, `event_type`, `event_id`, `notes`) VALUES (NULL, NULL, NULL, NULL, NULL, NULL);
    private final ArrayList<String> data;

    public MyListInsertedModel(AddDataForm form) {
        this.form = form;
        data = new ArrayList<>();
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public String getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public String remove(int index) {
        String result = data.remove(index);
        fireIntervalRemoved(this, index, index);
        return result;
    }

    private void addData(Integer id, java.sql.Date date, String student, String event_type, String event_id,
                         String event_sub, String notes) {
        String cadena = id + "; " + date + "; " + student + "; " + event_type + "; " + event_id + "; "
                + (event_sub == null ? "" : event_sub + "; ")+ notes;
        data.add(0, cadena);
        fireIntervalAdded(this, data.size() - 1, data.size());
    }
    /*
    SELECT * FROM Events LEFT JOIN Students ON Events.student=Students.id
        LEFT JOIN Events_type ON Events.event_type=Events_type.id
        LEFT JOIN Presentations ON Events.event_id=Presentations.id
        WHERE Events.id =
     */
    public void addData(Connection co, int[] indices, Boolean with_subs) {
        try {
            if (co == null) co = AddDataForm.bdManager.connect();
            Statement st = co.createStatement();
            String event_id = null;
            String eventLabel = null;
            if (form.listMontessori_NC.getSelectedIndex() == 0) {
                event_id = " LEFT JOIN Presentations ON Events.event_id=Presentations.id ";
                eventLabel = BDManager.tablePresentations.getName() + ".name";
            } else if (form.listMontessori_NC.getSelectedIndex() == 1) {
                event_id = " LEFT JOIN Outcomes ON Events.event_id=Outcomes.id ";
                eventLabel = BDManager.tableOutcomes.getName() + ".name";
            } else if (form.listMontessori_NC.getSelectedIndex() == 2) {
                event_id = " LEFT JOIN Targets ON Events.event_id=Targets.id ";
                eventLabel = BDManager.tableNC_targets.getName() + ".name";
            } else if (form.listMontessori_NC.getSelectedIndex() == 3) {
                event_id = " LEFT JOIN Observations ON Events.event_id=Observations.id ";
                eventLabel = BDManager.tableObservations.getName() + ".name";
            }

            String query = "SELECT * FROM Events LEFT JOIN Students ON Events.student=Students.id " +
                    " LEFT JOIN Events_type ON Events.event_type=Events_type.id " + event_id +
                    (with_subs ? " LEFT JOIN Presentations_sub ON Events.event_sub=Presentations_sub.id " : "") +
                    " WHERE " + BDManager.getSqlOrValues("Events.id", indices);
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt(BDManager.tableEvents.getName() + "." + TableEvents.id);
                java.sql.Date date = rs.getDate(BDManager.tableEvents.getName() + "." + TableEvents.date);
                String student = rs.getString(BDManager.tableStudents.getName() + "." + TableStudents.name);
                String event_type = rs.getString(BDManager.tableEvents_type.getName() + "." + TableEvents_type.name);
                event_id = rs.getString(eventLabel);
                String event_sub = (with_subs ? rs.getString(BDManager.tablePresentations_sub.getName() + "." + TablePresentations_sub.name) : null);
                String notes = rs.getString(BDManager.tableEvents.getName() + "." + TableEvents.notes);
                addData(id, date, student, event_type, event_id, event_sub, notes);
            }
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        }
    }
}

