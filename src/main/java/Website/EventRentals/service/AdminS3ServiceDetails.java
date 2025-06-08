package Website.EventRentals.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import java.util.List;
import Website.EventRentals.model.Review;
// import Website.EventRentals.service.S3ServiceDetails;

@Service
public class AdminS3ServiceDetails {

    private final S3Client s3Client;
    private final String detailsBucketName = "details-bucket-final-touch";
    private final S3ServiceDetails s3ServiceDetails;

    public AdminS3ServiceDetails(@Qualifier("adminS3Client") S3Client adminS3Client,
                                    S3ServiceDetails s3ServiceDetails) {
        this.s3Client = adminS3Client;
        this.s3ServiceDetails = s3ServiceDetails;
    }


    public void addReview(Review review) throws Exception {

        List<Review> reviews = s3ServiceDetails.getAllReviews();
        reviews.add(review);
        // Convert the Review object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(reviews);

        // Upload the JSON string to S3
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(detailsBucketName)
                .key("reviews.json")
                .build(), RequestBody.fromString(jsonString));
    }

    public void removeReview(String reviewId) throws Exception {
        // Fetch the existing reviews
        List<Review> reviews = s3ServiceDetails.getAllReviews();

        // Remove the review with the specified ID
        reviews.removeIf(review -> review.getId().equals(reviewId));

        // Convert the updated list of reviews to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(reviews);

        // Upload the updated JSON string to S3
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(detailsBucketName)
                .key("reviews.json")
                .build(), RequestBody.fromString(jsonString));
    }

}