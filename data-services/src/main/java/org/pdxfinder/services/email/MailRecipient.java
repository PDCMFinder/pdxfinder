package org.pdxfinder.services.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author michael
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailRecipient {
    private String recipientMail;
    private String recipientName;
    private String subject;
    private String missingCount;
}
