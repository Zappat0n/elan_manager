package ui.formReports.managers;

import utils.CacheManager;

import java.sql.Date;
import java.util.Calendar;

public class ReportManager {
    public static final String header = "Informe del [TERM] de [STUDENT]";
    public static final String body = "Estimada familia,\n\n" +
            "Adjuntamos la evaluación del [TERM].\n" +
            "Si queréis aportar vuestro propio comentario sobre lo que observáis en vuestro hijo, lo podéis hacer " +
            "contestando a este correo.\n\n" +
            "Muchas gracias y un saludo,\n\n" +
            "ELAN Montessori British School\n" +
            "Carril Rincona, Rincón de Beniscornia, 30108, Murcia";
    final CacheManager cacheManager;
    final Date date;
    final Integer student;
    final int month;
    final int year;

    public ReportManager(CacheManager cacheManager, Date date, Integer student) {
        this.cacheManager = cacheManager;
        this.date = date;
        this.student = student;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        month = cal.get(Calendar.MONTH)+1;
        year = cal.get(Calendar.YEAR);
    }

    protected Date getInitialDate(){
        int ini;
        if (month >= 2 && month <= 4) ini = 1;
        else if (month >= 5 && month <= 9) ini = 4;
        else ini = 9;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, ini - 1);
        cal.set(Calendar.YEAR, (ini != 9 ? year - 1: year));
        return new Date(cal.getTime().getTime());
        /*
        if (month >= 2 && month <= 4) ini = 2;
        else if (month >= 5 && month <= 9) ini = 5;
        else ini = 9;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, ini - 1);
        cal.set(Calendar.YEAR, (ini != 9 ? year : year - 1));
        return new Date(cal.getTime().getTime());
        */
    }

    protected Date getInitialEOYDate(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (month < 8) cal.set(Calendar.YEAR, year - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, 8);
        return new Date(cal.getTime().getTime());
    }

    protected Date getFinalDate(){
        int end;
        if (month >= 2 && month <= 4) end = 3;
        else if (month >= 5 && month <= 9) end = 6;
        else end = 12;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 30);
        cal.set(Calendar.MONTH, end - 1);
        cal.set(Calendar.YEAR, year);
        return new Date(cal.getTime().getTime());
        /*
        if (month >= 2 && month <= 4) end = 4;
        else if (month >= 5 && month <= 9) end = 8;
        else end = 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 28);
        cal.set(Calendar.MONTH, end - 1);
        cal.set(Calendar.YEAR, year);
        return new Date(cal.getTime().getTime());
         */
    }

    protected Date getFinalEOYDate(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (month > 7) cal.set(Calendar.YEAR, year + 1);
        cal.set(Calendar.DAY_OF_MONTH, 30);
        cal.set(Calendar.MONTH, 6);
        return new Date(cal.getTime().getTime());
    }

    public Integer getTerm(){
        if (month >= 2 && month <= 4) return 2;
        else if (month >= 5 && month <= 9) return 3;
        else return 1;
    }

    public String getAcademicYears(){
        if (getTerm() == 1) return  year + "/" + (year-2000+1);
        else return (year-1) + "/" + (year-2000);
    }

    public String getStringTerm(){
        if (month >= 2 && month <= 4) return "Segundo Trimestre";
        else if (month >= 5 && month <= 9) return "Tercer Trimestre";
        else return "Primer Trimestre";
    }

    public String getTextForEmail(String text, Integer student) {
        text = text.replace("[TERM]", getStringTerm());
        text = text.replace("[STUDENT]", (String)cacheManager.students.get(student)[0]);
        return text;
    }
}
