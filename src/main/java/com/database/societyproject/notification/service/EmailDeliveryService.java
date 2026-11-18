package com.database.societyproject.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@Service
public class EmailDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(EmailDeliveryService.class);
    private final JavaMailSender sender;
    private final boolean enabled;
    private final String from;

    public EmailDeliveryService(JavaMailSender sender, @Value("${app.mail.enabled:false}") boolean enabled,
                                @Value("${app.mail.from:}") String from) {
        this.sender = sender; this.enabled = enabled; this.from = from;
    }

    @PostConstruct
    void reportConfiguration() {
        if (!enabled) {
            log.info("Email delivery is disabled (set APP_MAIL_ENABLED=true to enable it).");
        } else if (from == null || from.isBlank()) {
            log.warn("Email delivery is enabled but APP_MAIL_FROM is empty.");
        } else {
            log.info("Email delivery is enabled for sender address {}.", from);
        }
    }

    /** Email delivery is optional: a mail outage must never break the in-app workflow. */
    public void sendIfEnabled(String recipient, String subject, String body) {
        if (!enabled || recipient == null || recipient.isBlank()) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient); message.setSubject(subject); message.setText(body);
            if (from != null && !from.isBlank()) message.setFrom(from);
            sender.send(message);
            log.info("Email delivery succeeded.");
        } catch (RuntimeException exception) {
            // The notification/invitation remains in the application even if SMTP is unavailable.
            log.warn("Email delivery failed: {}", exception.getMessage());
        }
    }
}
