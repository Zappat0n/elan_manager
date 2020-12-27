package utils;

import bd.BDManager;
import bd.MySet;
import bd.model.TableContacts;
import main.ApplicationLoader;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.sql.Connection;
import java.util.Properties;

public class EmailManager {
    public static void sendEmail(Connection co, Integer studentId, File[] files, String header, String body) {
        Properties properties = System.getProperties();
        String socket = ApplicationLoader.settingsManager.getValue(SettingsManager.EMAIL_SOCKET);
        String user = ApplicationLoader.settingsManager.getValue(SettingsManager.EMAIL);
        String server = ApplicationLoader.settingsManager.getValue(SettingsManager.EMAIL_SERVER);
        String pwd = ApplicationLoader.settingsManager.getValue(SettingsManager.EMAIL_PWD);
        properties.put("mail.smtp.host", ApplicationLoader.settingsManager.getValue(SettingsManager.EMAIL_SERVER));
        properties.put("mail.smtp.socketFactory.port", socket);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", socket);

        Session session = Session.getInstance(properties, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pwd); }
                });

        String[] fields={TableContacts.student1, TableContacts.student2, TableContacts.student3, TableContacts.student4};
        if (co == null) co = ApplicationLoader.bdManager.connect();
        for (String field : fields) {
            MySet set = ApplicationLoader.bdManager.getValues(co, BDManager.tableContacts,field + "=" + studentId);
            while (set.next()) {
                String to = set.getString(TableContacts.email);
                if (to == null) continue;
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(user));
                    message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
                    message.setSubject(header);
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(body);
                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(messageBodyPart);
                    for (File f: files) {
                        if ( f != null) addAttachment(multipart, f.getAbsolutePath(), f.getName());
                    }
                    message.setContent(multipart );

                    Transport.send(message);
                    Store store = session.getStore("imap");
                    store.connect(server, user, pwd);
                    Folder folder = store.getFolder("INBOX");
                    folder = folder.getFolder("Sent");
                    folder.open(Folder.READ_WRITE);
                    message.setFlag(Flags.Flag.SEEN, true);
                    folder.appendMessages(new Message[] {message});
                    store.close();
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
