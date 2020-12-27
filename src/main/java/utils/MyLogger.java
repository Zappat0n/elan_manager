package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

/**
 * Created by angel on 10/02/16.
 */
public class MyLogger {
    private static final String username = "test.nettie@gmail.com";
    private static final String password = "CQE3Tdqwedf";
    private static final Boolean DEBUG = true;
    private static final ArrayList<String> log = new ArrayList<>();

    public static void d(String TAG, String message) {
        log.add(TAG + ":" + message);
        System.out.println(TAG + ":" + message);
    }

    public static void e(String TAG, Exception e) {
        log.add("ERROR: " + TAG + " : " + e.getMessage());
        System.out.println(TAG + ":" + e.getMessage());
        e.printStackTrace();
        if (DEBUG) {
            StringBuilder trace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace())
                trace.append((trace.toString().equals("")) ? element : "\n" + element);
            emailError(TAG, trace.toString());
        }
    }

    private static void emailError(String TAG, String message) {
        email("\n.TAG: " + TAG + "\n." + message, true);
    }

    public static void emailLog() {
        if (log.size() == 0) return;
        StringBuilder text = new StringBuilder();
        for (String line : log) {
            text.append(line).append("\n");
        }
        email(text.toString(), false);
    }

    private static void email(String message, Boolean isError) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        Authenticator authenticator = new SMTPAuthenticator();
        Session session = Session.getDefaultInstance(props, authenticator);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(username));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("nettieapp@gmail.com"));
            msg.setSubject(isError ? "Error" : "Log");
            msg.setSentDate(new Date());
            msg.setText(message);
            Transport.send(msg);
        } catch (MessagingException e) {
            System.out.println("Error al enviar log, exception: " + e);
        }
    }



    private static class SMTPAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = MyLogger.username;
            String password = MyLogger.password;
            return new PasswordAuthentication(username, password);
        }
    }
}
