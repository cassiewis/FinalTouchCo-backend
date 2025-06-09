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

import Website.EventRentals.model.ApiResponse;
import Website.EventRentals.service.S3ServiceImage;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "https://finaltouchco-frontend.onrender.com", allowedHeaders = "*", methods = { RequestMethod.GET })
public class ImageS3Controller {

    private final S3ServiceImage s3ServiceImage;

    @Autowired
    public ImageS3Controller(S3ServiceImage s3ServiceImage) {
        this.s3ServiceImage = s3ServiceImage;
    }

    // Endpoint for fetching all image URLs
    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getAllImageUrls() {
        try {
            List<String> imageUrls = s3ServiceImage.getAllImageUrls();
            return ResponseEntity.ok(new ApiResponse<>(true, imageUrls, "Image URLs fetched successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Endpoint for fetching inspo image URLs
    @GetMapping("/inspo")
    public ResponseEntity<ApiResponse<List<String>>> getInspoImageUrls() {
        try {
            List<String> imageUrls = s3ServiceImage.getInspoImageUrls();
            return ResponseEntity.ok(new ApiResponse<>(true, imageUrls, "Inspo image URLs fetched successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Endpoint for fetching a single image URL by key
    @GetMapping("/{imageKey}")
    public ResponseEntity<ApiResponse<String>> getImageUrl(@PathVariable String imageKey) {
        try {
            String imageUrl = s3ServiceImage.getImageUrl(imageKey);
            return ResponseEntity.ok(new ApiResponse<>(true, imageUrl, "Image URL fetched successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }
}