package Website.EventRentals.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "https://finaltouchco-frontend.onrender.com", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST })
public class ReservationController {

    private final S3ServiceReservation s3ServiceReservation;

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    // Constructor injection for S3ServiceReservation
    public ReservationController(S3ServiceReservation s3ServiceReservation) {
        this.s3ServiceReservation = s3ServiceReservation;
    }

    // Endpoint for uploading a reservation
    @PostMapping
    public ResponseEntity<ApiResponse<Reservation>> addReservation(
                    @RequestBody Reservation reservation,
                    @RequestHeader("X-Recaptcha-Token") String recaptchaToken,
                    HttpServletRequest request
                    ) {
        
        String ip = request.getRemoteAddr();
        Bucket bucket = resolveBucket(ip);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiResponse<>(false, null, "Too many requests. Please try again later."));
        }
        
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
        String url = "https://www.google.com/recaptcha/api/siteverify";
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", token);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, params, Map.class);
        Map body = response.getBody();
        return (Boolean) body.get("success");
    }


/**************************************************
 ***       IP Rate Limiting with Bucket4j      ***
 *************************************************/

    // Add this map to store buckets per IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> Bucket4j.builder()
            .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(60))))
            .build());
    }
}
