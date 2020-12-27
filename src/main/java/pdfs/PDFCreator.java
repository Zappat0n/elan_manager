package pdfs;

import bd.BDManager;
import bd.MySet;
import bd.model.TableContacts;
import bd.model.TableEvents;
import pdfs.models.*;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.Properties;

/**
 * Created by angel on 8/04/17.
 */
public class PDFCreator {
    private static final String TAG = PDFCreator.class.getSimpleName();
    private static final String from = "reports@elanmontessori.org";
    private static final String host = "elanmontessori.org";
    private static final String user = "reports@elanmontessori.org";
    private static final String p    = "tXoLM80fKqK6";
    private static final String header1 = "Elan Montessori informe trimestral de ";
    private static final String header2 = "End of Term reports for ";

    private static final String body0 = "Estimada familia,\n" +
            "\n" +
            "Adjuntamos la evaluación del segundo trimestre. Incluye un comentario resumen general sobre los avances del niño y un informe sobre el progreso del niño en cuanto a los objetivos a cumplir dentro de la etapa de \"Early Years\" del currículum británico.\n" +
            "\n" +
            "Si queréis aportar vuestro propio comentario sobre lo que observáis en vuestro hijo, lo podéis hacer contestando a este correo.\n" +
            "\n" +
            "Muchas gracias y un saludo,\n" +
            "\n" +
            "Marian Martínez Serrano\n" +
            "Élan School Principal\n" +
            "ELAN Montessori British School\n" +
            "Carril Rincona, Rincón de Beniscornia, 30108, Murcia";

    private static final String body1 = "Estimada familia,\n" +
            "\n" +
            "Adjuntamos la evaluación del segundo trimestre.\n" +
            "\n" +
            "La primera parte son los comentarios de progreso basados en las observaciones del equipo docente. Este tipo de evaluación se conoce como: \"evaluación formativa\" y sirve para guiar al niño y a la familia en el proceso de aprendizaje del primero, así como para ofrecer una base sobre la que trabajar objetivos para el siguiente trimestre.  La familia puede aportar sus propios comentarios acerca de sus observaciones en casa.\n" +
            "\n" +
            "La segunda parte son las evaluaciones de progreso en cuanto al currículo que siguen nuestros niños. Como ya sabéis, trabajamos dentro del marco del currículo Montessori y Británico y recogemos los datos que son necesarios para la evaluación del progreso de los niños siguiendo las indicaciones del British National Curriculum.\n" +
            "\n" +
            "De nuevo, os animamos a responder a este email incluyendo cualquier comentario u observación sobre esta segunda parte de la evaluación.\n" +
            "\n\n" +
            "Marian Martínez Serrano\n" +
            "Élan School Principal\n" +
            "ELAN Montessori British School\n" +
            "Carril Rincona, Rincón de Beniscornia, 30108, Murcia";

    private static final String body2 = "Estimada familia,\n" +
            "\n" +
            "Adjuntamos la evaluación del segundo trimestre.\n" +
            "\n" +
            "La primera parte son los comentarios de progreso basados en las observaciones de las guías. Este tipo de evaluación se conoce como: \"evaluación formativa\" y sirve para guiar al niño y a la familia en el proceso de aprendizaje del primero, así como para ofrecer una base sobre la que trabajar objetivos para el siguiente trimestre.  La familia puede aportar sus propios comentarios acerca de sus observaciones en casa.\n" +
            "\n" +
            "La segunda parte son las evaluaciones de progreso en cuanto al currículo que siguen nuestros niños. Como ya sabéis, trabajamos dentro del marco del currículo Montessori y Británico y recogemos los datos que son necesarios para la evaluación del progreso de los niños siguiendo las indicaciones del British National Curriculum.\n" +
            "\n" +
            "De nuevo, os animamos a responder este email con cualquier comentario u observación sobre esta segunda parte de la evaluación.\n" +
            "\n" +
            "Si tenéis cualquier duda, podéis consultarlo con el equipo docente en cualquier momento.\n" +
            "\n" +
            "Gracias y un saludo.\n" +
            "\n" +
            "\n" +
            "Marian Martínez Serrano\n" +
            "Élan School Principal\n" +
            "ELAN Montessori British School\n" +
            "Carril Rincona, Rincón de Beniscornia, 30108, Murcia";

    private static final String body3 = "“El niño está dotado de poderes desconocidos que pueden encaminarnos hacia un luminoso porvenir. " +
            "Si verdaderamente se quiere llevar a cabo una reconstrucción, el objeto de la educación debe ser el desarrollo de las potencialidades humanas.”\n" +
            "La Mente Absorbente del Niño. Cap. 1. Pág.2. \n" +
            "María Montessori. ";


