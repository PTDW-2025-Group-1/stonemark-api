package pt.estga.shared.services;

public interface SmsService {

    void sendMessage(String phoneNumber, String code);

}

