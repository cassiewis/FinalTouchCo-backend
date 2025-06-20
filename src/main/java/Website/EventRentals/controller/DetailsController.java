package Website.EventRentals.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import Website.EventRentals.model.AddOnItem;
import Website.EventRentals.model.ApiResponse;
import Website.EventRentals.model.Review;
import Website.EventRentals.model.BlockoutDates;
import Website.EventRentals.service.S3ServiceDetails;

@RestController
@RequestMapping("/api/details")
// @CrossOrigin(origins = "https://finaltouchco-frontend.onrender.com", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST })
public class DetailsController {

    private final S3ServiceDetails s3ServiceDetails;

    @Autowired
    public DetailsController(S3ServiceDetails s3ServiceDetails) {
        this.s3ServiceDetails = s3ServiceDetails;
    }

    // Endpoint for filtering products by IDs
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<Review>>> getAllReviews() {
        try {
            List<Review> reviews = s3ServiceDetails.getAllReviews();
            return ResponseEntity.ok(new ApiResponse<>(true, reviews, "Reviews retrieved successfully"));
        } catch (IllegalArgumentException e) { // Client-side error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/blockoutdates")
    public ResponseEntity<ApiResponse<List<String>>> getAllBlockoutDates() {
        try {
            List<String> blockoutDates = s3ServiceDetails.getAllBlockoutDates();
            return ResponseEntity.ok(new ApiResponse<>(true, blockoutDates, "BlockoutDates retrieved successfully"));
        } catch (IllegalArgumentException e) { // Client-side error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }


     @GetMapping("/addons/{id}")
    public ResponseEntity<ApiResponse<AddOnItem>> getAddonById(@PathVariable String id) {
        System.out.println("CASSIE getAddonById hit in controller id: " + id);
        try {
            AddOnItem addon = s3ServiceDetails.getAddonById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, addon, "Add-on item retrieved successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // @PostMapping("/addons")
    // public ResponseEntity<ApiResponse<List<AddOnItem>>> getAddonsByIds(@RequestBody List<String> ids) {
    //     System.out.println("CASSIE getAddonById hit in controller ids: " + ids);
    //     try {
    //         List<AddOnItem> addons = s3ServiceDetails.getAddonsByIds(ids);
    //         return ResponseEntity.ok(new ApiResponse<>(true, addons, "Add-on items retrieved successfully"));
    //     } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or ID)
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    //             .body(new ApiResponse<>(false, null, e.getMessage()));
    //     } catch (Exception e) { // Server-side error (e.g., unexpected exception)
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
    //     }
    // }
}