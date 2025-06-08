package Website.EventRentals.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import Website.EventRentals.model.Product;
import Website.EventRentals.service.AdminS3ServiceProduct;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "http://192.168.0.145:4200", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
public class AdminProductController {

    private final AdminS3ServiceProduct adminS3ServiceProduct;

    @Autowired
    public AdminProductController(AdminS3ServiceProduct adminS3ServiceProduct) {
        this.adminS3ServiceProduct = adminS3ServiceProduct;
    }

    // Endpoint for fetching a single product by ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{productId}")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable String productId) {
        try {
            Product product = adminS3ServiceProduct.getProduct(productId);
            return ResponseEntity.ok(new ApiResponse<>(true, product, "Product retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    // Endpoint for fetching all products
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getProducts() {
        try {
            List<Product> products = adminS3ServiceProduct.getProducts();
            return ResponseEntity.ok(new ApiResponse<>(true, products, "Active reservations fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "Error fetching products: " + e.getMessage()));
        }
    }

    // Endpoint for uploading a product (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> uploadProduct(@RequestBody Product product) {
        try {
            Product uploadedProduct = adminS3ServiceProduct.uploadProduct(product);
            return ResponseEntity.ok(new ApiResponse<>(true, uploadedProduct, "Product uploaded successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Endpoint for updating a product (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable String productId, @RequestBody Product product) {
        try {
            Product updatedProduct = adminS3ServiceProduct.updateProduct(productId, product);
            return ResponseEntity.ok(new ApiResponse<>(true, updatedProduct, "Product updated successfully"));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Method to delete a product by ID from S3
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String productId) {
        try {
            adminS3ServiceProduct.deleteProduct(productId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Product deleted successfully from S3", null));
        } catch (IllegalArgumentException e) { // Client-side error (e.g., invalid status or reservation ID)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) { // Server-side error (e.g., unexpected exception)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
