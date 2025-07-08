package com.example.auth.service.email;

public interface EmailSender {
    void sendEmail(String to, String subject, String htmlBody);
}
