package Website.EventRentals.controller;

import Website.EventRentals.model.ApiResponse;
import Website.EventRentals.service.EmailVerificationService;
import Website.EventRentals.service.GmailApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EmailController {

    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private GmailApiService gmailApiService;

    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<String>> sendVerificationCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, "Email is required"));
            }

            boolean sent = emailVerificationService.sendVerificationCode(email);
            if (sent) {
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Code sent", "Verification code sent successfully")
                );
            } else {
                return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, "Failed to send verification code"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Error sending verification code: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Boolean>> verifyCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            
            if (email == null || code == null || email.trim().isEmpty() || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, false, "Email and code are required"));
            }

            boolean isValid = emailVerificationService.verifyCode(email, code);
            return ResponseEntity.ok(
                new ApiResponse<>(true, isValid, 
                    isValid ? "Code verified successfully" : "Invalid verification code")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, false, "Error verifying code: " + e.getMessage()));
        }
    }

    @GetMapping("/setup-oauth")
    public ResponseEntity<ApiResponse<String>> setupOAuth() {
        try {
            gmailApiService.authorizeGmailAccess();
            return ResponseEntity.ok(
                new ApiResponse<>(true, "OAuth setup initiated", 
                "Check the console logs for authorization URL and instructions")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Error setting up OAuth: " + e.getMessage()));
        }
    }

    @PostMapping("/complete-oauth")
    public ResponseEntity<ApiResponse<String>> completeOAuth(@RequestBody Map<String, String> request) {
        try {
            String authorizationCode = request.get("code");
            String redirectUri = request.get("redirectUri"); // Optional
            
            if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, "Authorization code is required"));
            }

            boolean success;
            if (redirectUri != null && !redirectUri.trim().isEmpty()) {
                success = gmailApiService.completeOAuthFlow(authorizationCode, redirectUri);
            } else {
                success = gmailApiService.completeOAuthFlow(authorizationCode);
            }
            
            if (success) {
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "OAuth completed", 
                    "Gmail API is now configured and ready to send emails")
                );
            } else {
                return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, "Failed to complete OAuth setup"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Error completing OAuth: " + e.getMessage()));
        }
    }
}
