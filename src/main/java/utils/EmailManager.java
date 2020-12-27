package utils;

import bd.BDManager;
import bd.MySet;
import bd.model.TableContacts;

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
    private static final String from = "reports@elanmontessori.org";
    private static final String host = "mail.elanmontessori.org";
    private static final String user = "reports@elanmontessori.org";
    private static final String p    = "tXoLM80fKqK6";

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

    public static void sendEmail(CacheManager cacheManager, BDManager bdManager, Connection co, Integer studentId,
                                 File[] files, String header, String body) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        Session session = Session.getInstance(properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, p);
                    }
                });

        String[] fields={TableContacts.student1, TableContacts.student2, TableContacts.student3, TableContacts.student4};
        if (co == null) co = bdManager.connect();
        for (String field : fields) {
            MySet set = bdManager.getValues(co, BDManager.tableContacts,field + "=" + studentId);
            while (set.next()) {
                String to = set.getString(TableContacts.email);
                if (to == null) continue;
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(from));
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
                    store.connect(host, user, p);
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
