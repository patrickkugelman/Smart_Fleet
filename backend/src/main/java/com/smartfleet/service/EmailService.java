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
     * Send driver login notification to fleet manager (Async)
     */
    @Async
    public void sendDriverLoginNotification(String driverUsername, String driverEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(managerEmail);
            helper.setSubject("🚗 Driver Login Alert - " + driverUsername);

            String htmlContent = buildLoginNotificationHtml(driverUsername, driverEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Login notification sent for driver: {}", driverUsername);
        } catch (MessagingException e) {
            log.error("Failed to send login notification for driver: {}", driverUsername, e);
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
     * Build HTML content for login notification email
     */
    private String buildLoginNotificationHtml(String username, String email) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { background: white; padding: 30px; border-radius: 0 0 10px 10px; 
                              box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .info-box { background: #f8f9fa; border-left: 4px solid #667eea; 
                               padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .info-box p { margin: 8px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚗 Smart Fleet Tracking</h1>
                        <p>Driver Login Notification</p>
                    </div>
                    <div class="content">
                        <h2>Driver Login Alert</h2>
                        <p>A driver has just logged into the system.</p>
                        <div class="info-box">
                            <p><strong>Driver:</strong> %s</p>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Time:</strong> %s</p>
                            <p><strong>Location:</strong> Cluj-Napoca, Romania</p>
                        </div>
                        <p style="color: #666; font-size: 12px;">This is an automated notification from Smart Fleet Tracking System.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, email, now.format(formatter));
    }

    /**
     * Build HTML content for welcome email
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
            """.formatted(name);
    }

}
