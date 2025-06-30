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
import Website.EventRentals.service.DynamoDbReservedDateService;
import Website.EventRentals.shared.model.ReservedDate;

@RestController
@RequestMapping("/api/reservedDates")
// @CrossOrigin(origins = "https://finaltouchco-frontend.onrender.com", allowedHeaders = "*", methods = { RequestMethod.GET})
public class ReservedDateController {

    private final DynamoDbReservedDateService dynamoDbReservedDateService;

    @Autowired
    public ReservedDateController(DynamoDbReservedDateService dynamoDbReservedDateService) {
        this.dynamoDbReservedDateService = dynamoDbReservedDateService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservedDate>>> getAllReservedDates() {
        try {
            List<ReservedDate> dates = dynamoDbReservedDateService.getAllReservedDates();
            return ResponseEntity.ok(new ApiResponse<>(true, dates, "Reserved dates fetched successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/productIds/{date}")
    // Endpoint to get all available product IDs for a specific date
    public ResponseEntity<ApiResponse<List<String>>> getProductIdsByDate(@PathVariable String date) {
        try {
            // Extract just the date part (YYYY-MM-DD) from the incoming date string
            String formattedDate;
            if (date.length() >= 10) {
                formattedDate = date.substring(0, 10); // Gets "2025-07-15" from "2025-07-15T06:00:00.000Z"
            } else {
                formattedDate = date; // Use as-is if it's already in the right format
            }
            System.out.println("Cassie: Original date: " + date + ", Formatted date: " + formattedDate);
            
            List<String> productIds = dynamoDbReservedDateService.getProductIdsByDate(formattedDate);
            return ResponseEntity.ok(new ApiResponse<>(true, productIds, "Available product IDs fetched successfully"));    
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/dates/{productId}")
    // Endpoint to get all reserved dates for a specific product ID
    public ResponseEntity<ApiResponse<List<String>>> getDatesByProductId(@PathVariable String productId) {
        try {
            List<String> dates = dynamoDbReservedDateService.getDatesByProductId(productId);
            return ResponseEntity.ok(new ApiResponse<>(true, dates, "Reserved dates fetched successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
