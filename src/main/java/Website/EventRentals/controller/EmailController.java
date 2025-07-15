package Website.EventRentals.controller;

import Website.EventRentals.model.ApiResponse;
import Website.EventRentals.service.EmailVerificationService;
import Website.EventRentals.service.SimpleEmailService;
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
    private SimpleEmailService simpleEmailService;

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

    @GetMapping("/test-email")
    public ResponseEntity<ApiResponse<String>> testEmail() {
        try {
            boolean success = simpleEmailService.testEmailConfiguration();
            if (success) {
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Email test completed", 
                    "Check console logs to see if email was sent or if it's in development mode")
                );
            } else {
                return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, "Email test failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Error testing email: " + e.getMessage()));
        }
    }
}
