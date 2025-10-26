package pt.estga.stonemark.controllers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.stonemark.services.EmailService;

@RestController
@RequestMapping("/api/v1/email")
@Validated
public class TestEmailController {

    private static final Logger log = LoggerFactory.getLogger(TestEmailController.class);

    private final EmailService emailService;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/send")
    public ResponseEntity<String> sendEmail(
            @RequestParam("to") @NotBlank @Email String to,
            @RequestParam(value = "subject", defaultValue = "Test email from Stonemark") String subject,
            @RequestParam(value = "body", defaultValue = "This is a test email.") String body) {
        try {
            emailService.sendEmail(to, subject, body);
            return ResponseEntity.ok("Email sent to " + to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email");
        }
    }
}
