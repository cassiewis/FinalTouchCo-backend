package Website.EventRentals.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import Website.EventRentals.model.Reservation;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


@Service
public class S3ServiceReservation {
    private final S3Client s3Client;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final String bucketName = "reservations-bucket-final-touch";

    public S3ServiceReservation(@Qualifier("generalS3Client") S3Client generalS3Client) {
        this.s3Client = generalS3Client;
    }

    // Add a reservation request to S3
    public Reservation addReservation(Reservation reservation) {
        String key = reservation.getReservationId() + ".json";
        String reservationJson = reservationToJson(reservation);
        System.out.println("CASSIE S3ServiceReservation: Adding reservation to S3: " + reservationJson);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromString(reservationJson)
        );

        return reservation;
    }

    // Converts reservation object to JSON
    private String reservationToJson(Reservation reservation) {
        try {
            return objectMapper.writeValueAsString(reservation);
        } catch (Exception e) {
            throw new RuntimeException("Error converting Reservation to JSON", e);
        }
    }

}
