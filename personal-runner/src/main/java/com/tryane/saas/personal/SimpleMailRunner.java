package com.tryane.saas.personal;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class SimpleMailRunner {

    public static void main(String[] args) {
        String host = "tryane-com.mail.protection.outlook.com";
        int port = 25;
        Mailer mailer = MailerBuilder.withSMTPServer(host, port)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();
        final Email email = EmailBuilder.startingBlank()
                .from("nico", "dkim@tryane.com")
                .to("bastien.vaneenaeme@tryane.com")
                .withSubject("hey, dkim test")
                .withPlainText("Look the header bro !")
                //.signWithDomainKey(Paths.get("C:\\Users\\nicolas\\dev\\git\\saas\\saas-connectors\\saas-con-commons\\src\\main\\azure\\certs\\dkim_tryane.dev.key.der.tmp")
                //.toFile(), "tryane.com", "tryane1")
                .buildEmail();
        mailer.sendMail(email);
    }
}
