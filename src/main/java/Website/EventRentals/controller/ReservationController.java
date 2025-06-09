package Website.EventRentals.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<Reservation>> addReservation(@RequestBody Reservation reservation) {
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
    }

}
