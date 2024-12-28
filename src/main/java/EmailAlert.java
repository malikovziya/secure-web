import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.*;

public class EmailAlert {
    public static void sendEmail(String subject, String messageBody) {
        // Sender's email ID and password
        String fromEmail = "ziyamelikov04@gmail.com";
        String password = "fuhz xbyk jxzq dslf"; // use App Password instead of your Gmail password

        // Recipient's email ID
        String toEmail = "ziyamelikov04@gmail.com"; // replace with your email

        // SMTP server settings for Gmail
        String host = "smtp.gmail.com";
        String port = "587";

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        System.setProperty("mail.smtp.ssl.trust", "smtp.gmail.com");
        // Get the session object with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set From: header field
            message.setFrom(new InternetAddress(fromEmail));

            // Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // Set Subject: header field
            message.setSubject(subject);

            // Set the actual message
            message.setText(messageBody);

            // Send the email
            Transport.send(message);
            System.out.println("Sent email alert successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
