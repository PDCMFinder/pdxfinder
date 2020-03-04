package org.pdxfinder.postload;

import org.pdxfinder.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;


@Service
@Order(value = 96)
public class SendNotifications {

    private final static Logger log = LoggerFactory.getLogger(SendNotifications.class);
    private EmailService emailService;

    public SendNotifications(EmailService emailService) {
        this.emailService = emailService;
    }

    public void run() {

        log.info("Creating and sending email notifications");

        getUnmappedSamples();

        sendEmail("", "", "URGENT: UNMAPPED TERMS FOUND", "86");

    }



    private void getUnmappedSamples(){

    }


    private void sendEmail(String recipientMail,
                           String recipientName,
                           String subject,
                           String missingCount){

        emailService.sendMail(recipientMail, recipientName, subject, missingCount);

    }


}
