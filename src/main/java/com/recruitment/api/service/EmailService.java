package com.recruitment.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordReset(String toEmail, String resetToken) {
        try {
            String resetUrl = appBaseUrl + "/auth/reset-password?token=" + resetToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("AMIC — Password Reset Request");
            helper.setText(buildResetEmailHtml(resetUrl), true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildResetEmailHtml(String resetUrl) {
        return """
                <html><body>
                <p>You requested a password reset for your AMIC account.</p>
                <p><a href="%s">Click here to reset your password</a></p>
                <p>This link expires in 15 minutes. If you did not request this, ignore this email.</p>
                </body></html>
                """.formatted(resetUrl);
    }
}
