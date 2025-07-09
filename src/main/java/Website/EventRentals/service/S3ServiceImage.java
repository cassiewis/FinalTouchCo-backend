package Website.EventRentals.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3ServiceImage {

    private final S3Client s3Client;
    private final String bucketName = "images-bucket-final-touch";

    public S3ServiceImage(@Qualifier("generalS3Client") S3Client s3Client) {
        this.s3Client = s3Client;
    }
    
    public List<String> getAllImageUrls() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream()
                .map(S3Object::key)
                .map(key -> String.format("https://%s.s3.amazonaws.com/%s", bucketName, key))
                .collect(Collectors.toList());
    }

    public List<String> getInspoImageUrls() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("Inspo/")  // Get all files in the inspo folder
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream()
                .map(S3Object::key)
                .filter(key -> !key.equals("Inspo/"))  // Exclude the folder itself if it appears
                .map(key -> String.format("https://%s.s3.amazonaws.com/%s", bucketName, key))
                .collect(Collectors.toList());
    }

    public String getImageUrl(String imageKey) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, imageKey);
    }
}