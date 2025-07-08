package com.example.auth.service.sms;

public interface SmsSender {
    void sendSms(String to, String text);
}
