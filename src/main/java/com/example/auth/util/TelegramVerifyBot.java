package com.example.auth.util;

import com.example.auth.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class TelegramVerifyBot extends TelegramLongPollingBot {
    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    private final VerificationService verificationService;
    private final Map<String, Long> pending = new ConcurrentHashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText() && message.getText().startsWith("/start ")) {
                String uuid = message.getText().split(" ")[1];

                pending.put(uuid, message.getChatId());
                requestContactButton(message.getChatId());
            } else if (message.hasContact()) {
                String phoneNumber = message.getContact().getPhoneNumber();
                String uuid = pending.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(message.getChatId()))
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null);

                if (uuid == null) return;

                String verificationCode = verificationService.processTelegramContact(uuid, phoneNumber, message.getChatId());
                sendMessage(message.getChatId(), "Your verification code is " + verificationCode);
            }
        }
    }

    private void sendMessage(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            throw new RuntimeException("Telegram send failed", e);
        }
    }

    private void requestContactButton(Long chatId) {
        KeyboardButton requestPhone = KeyboardButton.builder()
                .text("\uD83D\uDCDE Отправить номер")
                .requestContact(true)
                .build();

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboard(List.of(new KeyboardRow(List.of(requestPhone))))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();

        SendMessage askForPhone = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Please, share your phone number that you registered on")
                .replyMarkup(replyKeyboardMarkup)
                .build();

        try {
            execute(askForPhone);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Telegram send failed", e);
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }
}
