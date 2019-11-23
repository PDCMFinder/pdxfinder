package org.pdxfinder.services.email;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

/**
 *
 * @author michael
 */
@Component
public class EmailHelpers {

    private static final String SENDER_MAIL = "nonreply@pdxfinder.org";
    private static final String SENDER_NAME = "PDX FINDER ADMIN";

    public Mail BuildMail(MailRecipient mailRecipient, List<Object> attachments) {

        Map<String, Object> model = new HashMap<>();
        model.put("date", "xxxx");
        model.put("count", mailRecipient.getMissingCount());

        return Mail.builder()
            .recipientMail(mailRecipient.getRecipientMail())
            .recipientName(mailRecipient.getRecipientName())
            .senderMail(SENDER_MAIL)
            .senderName(SENDER_NAME)
            .subject(mailRecipient.getSubject())
            .model(model)
            .attachments(attachments)
            .build();
    }

}
