package Website.EventRentals.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;

import Website.EventRentals.model.ApiResponse;
import Website.EventRentals.model.Review;
import Website.EventRentals.service.AdminS3ServiceDetails;
import Website.EventRentals.model.BlockoutDates;

@RestController
@RequestMapping("/api/admin/details")
// @CrossOrigin(origins = "https://finaltouchco-frontend.onrender.com", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE })
public class AdminDetailsController {

    @Value("${app.frontendUrl}")
    private String frontendUrl;
    
    private final AdminS3ServiceDetails adminS3ServiceDetails;

    @Autowired
    public AdminDetailsController(AdminS3ServiceDetails adminS3ServiceDetails) {
        this.adminS3ServiceDetails = adminS3ServiceDetails;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<Review>> addReview(@RequestBody Review review) {
        try {
            adminS3ServiceDetails.addReview(review);
            return ResponseEntity.ok(new ApiResponse<>(true, review, "Review added successfully"));
        } catch (IllegalArgumentException e) { // Client-side error
            System.out.println("CASSIE illegalArgumentError error: " + e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error
            System.out.println("CASSIE exception: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<String>> removeReview(@PathVariable String reviewId) {
        try {
            adminS3ServiceDetails.removeReview(reviewId);
            return ResponseEntity.ok(new ApiResponse<>(true, reviewId, "Review removed successfully"));
        } catch (IllegalArgumentException e) { // Client-side error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/blockoutdates")
    public ResponseEntity<ApiResponse<List<BlockoutDates>>> getAllBlockoutDates() {
        try {
            List<BlockoutDates> blockoutDates = adminS3ServiceDetails.getAllBlockoutDates();
            return ResponseEntity.ok(new ApiResponse<>(true, blockoutDates, "BlockoutDates retrieved successfully"));
        } catch (IllegalArgumentException e) { // Client-side error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/blockoutdates")
    public ResponseEntity<ApiResponse<BlockoutDates>> addBlockoutDates(@RequestBody BlockoutDates blockoutDates) {
        try {
            adminS3ServiceDetails.addBlockoutDates(blockoutDates);
            return ResponseEntity.ok(new ApiResponse<>(true, blockoutDates, "BlockoutDates added successfully"));
        } catch (IllegalArgumentException e) { // Client-side error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/blockoutdates/{id}")
    public ResponseEntity<ApiResponse<String>> removeBlockoutDate(@PathVariable String id) {
        try {
            adminS3ServiceDetails.removeBlockoutDates(id);
            return ResponseEntity.ok(new ApiResponse<>(true, id, "BlockoutDate removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }
}