    private static void manageFile(CacheManager cacheManager, BDManager bdManager, Connection co, SettingsManager settingsManager,
                                   Integer studentId, PDFForm_Reports form, File file, String header, String body,
                                   Date changeDate, Boolean sendEmail, Integer teacher) {
        boolean isCreated = false;
        try {
            if (co == null) {co = bdManager.connect(); isCreated = true;}
            if (sendEmail) sendEmail(cacheManager, bdManager, co, studentId, form.studentName, new File[]{file}, header, body);
            if (changeDate!=null) bdManager.addValue(co, BDManager.tableEvents,
                    new String[]{TableEvents.date, TableEvents.student, TableEvents.event_type, TableEvents.event_id, TableEvents.event_sub, TableEvents.notes, TableEvents.teacher},
                    new String[]{new java.sql.Date(form.changeDate.getTime()).toString(),
                            String.valueOf(studentId), "15","NULL","NULL","NULL", String.valueOf(teacher)});
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            if (isCreated) BDManager.closeQuietly(co);
        }
    }

    private static void manageFiles(CacheManager cacheManager, BDManager bdManager, Connection co, SettingsManager settingsManager,
                                    Integer studentId, PDFForm_Reports form, File[] files, String body,
                                    Date changeDate, Boolean sendEmail, Integer teacher) {
        boolean created = false;
        if (co == null) { co = bdManager.connect(); created = true; }
        if (sendEmail) sendEmail(cacheManager, bdManager, co, studentId, form.studentName, files, PDFCreator.header2, body);
        if (changeDate!=null) bdManager.addValue(co, BDManager.tableEvents,
                    new String[]{TableEvents.date, TableEvents.student, TableEvents.event_type, TableEvents.event_id, TableEvents.event_sub, TableEvents.notes, TableEvents.teacher},
                    new String[]{new java.sql.Date(form.changeDate.getTime()).toString(),
                            String.valueOf(studentId), "15","NULL","NULL","NULL", String.valueOf(teacher)});
        if (created) BDManager.closeQuietly(co);
    }

