package Website.EventRentals.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import Website.EventRentals.model.Reservation;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.util.UUID;
import Website.EventRentals.service.SimpleEmailService;


@Service
public class S3ServiceReservation {
    private final S3Client s3Client;
    private final SimpleEmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final String bucketName = "reservations-bucket-final-touch";

    public S3ServiceReservation(@Qualifier("generalS3Client") S3Client generalS3Client, SimpleEmailService emailService) {
        this.s3Client = generalS3Client;
        this.emailService = emailService;
    }

    // Add a reservation request to S3
    public Reservation addReservation(Reservation reservation) {
        // check if reservation id already exists
        String key = checkReservationKey(reservation);
        String reservationJson = reservationToJson(reservation);
        System.out.println("CASSIE S3ServiceReservation: Adding reservation to S3: " + reservationJson);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromString(reservationJson)
        );
        emailService.sendNewReservationNotification(reservation);
        return reservation;
    }

    // Check if a reservation key already exists in S3, if it does, create a new one
    public String checkReservationKey(Reservation reservation) {
        System.out.println("CASSIE S3ServiceReservation: Checking reservation key: " + reservation.getReservationId());
        // Check if the reservation ID already exists in S3
        String key = reservation.getReservationId() + ".json";
        // if key doesn't exist, return it, otherwise call checkReservationKey again with a new reservationId
        if (s3Client.listObjectsV2Paginator(builder -> builder.bucket(bucketName).prefix(key)).stream()
                .flatMap(response -> response.contents().stream())
                .anyMatch(object -> object.key().equals(key))) {
            System.out.println("CASSIE S3ServiceReservation: Reservation ID already exists: " + key);
            // If the key exists, append a random suffix to make it unique and try again
            reservation.setReservationId(reservation.getReservationId() + UUID.randomUUID().toString().substring(0, 3));
            return checkReservationKey(reservation);
        }
        System.out.println("CASSIE S3ServiceReservation: Reservation ID is unique: " + key);
        return key;
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
