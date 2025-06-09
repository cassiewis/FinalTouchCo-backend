package Website.EventRentals.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import Website.EventRentals.model.ApiResponse;
import Website.EventRentals.model.Reservation;
import Website.EventRentals.service.S3ServiceReservation;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "https://finaltouchco-frontend.onrender.com", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST })
public class ReservationController {

    private final S3ServiceReservation s3ServiceReservation;

    // Constructor injection for S3ServiceReservation
    public ReservationController(S3ServiceReservation s3ServiceReservation) {
        this.s3ServiceReservation = s3ServiceReservation;
    }

    // Endpoint for uploading a reservation
    @PostMapping
    public ResponseEntity<ApiResponse<Reservation>> addReservation(@RequestBody Reservation reservation, @RequestHeader("X-Recaptcha-Token") String recaptchaToken) {
        if (verifyRecaptcha(recaptchaToken)) {
            try {
                Reservation addedReservation = s3ServiceReservation.addReservation(reservation);
                return ResponseEntity.ok(new ApiResponse<>(true, addedReservation, "Reservation added successfully"));
            } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
            } catch (Exception e) { // Server-side error (e.g., unexpected exception)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, "Recaptcha verification failed"));
        }
    }

    // Example: In your reservation controller/service
    public boolean verifyRecaptcha(String token) {
        String secret = "your_secret_key";
        String url = "https://www.google.com/recaptcha/api/siteverify";
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secret);
        params.add("response", token);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, params, Map.class);
        Map body = response.getBody();
        return (Boolean) body.get("success");
    }

}
