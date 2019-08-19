package org.pdxfinder.services;

import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.pdxfinder.services.email.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/*
 * Created by abayomi on 16/08/2019.
 */
@Service
public class EmailService {

    private Logger log = LoggerFactory.getLogger(EmailService.class);

    //@Qualifier("pdxfinderMailBean")
    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendMail(String recipientMail,
                         String recipientName,
                         String subject,
                         String missingCount){

        log.info("Sending MultiPart Email to {}", recipientMail);

        Mail mail = new Mail();
        mail.setSenderMail("nonreply@pdxfinder.org");
        mail.setSenderName("PDX FINDER ADMIN");

        mail.setRecipientMail(recipientMail);
        mail.setRecipientName(recipientName);

        mail.setSubject(subject);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("date", "xxxx");
        model.put("count", missingCount);
        mail.setModel(model);

        pdxFinderMailEngine(mail);
    }



    public void pdxFinderMailEngine(Mail mail)  {

        try {

            InternetAddress senderAddress = new InternetAddress(mail.getSenderMail(), mail.getSenderName());
            InternetAddress receiverAddress = new InternetAddress(mail.getRecipientMail(), mail.getRecipientName());

            MimeMessage message = emailSender.createMimeMessage();
            message.setSender(senderAddress);


            MimeMessageHelper helper = new MimeMessageHelper(message,
                                                             MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                                                             StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariables(mail.getModel());
            String html = templateEngine.process("email-template", context);

            helper.setFrom(senderAddress);
            helper.setTo(receiverAddress);
            helper.setSubject(mail.getSubject());
            helper.setText(html, true);

            emailSender.send(message);

        }catch (Exception e){  }

    }

}
