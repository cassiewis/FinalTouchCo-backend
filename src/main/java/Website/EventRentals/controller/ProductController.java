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
import Website.EventRentals.model.Product;
import Website.EventRentals.service.S3ServiceProduct;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://192.168.0.145:4200", allowedHeaders = "*", methods = { RequestMethod.GET })
public class ProductController {

    private final S3ServiceProduct s3ServiceProduct;

    @Autowired
    public ProductController(S3ServiceProduct s3ServiceProduct) {
        this.s3ServiceProduct = s3ServiceProduct;

    }

    // Endpoint for fetching a single product by ID
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Product>> getActiveProduct(@PathVariable String productId) {
        try {
            Product product = s3ServiceProduct.getActiveProduct(productId);
            return ResponseEntity.ok(new ApiResponse<>(true, product, "Product retrieved successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Endpoint for fetching all customer facing products
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getActiveProducts() {
        System.out.println("CASSIE: getActiveProducts called");
        try {
            List<Product> products = s3ServiceProduct.getActiveProducts();
            System.out.println("CASSIE: products = " + products);
            return ResponseEntity.ok(new ApiResponse<>(true, products, "Active products fetched successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or ID)
            System.out.println("CASSIE: IllegalArgumentException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            System.out.println("CASSIE: Exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

}
