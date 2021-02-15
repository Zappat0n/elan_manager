package bd;

import bd.model.TableEvents;

public class EventCondition {
    public final Integer event_id;
    public final Integer event_sub;
    public final Integer student;

    public EventCondition(Integer event_id, Integer event_sub, Integer student) {
        this.event_id = event_id;
        this.event_sub = event_sub;
        this.student = student;
    }

    public String getCode() {
        return event_id + "." + (event_sub != null ? event_sub : "0");
    }

    public String getBasicCondition() {
        return getBasicCondition(student, event_id, event_sub);
    }

    public static String getCondition(Integer student, Integer event_type, Integer event_id, Integer event_sub) {
        return getBasicCondition(student, event_id, event_sub) + " AND " +
                TableEvents.event_type + " = " + event_type;
    }

    public static String getBasicCondition(Integer student, Integer event_id, Integer event_sub) {
        return TableEvents.student + "=" + student + " AND " +
                TableEvents.event_id + "=" + event_id + " AND " +
                (event_sub!=null?TableEvents.event_sub + "=" + event_sub:TableEvents.event_sub + " IS NULL");
    }
}
