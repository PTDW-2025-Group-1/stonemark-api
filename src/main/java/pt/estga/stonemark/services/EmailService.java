package pt.estga.stonemark.services;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

}
