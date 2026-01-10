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

    @PostMapping("/send-service-quote")
    public ResponseEntity<ApiResponse<String>> sendServiceQuoteRequest(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String address = request.get("address");
            String eventDate = request.get("eventDate");
            String items = request.get("items");
            String times = request.get("times");
            
            // Validate required fields
            if (name == null || email == null || name.trim().isEmpty() || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, "Name and email are required"));
            }

            String subject = "New Quote Request From: " + name;
            String body = String.format(
                "You have received a new quote request.\n\n" +
                "Details:\n" +
                "Name: %s\n" +
                "Email: %s\n" +
                "Address: %s\n" +
                "Event Date: %s\n" +
                "Items: %s\n" +
                "Times: %s\n",
                name, email, address, eventDate, items, times
            );

            boolean sent = simpleEmailService.sendEmail("finaltouchdecor.co@gmail.com", subject, body);

            if (sent) {
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Quote sent", "Quote request sent successfully")
                );
            } else {
                return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, "Failed to send quote request"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Error sending quote request: " + e.getMessage()));
        }
    }

    @PostMapping("/send-inquiry")
    public ResponseEntity<ApiResponse<String>> sendInquiry(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String eventType = (String) request.get("eventType");
            String eventDate = (String) request.get("eventDate");
            String venue = (String) request.get("venue");
            String products = (String) request.get("products");
            String message = (String) request.get("message");
            String hearAboutUs = (String) request.get("hearAboutUs");
            
            // Handle deliveryOptions as an array
            Object deliveryOptionsObj = request.get("deliveryOptions");
            String deliveryOptions = "";
            if (deliveryOptionsObj != null && deliveryOptionsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> deliveryList = (java.util.List<String>) deliveryOptionsObj;
                deliveryOptions = String.join(", ", deliveryList);
            }
            
            // Validate required fields
            if (name == null || email == null || eventType == null || eventDate == null || 
                venue == null || products == null || name.trim().isEmpty() || email.trim().isEmpty() ||
                eventType.trim().isEmpty() || eventDate.trim().isEmpty() || venue.trim().isEmpty() || 
                products.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, "Name, email, event type, event date, venue, and products are required"));
            }

            String subject = "New Inquiry From: " + name;
            String body = String.format(
                "You have received a new inquiry.\n\n" +
                "Customer Details:\n" +
                "Name: %s\n" +
                "Email: %s\n\n" +
                "Event Information:\n" +
                "Event Type: %s\n" +
                "Event Date: %s\n" +
                "Venue: %s\n\n" +
                "Rental Details:\n" +
                "Items Interested In: %s\n" +
                "Delivery Options: %s\n" +
                "Additional Information:\n" +
                "Message: %s\n" +
                "How they heard about us: %s\n",
                name, email, eventType, eventDate, venue, products, 
                deliveryOptions.isEmpty() ? "Not specified" : deliveryOptions,
                message != null && !message.trim().isEmpty() ? message : "No additional message",
                hearAboutUs != null ? hearAboutUs : "Not specified"
            );

            boolean sent = simpleEmailService.sendEmail("finaltouchdecor.co@gmail.com", subject, body);

            if (sent) {
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Inquiry sent", "Inquiry sent successfully")
                );
            } else {
                return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, "Failed to send inquiry"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Error sending inquiry: " + e.getMessage()));
        }
    }

}
