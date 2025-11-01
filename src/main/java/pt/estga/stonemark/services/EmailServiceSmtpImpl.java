package pt.estga.stonemark.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.exceptions.EmailSendingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceSmtpImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent successfully to {} with subject '{}'", to, subject);
        } catch (MailException e) {
            log.error("Failed to send email to {} with subject '{}': {}", to, subject, e.getMessage());
            throw new EmailSendingException("Failed to send email to " + to, e);
        }
    }
}
