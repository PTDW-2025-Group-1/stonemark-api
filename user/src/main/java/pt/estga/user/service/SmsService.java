package pt.estga.user.service;

public interface SmsService {
    void sendVerificationCode(String phoneNumber, String code);
}

