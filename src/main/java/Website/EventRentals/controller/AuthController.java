package Website.EventRentals.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import Website.EventRentals.model.AuthRequest;
import Website.EventRentals.model.AuthResponse;
import Website.EventRentals.service.AdminAuthenticationService;
import Website.EventRentals.service.JwtTokenProvider;
@RestController
public class AuthController {

    private final AdminAuthenticationService adminAuthenticationService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AdminAuthenticationService adminAuthenticationService, JwtTokenProvider tokenProvider) {
        this.adminAuthenticationService = adminAuthenticationService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        // Validate credentials using AdminAuthenticationService
        boolean isAuthenticated = adminAuthenticationService.authenticate(authRequest.getUsername(), authRequest.getPassword());
        if (!isAuthenticated) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Generate a static admin token
        String adminToken = tokenProvider.generateAdminToken();

        return ResponseEntity.ok(new AuthResponse(adminToken));
    }
}