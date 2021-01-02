package ui.formReports;

import bd.BDManager;
import bd.model.TableEvents;
import drive.DriveGovernor;
import pdfs.models.Pdf_FollowUpReports;
import ui.formReports.managers.ReportManager;
import ui.formReports.models.Pdf_EoY_Reports;
import ui.formReports.models.Pdf_Yet_Reports;
import utils.CacheManager;
import utils.EmailManager;
import utils.MyLogger;
import utils.SettingsManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SwingReportGenerator extends SwingWorker<Object, Object> implements PropertyChangeListener {
    private static final String TAG = SwingReportGenerator.class.getSimpleName();
    final BDManager bdManager;
    final CacheManager cacheManager;
    final SettingsManager settingsManager;
    final Integer classroom;
    final Date reportDate;
    final Date changeDate;
    final Boolean recordDate;
    final Boolean sendEmail;
    BufferedImage logo;
    final Boolean doYetReport;
    final Boolean doTargetsReport;
    final Boolean doEoYReport;
    final Boolean doEoYComments;
    final Boolean doEoYPhotos;
    final Boolean checkContacts;
    final Boolean uploadToDrive;
    final JFrame frame;
    final JProgressBar progressBar;
    Integer max;
    final Integer student;
    final String header;
    final String body;
    final ArrayList<Integer> students;
    final DefaultListModel<String> log;

    public SwingReportGenerator(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager,
                                JFrame frame, Integer student, Integer classroom, Date reportDate, Date changeDate,
                                Boolean recordDate, Boolean sendEmail, Boolean doYetReport, Boolean doTargetsReport,
                                Boolean doEoYReport, Boolean doEoYComments, Boolean doEoYPhotos, Boolean checkContacts,
                                Boolean uploadToDrive, String header, String body, JProgressBar progressBar,
                                DefaultListModel<String> log) {
        this.bdManager = bdManager;
        this.cacheManager = cacheManager;
        this.settingsManager = settingsManager;
        this.classroom = classroom;
        this.reportDate = reportDate;
        this.changeDate = changeDate;
        this.recordDate = recordDate;
        this.sendEmail = sendEmail;
        this.doYetReport = doYetReport;
        this.doTargetsReport = doTargetsReport;
        this.doEoYReport = doEoYReport;
        this.doEoYComments = doEoYComments;
        this.doEoYPhotos = doEoYPhotos;
        this.checkContacts = checkContacts;
        this.frame = frame;
        this.progressBar = progressBar;
        this.student = student;
        this.header = header;
        this.body = body;
        this.log = log;
        this.uploadToDrive = uploadToDrive;
        if (student == null) students = cacheManager.studentsPerClassroom.get(classroom);
        else {
            students = new ArrayList<>();
            students.add(student);
        }
        try {
            logo = ImageIO.read(getClass().getResourceAsStream("logo.png"));
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
    }

    @Override
    protected Object doInBackground() {
        Connection co = null;
        File f1 = null;
        File f2 = null;
        File f3 = null;
        Integer teacher = null;
        try {
            co = bdManager.connect();
            if (checkContacts && checkContacts(co)) {
                BDManager.closeQuietly(co);
                return null;
            }
            int i = 0;
            max = students.size();
            progressBar.setMaximum(max);
            for (Integer studentId : students) {
                ReportManager reportManager = new ReportManager(cacheManager, new java.sql.Date(reportDate.getTime()), student);
                setProgress(i++);
                if (doYetReport) {
                    Pdf_Yet_Reports pdf1 = new Pdf_Yet_Reports(bdManager, co, cacheManager, settingsManager, studentId, classroom,
                            reportDate, logo, reportManager, log);
                    teacher = pdf1.teacher;
                    f1 = (!pdf1.isEmpty ? new File(pdf1.createDocument()) : null);
                }
                if (doTargetsReport) {
                    Pdf_FollowUpReports pdf2 = new Pdf_FollowUpReports(bdManager, cacheManager, settingsManager, co, studentId, classroom,
                            reportDate, changeDate, logo, false, log);
                    f2 = new File(pdf2.createDocument());
                }
                if (doEoYReport) {
                    Pdf_EoY_Reports pdf3 = new Pdf_EoY_Reports(bdManager, co, cacheManager,  settingsManager, studentId,
                            classroom, reportDate, logo, reportManager, doEoYComments, doEoYPhotos, log);
                    f3 = new File(pdf3.createDocument());
                }
                if (f1!=null || f2!=null || f3!=null) {
                    if (sendEmail)
                        EmailManager.sendEmail(co, studentId, new File[] {f1,f2, f3},
                                reportManager.getTextForEmail(header, studentId), reportManager.getTextForEmail(body, studentId));
                    if (uploadToDrive) {
                        File finalF1 = f1; File finalF2 = f2; File finalF3 = f3;
                        SwingUtilities.invokeLater(() -> {
                            try {
                                DriveGovernor driveGovernor = new DriveGovernor(bdManager, cacheManager, null);
                                if (finalF1 != null) driveGovernor.uploadReport(studentId, finalF1);
                                if (finalF2 != null) driveGovernor.uploadReport(studentId, finalF2);
                                if (finalF3 != null) driveGovernor.uploadReport(studentId, finalF3);
                            } catch (Exception ex) {
                                MyLogger.e(TAG, ex);
                            }
                        });
                    }
                } else MyLogger.d("Error generating form", "Student: " + studentId);

                if (recordDate && changeDate != null)
                    bdManager.addValue(co, BDManager.tableEvents, new String[]{TableEvents.date, TableEvents.student,
                            TableEvents.event_type, TableEvents.event_id, TableEvents.event_sub, TableEvents.notes,
                            TableEvents.teacher}, new String[]{new java.sql.Date(changeDate.getTime()).toString(),
                            String.valueOf(studentId), "15","NULL","NULL","NULL", teacher != null ? String.valueOf(teacher) : null});
            }
            setProgress(i);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co);
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            progressBar.setString(String.format("Completed %d%% of task.\n", progress * 100 / max));
        }
    }

    private Boolean checkContacts(Connection co) {
        ArrayList<Integer> pending = new ArrayList<>();
        HashMap<Integer, ArrayList<String>> contacts = bdManager.getContacts(co, students);
        for (Integer student : students) {
            if (!contacts.containsKey(student)) pending.add(student);
        }
        if (pending.size() > 0) {
            StringBuilder names = new StringBuilder();
            for (Integer id : pending) {
                names.append(cacheManager.students.get(id)[0]).append("\n");
            }
            JOptionPane.showInternalMessageDialog(frame, "There are children without contact data", names.toString(),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else return true;
    }
}
