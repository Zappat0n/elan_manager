package utils;

import main.ApplicationLoader;
import utils.data.Presentation;
import utils.planner.PresentationsTree;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlanningManager {
    private static final String TAG = PlanningManager.class.getSimpleName();
    int student;
    public Double age;
    public String name;
    private Date start;
    private Date end;

    public PlanningManager(int student, Date start, Date end) {
        this.student = student;
        this.start = start;
        this.end = end;

        Object[] data = ApplicationLoader.cacheManager.students.get(student); //name, birthday, drive_main, drive_documents, drive_photos, drive_reports
        age = getAge((Date) data[1]);
        name = (String) data[0];
        int weeks = (int)ChronoUnit.WEEKS.between(start.toLocalDate(), end.toLocalDate());
        PresentationsTree tree = new PresentationsTree(student, age);
        int presentations = Math.round(tree.size * 0.66f);
        ArrayList<Presentation> list = tree.listPresentations(presentations);
        int count = 1;
        int week = 1;
        int presentationPerWeek = Math.round(presentations/weeks);
        MyLogger.d("*** ", "Week: 1");
        for (Presentation ps : list) {
            if (count%presentationPerWeek == 0) MyLogger.d("*** ", "Week: " + ++week);
            String sub = null;
            String name = (String) ApplicationLoader.cacheManager.presentations.get(ps.presentation)[0];
            if (ps.sub != null) {
                sub = (String) ApplicationLoader.cacheManager.presentationsSub.get(ps.sub)[0];
            }
            MyLogger.d("- ", (count++)+"." + name + (sub != null ? " : " + sub :""));
        }
    }

    public Double getAge(Date birthday) {
        long diffInMillies = Math.abs(new Date(new java.util.Date().getTime()).getTime() - birthday.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return (double) diff / 365;
    }
}