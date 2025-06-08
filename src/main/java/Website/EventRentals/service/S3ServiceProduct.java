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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3ServiceProduct {
    private final S3Client s3Client;
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private final String bucketName = "products-bucket-final-touch";
    
    public S3ServiceProduct(@Qualifier("generalS3Client") S3Client generalS3Client) {
        this.s3Client = generalS3Client;
    }

    // Fetch all products from S3
    public List<Product> getActiveProducts() {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            List<Product> products = s3Client.listObjectsV2(listObjectsV2Request).contents()
                    .stream()
                    .map(this::getProductFromObject)
                    .filter(product -> product != null && product.getActive()) // Filter out null values and only return active products
                    .collect(Collectors.toList());
            return products;
        } catch (S3Exception e) {
            throw new RuntimeException("Error fetching products from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    // Fetches and converts a single product from S3 by ID
    public Product getActiveProduct(String productId) {
        String key = productId + ".json";
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest)) {
                String productJson = new String(s3ObjectStream.readAllBytes(), StandardCharsets.UTF_8);
                Product product = mapToProduct(productJson);
                if (!product.getActive()) {
                    throw new RuntimeException("Product with ID " + productId + " is not active.");
                } else {
                    return product;
                }
            }
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Product not found with ID: " + productId, e);
        } catch (S3Exception e) {
            throw new RuntimeException("Error fetching product from S3", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading product data", e);
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
            return getActiveProduct(s3Object.key().replace(".json", ""));
        } catch (RuntimeException e) {
            return null; // Log or handle if needed
        }
    }

    // Check if a product exists in S3 and is active
    private boolean productExists(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // Fetch the object from S3
            ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
            String productJson = new String(s3ObjectStream.readAllBytes(), StandardCharsets.UTF_8);
            Product product = objectMapper.readValue(productJson, Product.class);

            // Check if the product is active
            return product.getActive();
        } catch (NoSuchKeyException e) {
            return false; // Product doesn't exist
        } catch (S3Exception e) {
            // Handle other exceptions (e.g., permission issues)
            throw new RuntimeException("Error checking if product exists in S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading product data", e);
        }
    }
}
