package Website.EventRentals.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.List;
import Website.EventRentals.model.Review;
import Website.EventRentals.model.AddOnItem;
import Website.EventRentals.model.BlockoutDates;

@Service
public class S3ServiceDetails {

    private final S3Client s3Client;
    private final String detailsBucketName = "details-bucket-final-touch";

    public S3ServiceDetails(@Qualifier("generalS3Client") S3Client generalS3Client) {
        this.s3Client = generalS3Client;
    }

    public List<Review> getAllReviews() throws Exception {
        // Fetch the JSON file from S3
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(detailsBucketName)
                .key("reviews.json")
                .build();

        InputStream inputStream = s3Client.getObject(getObjectRequest);

        // Parse the JSON file into a list of Review objects
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(inputStream, new TypeReference<List<Review>>() {});
    }

    public List<String> getAllBlockoutDates() throws Exception {
        InputStream inputStream = s3Client.getObject(GetObjectRequest.builder()
        .bucket(detailsBucketName)
        .key("blockoutdates.json")
        .build());

        ObjectMapper objectMapper = new ObjectMapper();
        List<BlockoutDates> blockoutDatesList = objectMapper.readValue(
            inputStream,
            new TypeReference<List<BlockoutDates>>() {}
        );

        List<String> allDates = new ArrayList<>();
        for (BlockoutDates blockout : blockoutDatesList) {
            if (blockout.getDates() != null) {
                allDates.addAll(Arrays.asList(blockout.getDates()));
            }
        }
        return allDates;
    }

    public AddOnItem getAddonById(String id) throws Exception {
        // Fetch the JSON file for the specific add-on from S3
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(detailsBucketName)
                .key(id + ".json") // Assuming each add-on is stored as a separate JSON file
                .build();

        InputStream inputStream = s3Client.getObject(getObjectRequest);

        // Parse the JSON file into an AddOnItem object
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(inputStream, AddOnItem.class);
    }

}