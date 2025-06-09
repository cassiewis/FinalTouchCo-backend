package Website.EventRentals.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import Website.EventRentals.model.ApiResponse;
import Website.EventRentals.model.Reservation;
import Website.EventRentals.service.AdminS3ServiceReservation;
import Website.EventRentals.service.S3ServiceReservation;

@RestController
@RequestMapping("/api/admin/reservations")
@CrossOrigin(origins = "https://finaltouchco-frontend.onrender.com", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
public class AdminReservationController {

    private final AdminS3ServiceReservation adminS3ServiceReservation;
    private final S3ServiceReservation s3ServiceReservation;

    // Constructor injection for S3ServiceReservation
    public AdminReservationController(AdminS3ServiceReservation adminS3ServiceReservation, S3ServiceReservation s3ServiceReservation) {
        this.adminS3ServiceReservation = adminS3ServiceReservation;
        this.s3ServiceReservation = s3ServiceReservation;
    }

    // Endpoint for fetching all reservations
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Reservation>>> getReservations() {
        try {
            List<Reservation> reservations = adminS3ServiceReservation.getReservations();
            return ResponseEntity.ok(new ApiResponse<>(true, reservations, "Reservations fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "Error fetching reservations: " + e.getMessage()));
        }
    }

    // Endpoint for fetching all reservations that are active, fulfilled, or pending
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/activeReservations")
    public ResponseEntity<ApiResponse<List<Reservation>>> getActiveReservations() {
        try {
            List<Reservation> activeReservations = adminS3ServiceReservation.getActiveReservations();
            return ResponseEntity.ok(new ApiResponse<>(true, activeReservations, "Active reservations fetched successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Endpoint for fetching a single reservation by ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{reservationId}")
    public ResponseEntity<ApiResponse<Reservation>> getReservation(@PathVariable String reservationId) {
        try {
            Reservation reservation = adminS3ServiceReservation.getReservation(reservationId);
            return ResponseEntity.ok(new ApiResponse<>(true, reservation, "Reservation fetched successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
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

    // Endpoint for updating an existing reservation
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<String>> updateReservation(@PathVariable String reservationId, @RequestBody Reservation updatedReservation) {
        try {
            adminS3ServiceReservation.updateReservation(reservationId, updatedReservation);
            return ResponseEntity.ok(new ApiResponse<>(true, "Reservation updated successfully", null));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Method to delete a reservation by ID from S3
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<ApiResponse<String>> deleteReservation(@PathVariable String reservationId) {
        try {
            adminS3ServiceReservation.deleteReservation(reservationId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Reservation deleted successfully from S3", null));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Method to change the status of a reservation
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/changeStatus/{reservationId}")
    public ResponseEntity<ApiResponse<String>> changeReservationStatus(@PathVariable String reservationId, @RequestBody String status) {
        try {
            adminS3ServiceReservation.changeReservationStatus(reservationId, status);
            return ResponseEntity.ok(new ApiResponse<>(true, "Reservation updated successfully", null));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
