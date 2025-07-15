package Website.EventRentals.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    @Autowired
    private SimpleEmailService simpleEmailService;

    @Value("${gmail.user.email:finaltouchdecor.co@gmail.com}")
    private String fromEmail;
    
    @Value("${app.email.provider:gmail}")
    private String emailProvider; // gmail, mock

    // Store verification codes temporarily (in production, use Redis or database)
    private final Map<String, VerificationData> verificationCodes = new ConcurrentHashMap<>();
    
    // Cleanup expired codes periodically
    private static final int CODE_EXPIRY_MINUTES = 10;
    private static final int CODE_LENGTH = 6;

    public boolean sendVerificationCode(String email) {
        try {
            // Generate a random 6-digit code
            String code = generateVerificationCode();
            
            // Store the code with expiry time
            verificationCodes.put(email.toLowerCase(), 
                new VerificationData(code, LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES)));
            
            // Send email based on provider configuration
            boolean emailSent = false;
            
            // Use Gmail API (OAuth 2.0)
            String subject = "Your Final Touch Verification Code";
            String message = String.format(
                "Your verification code for your reservation is: %s\n\n" +
                "This code will expire in %d minutes.\n\n" +
                "If you did not request this code, please ignore this email.\n\n" +
                "Thank you for renting from Final Touch!", 
                code, CODE_EXPIRY_MINUTES);
            
            emailSent = simpleEmailService.sendEmail(email, subject, message);
            
            if (emailSent) {
                System.out.println("Simple Email Service: Verification email sent successfully to: " + email);
            } else {
                System.err.println("Simple Email Service: Failed to send verification email to: " + email);
            }
            
            // Clean up expired codes
            cleanupExpiredCodes();
            
            return emailSent;
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            return false;
        }
    }

    public boolean verifyCode(String email, String code) {
        try {
            email = email.toLowerCase();
            VerificationData storedData = verificationCodes.get(email);
            
            if (storedData == null) {
                return false; // No code found for this email
            }
            
            // Check if code has expired
            if (LocalDateTime.now().isAfter(storedData.expiryTime)) {
                verificationCodes.remove(email); // Remove expired code
                return false;
            }
            
            // Check if code matches
            boolean isValid = storedData.code.equals(code);
            
            if (isValid) {
                verificationCodes.remove(email); // Remove used code
            }
            
            return isValid;
        } catch (Exception e) {
            System.err.println("Failed to verify code: " + e.getMessage());
            return false;
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().expiryTime));
    }

    // Inner class to store verification data
    private static class VerificationData {
        final String code;
        final LocalDateTime expiryTime;

        VerificationData(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}
