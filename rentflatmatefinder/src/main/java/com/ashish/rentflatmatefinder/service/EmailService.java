package com.ashish.rentflatmatefinder.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendInterestNotificationToOwner(String ownerEmail, String ownerName,
                                                 String tenantName, String listingTitle,
                                                 int compatibilityScore, String explanation) {
        String subject = "🏠 New Interest Request – " + listingTitle;
        String body = buildOwnerInterestEmail(ownerName, tenantName, listingTitle, compatibilityScore, explanation);
        sendHtmlEmail(ownerEmail, subject, body);
    }

    @Async
    public void sendInterestAcceptedToTenant(String tenantEmail, String tenantName,
                                              String listingTitle, String ownerName) {
        String subject = "✅ Your Interest Was Accepted – " + listingTitle;
        String body = buildAcceptedEmail(tenantName, listingTitle, ownerName);
        sendHtmlEmail(tenantEmail, subject, body);
    }

    @Async
    public void sendInterestDeclinedToTenant(String tenantEmail, String tenantName,
                                              String listingTitle) {
        String subject = "❌ Interest Update – " + listingTitle;
        String body = buildDeclinedEmail(tenantName, listingTitle);
        sendHtmlEmail(tenantEmail, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOwnerInterestEmail(String ownerName, String tenantName,
                                            String listingTitle, int score, String explanation) {
        return """
            <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px;">
              <div style="background: linear-gradient(135deg, #667eea, #764ba2); padding: 30px; border-radius: 12px; color: white; text-align: center;">
                <h1 style="margin: 0;">🏠 New Interest Request</h1>
              </div>
              <div style="padding: 24px; background: #f9f9f9; border-radius: 0 0 12px 12px;">
                <p>Hi <strong>%s</strong>,</p>
                <p><strong>%s</strong> is interested in your listing: <strong>%s</strong></p>
                <div style="background: white; border-left: 4px solid #667eea; padding: 16px; border-radius: 8px; margin: 16px 0;">
                  <p style="margin: 0; font-size: 24px; font-weight: bold; color: #667eea;">Compatibility Score: %d/100</p>
                  <p style="margin: 8px 0 0 0; color: #666;">%s</p>
                </div>
                <p>Log in to your dashboard to view and respond to this interest request.</p>
                <p style="color: #999; font-size: 12px;">Rent & Flatmate Finder</p>
              </div>
            </html>
            """.formatted(ownerName, tenantName, listingTitle, score, explanation);
    }

    private String buildAcceptedEmail(String tenantName, String listingTitle, String ownerName) {
        return """
            <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px;">
              <div style="background: linear-gradient(135deg, #11998e, #38ef7d); padding: 30px; border-radius: 12px; color: white; text-align: center;">
                <h1 style="margin: 0;">✅ Interest Accepted!</h1>
              </div>
              <div style="padding: 24px; background: #f9f9f9; border-radius: 0 0 12px 12px;">
                <p>Hi <strong>%s</strong>,</p>
                <p>Great news! <strong>%s</strong> (owner of <em>%s</em>) has accepted your interest request.</p>
                <p>You can now chat with the owner in real time. Log in to start the conversation!</p>
                <p style="color: #999; font-size: 12px;">Rent & Flatmate Finder</p>
              </div>
            </html>
            """.formatted(tenantName, ownerName, listingTitle);
    }

    private String buildDeclinedEmail(String tenantName, String listingTitle) {
        return """
            <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px;">
              <div style="background: linear-gradient(135deg, #ee0979, #ff6a00); padding: 30px; border-radius: 12px; color: white; text-align: center;">
                <h1 style="margin: 0;">Interest Update</h1>
              </div>
              <div style="padding: 24px; background: #f9f9f9; border-radius: 0 0 12px 12px;">
                <p>Hi <strong>%s</strong>,</p>
                <p>The owner has declined your interest in <strong>%s</strong>.</p>
                <p>Don't worry — there are more listings available. Keep browsing to find your perfect match!</p>
                <p style="color: #999; font-size: 12px;">Rent & Flatmate Finder</p>
              </div>
            </html>
            """.formatted(tenantName, listingTitle);
    }
}