    public static void createEndOfTermReports(BDManager bdManager, Connection co, CacheManager cacheManager,
                                              SettingsManager settingsManager, Integer studentId,
                                              Integer classroom, Date reportDate, Date changeDate,
                                              Boolean recordDate, Boolean sendEmail, BufferedImage logo){
        boolean isCreated = false;
        File f1 = null;
        Integer teacher = null;

        try {
            if (co == null) {co = bdManager.connect(); isCreated = true;}
            if (classroom != 1) {
                Pdf_Yet_Reports pdf1 = new Pdf_Yet_Reports(bdManager, co, cacheManager, settingsManager, studentId, classroom,
                        reportDate, logo);
                teacher = pdf1.teacher;
                f1 = new File(pdf1.createDocument());
            }

            Pdf_FollowUpReports pdf2 = new Pdf_FollowUpReports(bdManager, cacheManager, settingsManager, co, studentId, classroom,
                    reportDate, changeDate, logo, true, null);
            File f2 = new File(pdf2.createDocument());
            if (f1!=null || f2!=null) {
                MyLogger.d(TAG, "New files: " + ((f1!=null) ? f1.getAbsolutePath() : "") +  ":" + f2.getAbsolutePath());
                if (recordDate || sendEmail)
                    manageFiles(cacheManager, bdManager, co, settingsManager, studentId, pdf2, new File[] {f1,f2}, body3, recordDate ? changeDate : null, sendEmail, teacher);
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            if (isCreated) BDManager.closeQuietly(co);
        }
    }

    public static void createEndOfTermReportsForClassroom(BDManager bdManager, CacheManager cacheManager,
                                                      SettingsManager settingsManager, Integer classroom,
                                                      Date reportDate, Date changeDate, Boolean recordDate,
                                                      Boolean sendEmail, BufferedImage logo, Boolean doYetReport) {
        Connection co = null;
        File f1 = null;
        Integer teacher = null;
        try {
            co = bdManager.connect();
            for (Integer studentId : cacheManager.studentsperclassroom.get(classroom)) {
                if (classroom != 1 && doYetReport) {
                    Pdf_Yet_Reports pdf1 = new Pdf_Yet_Reports(bdManager, co, cacheManager, settingsManager, studentId, classroom,
                            reportDate, logo);
                    teacher = pdf1.teacher;
                    f1 = new File(pdf1.createDocument());
                }
                Pdf_FollowUpReports pdf2 = new Pdf_FollowUpReports(bdManager, cacheManager, settingsManager, co, studentId, classroom,
                        reportDate, changeDate, logo, false, null);
                File f2 = new File(pdf2.createDocument());
                if (f1!=null || f2!=null) {
                    MyLogger.d(TAG, "New files: " + ((f1!=null) ? f1.getAbsolutePath() : "") +  ":" + f2.getAbsolutePath());
                    if (recordDate || sendEmail)
                        manageFiles(cacheManager, bdManager, co, settingsManager, studentId, pdf2, new File[] {f1,f2}, body2, recordDate? changeDate: null, sendEmail, teacher);
                } else MyLogger.d("Error generating form", "Student: " + studentId);
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    public static void createEndOfEYPupilReport(BDManager bdManager, CacheManager cacheManager,
                                                SettingsManager settingsManager, Integer studentId,
                                                String studentName, Date reportDate, Date changeDate,
                                                Boolean recordDate, Boolean sendEmail, BufferedImage logo){
        Pdf_EndOfPeriodPupilReports pdf = new Pdf_EndOfPeriodPupilReports(bdManager, cacheManager, settingsManager, studentId,
                studentName, 1, reportDate, changeDate, logo, "END OF EY PRIME AREAS REPORT", 2.5);
        File f = new File(pdf.createDocument());
        if (f!=null) {
            MyLogger.d(TAG, "New file: " + f.getAbsolutePath());
            if (recordDate || sendEmail)
                manageFile(cacheManager, bdManager, null, settingsManager, studentId, pdf, f, header2, body2, recordDate? changeDate: null, sendEmail, null);
        }
    }

    public static void createEndOfFSPupilReport(BDManager bdManager, CacheManager cacheManager,
                                                SettingsManager settingsManager, Integer studentId,
                                                String studentName, Integer classroom, Date reportDate, Date changeDate,
                                                Boolean recordDate, Boolean sendEmail, BufferedImage logo){
        Pdf_EndOfPeriodPupilReports pdf = new Pdf_EndOfPeriodPupilReports(bdManager, cacheManager, settingsManager, studentId,
                studentName, classroom, reportDate, changeDate, logo, "END OF FS PRIME AREAS REPORT", 5d);
        File f = new File(pdf.createDocument());
        if (f!=null && (recordDate || sendEmail))
            manageFile(cacheManager, bdManager, null, settingsManager, studentId, pdf, f, header2, body2, recordDate? changeDate: null, sendEmail, null);
    }

    public static void createEndOfY1PupilReport(BDManager bdManager, CacheManager cacheManager,
                                                SettingsManager settingsManager, Integer studentId,
                                                String studentName, Integer classroom, Date reportDate, Date changeDate,
                                                Boolean recordDate, Boolean sendEmail, BufferedImage logo){
        Pdf_EndOfPeriodPupilReports pdf = new Pdf_EndOfPeriodPupilReports(bdManager, cacheManager, settingsManager, studentId,
                studentName, classroom, reportDate, changeDate, logo, "END OF YEAR 1 PRIME AREAS REPORT", 6d);
        File f = new File(pdf.createDocument());
        if (f!=null && (recordDate || sendEmail))
            manageFile(cacheManager, bdManager, null, settingsManager, studentId, pdf, f, header2, body2, recordDate? changeDate: null, sendEmail, null);
    }

    public static void createEYPupilReport(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager,
                                           Integer studentId, Date date, Date changeDate, Integer classroom, Boolean recordDate,
                                           Boolean sendEmail, BufferedImage logo){
        Pdf_EYPupilReports pdf = new Pdf_EYPupilReports(bdManager, cacheManager, settingsManager, null,
                studentId, classroom, date, logo);
        File f = new File(pdf.createDocument());
        if (f!=null && (recordDate || sendEmail))
            manageFile(cacheManager, bdManager, null, settingsManager, studentId, pdf, f, header1, body1, recordDate? changeDate: null, sendEmail, null);
    }

    public static void createYetReport(BDManager bdManager, Connection co, CacheManager cacheManager,
                                                SettingsManager settingsManager, Integer studentId,
                                                Integer classroom, Date reportDate, Date changeDate,
                                                Boolean recordDate, Boolean sendEmail,
                                       BufferedImage logo){
        Pdf_Yet_Reports pdf = new Pdf_Yet_Reports(bdManager, co, cacheManager, settingsManager, studentId, classroom,
                reportDate, logo);
        File f = new File(pdf.createDocument());
        if (f!=null && (recordDate || sendEmail))
            manageFile(cacheManager, bdManager, co, settingsManager, studentId, pdf, f, header2, body2, recordDate? changeDate: null, sendEmail, pdf.teacher);
    }

    public static void createCDBPupilReport(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager,
                                            Integer studentId, Date date, Date changeDate, Integer classroom, Boolean recordDate,
                                            Boolean sendEmail, BufferedImage logo){
        Pdf_CDBPupilReports pdf = new Pdf_CDBPupilReports(bdManager, cacheManager, settingsManager, null, studentId, classroom, date, logo);
        File f = new File(pdf.createDocument());
        if (f!=null && (recordDate || sendEmail))
            manageFile(cacheManager, bdManager, null, settingsManager, studentId, pdf, f, header1, body1, recordDate? changeDate: null, sendEmail, null);
    }

    public static void createTallerPupilReport(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager,
                                            Integer studentId, Date date, Date changeDate, Integer classroom, Boolean recordDate,
                                            Boolean sendEmail, BufferedImage logo){
        Pdf_TallerPupilReports pdf = new Pdf_TallerPupilReports(bdManager, cacheManager, settingsManager, null, studentId, classroom, date, logo);
        File f = new File(pdf.createDocument());
        if (f!=null) {
            if (f.exists()) MyLogger.d(TAG, "File:" + f.getAbsolutePath());
            else MyLogger.d(TAG, "ERROR creando fichero:" + f.getAbsolutePath());
        }
        else MyLogger.d(TAG, "File: null");

        if (f!=null && (recordDate || sendEmail))
            manageFile(cacheManager, bdManager, null, settingsManager, studentId, pdf, f, header1, body1, recordDate? changeDate: null, sendEmail, null);
    }

    public static void createFollowUpReport(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager,
                                               Integer studentId, Date date, Date changeDate, Integer classroom, Boolean recordDate,
                                               Boolean sendEmail, BufferedImage logo){
        Pdf_FollowUpReports pdf = new Pdf_FollowUpReports(bdManager, cacheManager, settingsManager, null, studentId,
                classroom, date, null, logo, true, null);
        File f = new File(pdf.createDocument());
        if (f!=null) {
            if (f.exists()) MyLogger.d(TAG, "File:" + f.getAbsolutePath());
            else MyLogger.d(TAG, "ERROR creando fichero:" + f.getAbsolutePath());
        }
        else MyLogger.d(TAG, "File: null");

        if (f!=null && (recordDate || sendEmail))
            manageFile(cacheManager, bdManager, null, settingsManager, studentId, pdf, f, header1, body1, recordDate? changeDate: null, sendEmail, null);
    }


    public static void createReportsForClassroom(BDManager bdManager, CacheManager cacheManager,
                                                 SettingsManager settingsManager, Integer classroomId, Date date,
                                                 Boolean recordDate, Boolean sendEmail, BufferedImage logo) {
        PDFForm_Reports form;
        Connection co = null;
        try {
            co = bdManager.connect();
            for (Integer studentId : cacheManager.studentsperclassroom.get(classroomId)) {
                form =  new Pdf_FollowUpReports(bdManager, cacheManager, settingsManager,
                        co, studentId, classroomId, date, null, logo, true, null);
                if (form!=null) {
                    File file = new File(form.createDocument());
                    if (file.exists()) {
                        if (sendEmail) sendEmail(cacheManager, bdManager, co, studentId, form.studentName, new File[]{file}, header1, body1);
                        if (recordDate) bdManager.addValue(co, BDManager.tableEvents,
                                new String[]{TableEvents.date, TableEvents.student, TableEvents.event_type, TableEvents.teacher},
                                new String[]{new java.sql.Date(form.reportDate.getTime()).toString(),
                                        String.valueOf(studentId), String.valueOf(15), String.valueOf(settingsManager.teacher)});
                    }
                } else MyLogger.d("Error generating form", "Student: " + studentId);
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co);
        }
    }

    public static void sendEmail(CacheManager cacheManager, BDManager bdManager, Connection co, Integer studentId, String studentName,
                                 File[] files, String header, String body) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", "mail.elanmontessori.org");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(PDFCreator.user, PDFCreator.p);
                    }
                });

        String[] fields={TableContacts.student1, TableContacts.student2, TableContacts.student3, TableContacts.student4};
        if (co == null) co = bdManager.connect();
        for (String field : fields) {
            if (studentId==1) continue;
            MySet set = bdManager.getValues(co, BDManager.tableContacts,field + "=" + studentId);
            while (set.next()) {
                String to = set.getString(TableContacts.email);
                if (to == null) continue;
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(from));
                    message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
                    message.setSubject(header + studentName);
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(body);
                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(messageBodyPart);
                    for (File f: files) {
                        if ( f != null) addAttachment(multipart, f.getAbsolutePath(), f.getName());
                    }
                    message.setContent(multipart );
                    Transport.send(message);
                    System.out.println("Sent message successfully.... "+to);
                }catch (MessagingException mex) {
                    mex.printStackTrace();
                }
            }
        }
    }

    private static void addAttachment(Multipart multipart, String filename, String name) throws MessagingException {
        DataSource source = new FileDataSource(filename);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(name);
        multipart.addBodyPart(messageBodyPart);
    }


}
