package com.smartfleet.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email Service for sending notifications
 * CRITICAL: Uses @Async for non-blocking email operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.fleet.manager.email}")
    private String managerEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * MODIFICAT: Trimite notificare de login direct ȘOFERULUI (Welcome Back)
     */
    @Async
    public void sendDriverLoginNotification(String driverUsername, String driverEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            // SCHIMBARE: Trimitem la emailul șoferului, nu al managerului
            helper.setTo(driverEmail);
            helper.setSubject("👋 Bine ai venit înapoi, " + driverUsername + "!");

            String htmlContent = buildLoginNotificationHtml(driverUsername);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Login welcome email sent to driver: {}", driverEmail);
        } catch (MessagingException e) {
            log.error("Failed to send login email to: {}", driverEmail, e);
        }
    }

    /**
     * Send welcome email to newly registered driver (Async)
     */
    @Async
    public void sendWelcomeEmail(String driverEmail, String driverName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(driverEmail);
            helper.setSubject("🎉 Welcome to Smart Fleet Tracking!");

            String htmlContent = buildWelcomeEmailHtml(driverName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to: {}", driverEmail);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", driverEmail, e);
        }
    }

    /**
     * MODIFICAT: HTML prietenos pentru șofer la logare
     */
    private String buildLoginNotificationHtml(String username) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; }
                        .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #ffffff; }
                        .info { background-color: #f8fafc; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #2563eb; }
                        .footer { background-color: #f1f5f9; padding: 15px; text-align: center; font-size: 12px; color: #64748b; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2 style="margin:0;">🚗 Smart Fleet</h2>
                        </div>
                        <div class="content">
                            <h3>Salut %s,</h3>
                            <p>Te-ai logat cu succes în aplicația Smart Fleet Tracking.</p>

                            <div class="info">
                                <p><strong>🕒 Ora logării:</strong> %s</p>
                                <p><strong>📍 Status:</strong> Sistemul este activ și pregătit.</p>
                            </div>

                            <p>Drumuri bune și conducere prudentă!</p>
                            <br>
                            <a href="http://localhost:3000" style="background-color:#2563eb; color:white; padding:10px 20px; text-decoration:none; border-radius:5px;">Mergi la Dashboard</a>
                        </div>
                        <div class="footer">
                            <p>Acesta este un mesaj automat. Te rugăm să nu răspunzi.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(username, now.format(formatter));
    }

    /**
     * Build HTML content for welcome email (Registration)
     */
    private String buildWelcomeEmailHtml(String name) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                                 color: white; padding: 40px; text-align: center; border-radius: 10px 10px 0 0; }
                        .header h1 { margin: 0; font-size: 32px; }
                        .content { background: white; padding: 30px; border-radius: 0 0 10px 10px;
                                  box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                        .feature { background: #f8f9fa; padding: 15px; margin: 15px 0;
                                  border-radius: 8px; border-left: 4px solid #10b981; }
                        .feature strong { color: #10b981; }
                        .footer { text-align: center; margin-top: 20px; padding-top: 20px;
                                 border-top: 1px solid #eee; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🚗 Welcome to Smart Fleet!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>Welcome to the Smart Fleet Tracking System! We're excited to have you on board.</p>

                            <div class="feature">
                                <strong>📊 View Your Dashboard</strong><br>
                                Track vehicle status, kilometers, and driving statistics.
                            </div>

                            <div class="feature">
                                <strong>🗺️ Live Map Access</strong><br>
                                Real-time location tracking powered by Geoapify maps.
                            </div>

                            <div class="feature">
                                <strong>🔧 Vehicle Health Monitor</strong><br>
                                Monitor tire pressure, oil level, fuel, and battery status.
                            </div>

                            <div class="feature">
                                <strong>🤖 AI Route Planner</strong><br>
                                Get optimized routes and fuel consumption predictions.
                            </div>

                            <p style="margin-top: 30px; padding: 20px; background: #e8f5e9; border-radius: 8px;">
                                <strong>Get Started:</strong> Log in to your dashboard to view your assigned vehicle and start tracking!
                            </p>

                            <div class="footer">
                                <p>Smart Fleet Tracking System © 2024</p>
                                <p>For support, contact: support@smartfleet.com</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(name);
    }

}