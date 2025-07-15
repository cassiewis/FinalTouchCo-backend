package Website.EventRentals.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import Website.EventRentals.model.Product;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class AdminS3ServiceProduct {
    private final S3Client s3Client;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());
    private final String bucketName = "products-bucket-final-touch";

    public AdminS3ServiceProduct(@Qualifier("adminS3Client") S3Client adminS3Client) {
        this.s3Client = adminS3Client;
    }

    // Upload a product to S3
    public Product uploadProduct(Product product) {
        String key = product.getProductId() + ".json";
        System.out.println("Fetching product with key: " + key);
        // Check if the product already exists in S3
        if (productExists(key)) {
            throw new RuntimeException("Product with ID " + product.getProductId() + " already exists.");
        }
        String productJson = productToJson(product);
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(productJson)
            );
            // Don't modify the productId - it should remain without .json extension
            // The .json is only for S3 storage, not for the product identifier
            return product; // Return the product object
        } catch (S3Exception e) {
            throw new RuntimeException("Error uploading product to S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }


    // Fetch all products from S3
    public List<Product> getProducts() {
        System.out.println("Listing products in S3 bucket " + bucketName);
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            List<Product> products = s3Client.listObjectsV2(listObjectsV2Request).contents()
                    .stream()
                    .map(this::getProductFromObject)
                    .filter(product -> product != null) // Filter out null values
                    .collect(Collectors.toList());
            
            // Clean up any products that have .json in their productId
            for (Product product : products) {
                if (product.getProductId().endsWith(".json")) {
                    String oldProductId = product.getProductId();
                    String cleanProductId = oldProductId.replace(".json", "");
                    
                    System.out.println("Auto-cleaning product ID: " + oldProductId + " -> " + cleanProductId);
                    
                    // Update the product with clean ID
                    product.setProductId(cleanProductId);
                    
                    // Save the updated product (this will create a new file with clean ID)
                    String updatedProductJson = productToJson(product);
                    String newKey = cleanProductId + ".json";
                    
                    s3Client.putObject(
                        PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(newKey)
                            .contentType("application/json")
                            .build(),
                        RequestBody.fromString(updatedProductJson)
                    );
                    
                    // Delete the old file with the messy key
                    String oldKey = oldProductId + ".json"; // This would be "productId.json.json"
                    try {
                        s3Client.deleteObject(
                            DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(oldKey)
                                .build()
                        );
                        System.out.println("Deleted old file: " + oldKey);
                    } catch (Exception e) {
                        System.err.println("Could not delete old file " + oldKey + ": " + e.getMessage());
                    }
                }
            }
            
            return products;
        } catch (S3Exception e) {
            throw new RuntimeException("Error fetching products from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    // Fetches and converts a single product from S3 by ID
    public Product getProduct(String productId) {
        String key = productId + ".json";
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest)) {
                String productJson = new String(s3ObjectStream.readAllBytes(), StandardCharsets.UTF_8);
                return mapToProduct(productJson);
            }
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Product not found with ID: " + productId, e);
        } catch (S3Exception e) {
            throw new RuntimeException("Error fetching product from S3", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading product data", e);
        }
    }

    // Update an existing product in S3
    public Product updateProduct(String productId, Product updatedProduct) {
        // Always ensure we're working with clean productId (no .json extension)
        String cleanProductId = productId.replace(".json", "");
        String key = cleanProductId + ".json";
        
        // Also ensure the product object has clean ID
        updatedProduct.setProductId(cleanProductId);
        
        String updatedProductJson = productToJson(updatedProduct);
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(updatedProductJson)
            );
            return updatedProduct;
        } catch (S3Exception e) {
            throw new RuntimeException("Error updating product in S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    // Method to delete a product from S3 using its productId
    public void deleteProduct(String productId) {
        // Always ensure we're working with clean productId (no .json extension)
        String cleanProductId = productId.replace(".json", "");
        String key = cleanProductId + ".json";

        try {
            // Deleting the object from S3
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            System.out.println("Successfully deleted product with ID: " + cleanProductId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting product from S3: " + e.getMessage(), e);
        }
    }

    // Converts Product object to JSON
    private String productToJson(Product product) {
        try {
            return objectMapper.writeValueAsString(product);
        } catch (Exception e) {
            throw new RuntimeException("Error converting Product to JSON", e);
        }
    }

    // Converts JSON string to Product object
    private Product mapToProduct(String productJson) {
        try {
            return objectMapper.readValue(productJson, Product.class);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Product", e);
        }
    }

    // Helper method to fetch the JSON string from S3 and map it to a Product object
    private Product getProductFromObject(S3Object s3Object) {
        try {
            // Extract productId by removing .json extension from the S3 key
            String productId = s3Object.key().replace(".json", "");
            return getProduct(productId);
        } catch (RuntimeException e) {
            return null; // Log or handle if needed
        }
    }

    private boolean productExists(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            // Try to fetch the object from S3. If it exists, an exception won't be thrown.
            s3Client.getObject(getObjectRequest);
            return true; // If we get the object, it exists
        } catch (NoSuchKeyException e) {
            return false; // Product doesn't exist
        } catch (S3Exception e) {
            // Handle other exceptions (e.g., permission issues)
            throw new RuntimeException("Error checking if product exists in S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
}
