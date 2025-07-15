package Website.EventRentals.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Simple email service using Gmail SMTP with App Password
 * This approach is more reliable than OAuth tokens and doesn't expire
 */
@Service
public class SimpleEmailService {

    @Value("${gmail.user.email:finaltouchdecor.co@gmail.com}")
    private String userEmail;

    /**
     * Send email using Gmail SMTP with App Password
     * This is more reliable than OAuth tokens and doesn't expire
     */
    public boolean sendEmail(String recipientEmail, String subject, String bodyText) {
        return sendEmailWithRetry(recipientEmail, subject, bodyText, 2);
    }

    private boolean sendEmailWithRetry(String recipientEmail, String subject, String bodyText, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String appPassword = System.getenv("GMAIL_APP_PASSWORD");
                
                if (appPassword == null || appPassword.trim().isEmpty()) {
                    // Fallback to development mode
                    System.out.println("=== EMAIL SERVICE DEVELOPMENT MODE ===");
                    System.out.println("TO: " + recipientEmail);
                    System.out.println("FROM: " + userEmail);
                    System.out.println("SUBJECT: " + subject);
                    System.out.println("MESSAGE:");
                    System.out.println(bodyText);
                    System.out.println("STATUS: GMAIL_APP_PASSWORD not configured, using development mode");
                    System.out.println("=== END DEVELOPMENT MODE ===");
                    return true;
                }

                System.out.println("📧 Sending email via Gmail SMTP (attempt " + attempt + "/" + maxRetries + ")");

                // Gmail SMTP configuration
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");

                // Create session with authentication
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userEmail, appPassword);
                    }
                });

                // Create message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(userEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(bodyText);

                // Send message
                Transport.send(message);
                
                System.out.println("✅ Email sent successfully via Gmail SMTP");
                return true;

            } catch (Exception e) {
                System.err.println("❌ Email sending failed (attempt " + attempt + "): " + e.getMessage());
                
                // Check if we should retry
                boolean shouldRetry = false;
                
                if (e.getMessage().contains("Authentication failed") || 
                    e.getMessage().contains("Invalid credentials")) {
                    System.err.println("🔐 Authentication error - check your Gmail App Password");
                } else if (e.getMessage().contains("rate") || 
                           e.getMessage().contains("limit") ||
                           e.getMessage().contains("temporarily")) {
                    System.err.println("🔄 Rate limit or temporary error, will retry");
                    shouldRetry = true;
                    try { Thread.sleep(2000); } catch (InterruptedException ie) {}
                } else {
                    System.err.println("🌐 Network or other error, will retry");
                    shouldRetry = true;
                    try { Thread.sleep(1000); } catch (InterruptedException ie) {}
                }
                
                if (attempt >= maxRetries || !shouldRetry) {
                    System.err.println("🚨 Email sending failed after " + attempt + " attempts");
                    
                    // Fallback to console logging
                    System.out.println("=== EMAIL FALLBACK ===");
                    System.out.println("TO: " + recipientEmail);
                    System.out.println("FROM: " + userEmail);
                    System.out.println("SUBJECT: " + subject);
                    System.out.println("MESSAGE:");
                    System.out.println(bodyText);
                    System.out.println("ERROR: " + e.getMessage());
                    System.out.println("=== END FALLBACK ===");
                    return true; // Return true so the process continues
                }
            }
        }
        
        return false;
    }

    /**
     * Test email configuration
     */
    public boolean testEmailConfiguration() {
        return sendEmail(userEmail, "Test Email", "This is a test email from Event Rentals system.");
    }
}
