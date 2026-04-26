package com.recruitment.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String appBaseUrl;

    // Brand colours (kept here so emails are self-contained)
    private static final String COLOR_DARK  = "#293940";
    private static final String COLOR_PEACH = "#F2B29B";
    private static final String COLOR_LIGHT = "#F2F2F2";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Forgot password ───────────────────────────────────────────────────────

    public void sendPasswordReset(String toEmail, String resetToken) {
        String url = appBaseUrl + "/auth/set-password?token=" + resetToken;
        send(toEmail,
             "AMIC — Password Reset Request",
             buildEmail(
                 "Reset your password",
                 "You requested a password reset for your AMIC account.",
                 "This link expires in <strong>15 minutes</strong>. If you did not request a reset, you can safely ignore this email.",
                 url,
                 "Reset Password",
                 false
             ));
    }

    // ── Welcome / first-login setup ───────────────────────────────────────────

    public void sendWelcomeEmail(String toEmail, String firstName, String setupToken) {
        String url = appBaseUrl + "/auth/set-password?token=" + setupToken + "&welcome=true";
        send(toEmail,
             "Welcome to AMIC — Set Up Your Account",
             buildEmail(
                 "Welcome, " + firstName + "!",
                 "Your AMIC account has been created by HR. Before you can sign in, you need to set your own password.",
                 "This link is valid for <strong>7 days</strong>. If you have any questions, contact your HR administrator.",
                 url,
                 "Set My Password",
                 true
             ));
    }

    // ── Shared builder ────────────────────────────────────────────────────────

    private void send(String to, String subject, String html) {
        try {
            log.info("Sending email '{}' to {}", subject, to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "AMIC Portal");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    private String buildEmail(String heading,
                              String intro,
                              String footer,
                              String ctaUrl,
                              String ctaLabel,
                              boolean isWelcome) {
        String accentLine = isWelcome
            ? "<div style='width:48px;height:4px;background:" + COLOR_PEACH + ";border-radius:2px;margin:0 auto 24px;'></div>"
            : "";

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
            <body style="margin:0;padding:0;background:%s;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:%s;padding:40px 0;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(41,57,64,.10);">

                    <!-- Header -->
                    <tr>
                      <td align="center" style="background:%s;padding:32px 40px 28px;">
                        <h1 style="margin:0;color:#ffffff;font-size:22px;font-weight:700;letter-spacing:.5px;">AMIC Portal</h1>
                      </td>
                    </tr>

                    <!-- Body -->
                    <tr>
                      <td style="padding:36px 40px 0;">
                        %s
                        <h2 style="margin:0 0 16px;color:%s;font-size:20px;font-weight:700;text-align:center;">%s</h2>
                        <p style="margin:0 0 24px;color:#555;font-size:15px;line-height:1.6;text-align:center;">%s</p>

                        <!-- CTA button -->
                        <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr><td align="center" style="padding-bottom:28px;">
                            <a href="%s"
                               style="display:inline-block;background:%s;color:#ffffff;text-decoration:none;
                                      padding:14px 36px;border-radius:8px;font-size:15px;font-weight:600;
                                      letter-spacing:.3px;">
                              %s
                            </a>
                          </td></tr>
                        </table>

                        <p style="margin:0 0 8px;color:#999;font-size:12px;text-align:center;">
                          If the button doesn't work, copy this link into your browser:
                        </p>
                        <p style="margin:0 0 32px;font-size:11px;text-align:center;word-break:break-all;">
                          <a href="%s" style="color:%s;">%s</a>
                        </p>
                      </td>
                    </tr>

                    <!-- Footer note -->
                    <tr>
                      <td style="background:%s;padding:20px 40px;border-top:1px solid #eee;">
                        <p style="margin:0;color:#888;font-size:12px;text-align:center;line-height:1.5;">%s</p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                COLOR_LIGHT, COLOR_LIGHT,      // body bg
                COLOR_DARK,                    // header bg
                accentLine,                    // optional accent bar
                COLOR_DARK, heading,           // h2
                intro,                         // intro paragraph
                ctaUrl, COLOR_DARK, ctaLabel,  // CTA button
                ctaUrl, COLOR_PEACH, ctaUrl,   // fallback link
                COLOR_LIGHT,                   // footer bg
                footer                         // footer text
            );
    }
}
