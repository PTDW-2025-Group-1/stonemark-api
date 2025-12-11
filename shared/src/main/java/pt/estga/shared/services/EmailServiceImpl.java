package pt.estga.shared.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.estga.shared.models.Email;

@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Override
    @Async
    public void sendEmail(Email email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(templateService.generateBody(email), true);

            log.info("Sending email to {}", email.getTo());
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Failed to send email to {} with template {}", email.getTo(), email.getTemplate(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
