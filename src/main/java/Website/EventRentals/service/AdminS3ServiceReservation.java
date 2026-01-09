package Website.EventRentals.service;
import Website.EventRentals.model.ReservedItem;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import Website.EventRentals.model.Reservation;
import Website.EventRentals.shared.model.ReservedDate;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

// import Website.EventRentals.service.AdminCalendarService;

@Service
public class AdminS3ServiceReservation {
    private final S3Client s3Client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AdminDynamoDbReservedDateService adminDynamoDbReservedDateService;
    // private final AdminCalendarService adminCalendarService;

    private final String bucketName = "reservations-bucket-final-touch";

    public AdminS3ServiceReservation(@Qualifier("adminS3Client") S3Client adminS3Client, 
                                    AdminDynamoDbReservedDateService adminDynamoDbReservedDateService
                                    ) {
        this.s3Client = adminS3Client;
        this.adminDynamoDbReservedDateService = adminDynamoDbReservedDateService;
        // this.adminCalendarService = adminCalendarService;
    }

    // Fetch all reservations from S3
    public List<Reservation> getReservations() {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            List<Reservation> reservations = s3Client.listObjectsV2(listObjectsV2Request).contents()
                    .stream()
                    .map(this::getReservationFromObject)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return reservations;
        } catch (S3Exception e) {
            throw new RuntimeException("Error fetching products from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public Reservation addReservation(Reservation reservation) {
        String key = reservation.getReservationId() + ".json";
        String reservationJson = reservationToJson(reservation);
        System.out.println("CASSIE AdminS3ServiceReservation: Adding reservation to S3: " + reservationJson);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromString(reservationJson)
        );

        return reservation;
    }


    // Fetches all reservations from S3 that are active, pending, or fulfilled
    public List<Reservation> getActiveReservations() {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();
        
            List<Reservation> reservations = s3Client.listObjectsV2(listObjectsV2Request).contents()
                    .stream()
                    .map(this::getReservationFromObject)
                    .filter(reservation -> reservation != null && 
                            (reservation.getStatus().equals("active") || 
                            reservation.getStatus().equals("pending") || 
                            reservation.getStatus().equals("fulfilled")))
                    .collect(Collectors.toList());
            return reservations;
        } catch (S3Exception e) {
            throw new RuntimeException("Error fetching products from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    // Fetches and converts a single reservation from S3 by ID
    public Reservation getReservation(String reservationId) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(reservationId + ".json")
                .build();

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest)) {
            String reservationJson = new String(s3ObjectStream.readAllBytes(), StandardCharsets.UTF_8);
            return mapToReservation(reservationJson);
        } catch (Exception e) {
            System.err.println("Cassie Error fetching reservation from S3 for ID: " + reservationId + ", Error: " + e.getMessage());
            throw new RuntimeException("Error fetching reservation from S3 for ID: " + reservationId, e);
        }
    }

    // Converts reservation object to JSON
    private String reservationToJson(Reservation reservation) {
        try {
            return objectMapper.writeValueAsString(reservation);
        } catch (Exception e) {
            throw new RuntimeException("Error converting Reservation to JSON", e);
        }
    }

    // Converts JSON string to reservation object
    public Reservation mapToReservation(String reservationJson) {
        try {
            return objectMapper.readValue(reservationJson, Reservation.class);
        } catch (Exception e) {
            System.err.println("Error converting JSON to Reservation: " + e.getMessage());
            throw new RuntimeException("Error converting JSON to Reservation", e);
        }
    }

    // Helper method to fetch the JSON string from S3 and map it to a reservation object
    private Reservation getReservationFromObject(S3Object s3Object) {
        String key = s3Object.key();
        try {
            return getReservation(key.replace(".json", ""));
        } catch (RuntimeException e) {
            System.err.println("Error fetching reservation for key: " + key + ", Error: " + e.getMessage());
            return null;
        }
    }

    boolean putReservation(String reservationId, Reservation reservation) {
        try {
            String key = reservationId + ".json";
            String reservationJson = reservationToJson(reservation); // Use the method parameter directly
            s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromString(reservationJson)
            );
        } catch (S3Exception e) {
            return false;
        }
        return true;
    }

    // Update an existing reservation in S3
    public String updateReservation(String reservationId, Reservation updatedReservation) {
        if (!reservationId.equals(updatedReservation.getReservationId())) {
            throw new IllegalArgumentException("Reservation ID in the URL and request body must match.");
        }

        Reservation existingReservation = getReservation(reservationId);
        // if the dates have changed, then update the reserved dates in DynamoDB. Also make sure status is active
        if (!existingReservation.getDates().equals(updatedReservation.getDates()) && updatedReservation.getStatus().equals("active")) {
            // dates have changed, remove old reserved dates
            List<ReservedDate> itemsToDelete = adminDynamoDbReservedDateService.queryByReservationId(reservationId);
            List<String> newDates = updatedReservation.getDates();
            
            adminDynamoDbReservedDateService.removeAllDatesRelatedToReservationId(reservationId); // remove all the dates before re-adding

            // add new reserved dates
            for (String date : newDates) {
                for (ReservedItem item : updatedReservation.getItems()) {
                    // String formattedDate = date.substring(0, 10); // Assuming the date is in the format YYYY-MM-DD
                    System.out.println("Cassie Adding reserved date for reservationId: " + reservationId + ", date: " + date);
                    adminDynamoDbReservedDateService.addReservedDate(item.getProductId(), date, reservationId, updatedReservation.getStatus());
                }
            }
        }
        

        // Check if any new items have been added or removed to the reservation
        if (!existingReservation.getItemIds().equals(updatedReservation.getItemIds()) && updatedReservation.getStatus().equals("active")) {
            // Items have changed, update the reserved dates in DynamoDB
            List<String> oldItems = existingReservation.getItemIds();
            List<String> newItems = updatedReservation.getItemIds();

            // Remove reserved dates for old items
            for (String oldItem : oldItems) {
                if (!newItems.contains(oldItem)) {
                    System.out.println("Cassie Removing reserved dates for old item: " + oldItem);
                    // Loop through each date and remove reserved date for each one
                    for (String date : updatedReservation.getDates()) {
                        // String formattedDate = date.substring(0, 10); // Assuming the date is in the format YYYY-MM-DD
                        adminDynamoDbReservedDateService.deleteReservedDate(oldItem, date);
                    }
                }
            }

            // Add reserved dates for new items
            for (String newItem : newItems) {
                if (!oldItems.contains(newItem)) {
                    System.out.println("Cassie Adding reserved dates for new item: " + newItem);
                    // Loop through each date and add reserved date for each one
                    for (String date : updatedReservation.getDates()) {
                        // String formattedDate = date.substring(0, 10); // Assuming the date is in the format YYYY-MM-DD
                        adminDynamoDbReservedDateService.addReservedDate(newItem, date, reservationId, updatedReservation.getStatus());
                    }
                }
            }
        }


        boolean success = putReservation(reservationId, updatedReservation);

        if (!success) {
            throw new RuntimeException("Error updating reservation in S3");
        }
        return reservationId + ".json";
    }

    // Method to delete a reservation from S3 using its reservationId
    public void deleteReservation(String reservationId) {
        String key = reservationId + ".json"; // Assuming the reservation is stored with a key that ends in ".json"

        try {
            // Deleting the object from S3
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            System.out.println("Successfully deleted reservation with ID: " + reservationId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting reservation from S3: " + e.getMessage(), e);
        }
    }

    public void changeReservationStatus(String reservationId, String status) {
        // change the status
        Reservation reservation = getReservation(reservationId);
        reservation.setStatus(status);

        if (putReservation(reservationId, reservation)){

            if (status.equals("active")) {
                // Add each item
                for (String productId : reservation.getItemIds()) {
                    for (String reservedDate : reservation.getDates()) {
                        System.out.println("CASSIE Adding reserved date: " + reservedDate);
                        adminDynamoDbReservedDateService.addReservedDate(productId, reservedDate, reservationId, "active");
                    }
                }
                // add reservation to calendar
                // String title = "Reservation: " + reservation.getName();
                // String description = "Reservation ID: " + reservationId + "\n" +
                //         "Items: " + reservation.getItemIds().stream().collect(Collectors.joining(", ")) + "\n" +
                //         "Description: " + reservation.getDescription();
                // adminCalendarService.createEvent(title, description, reservation.getStartDateTime(), reservation.getDurationMinutes());
            } else if (status.equals("fulfilled") || status.equals("canceled") || status.equals("pending")) {
                System.out.println("Removing all reserved dates for reservationId: " + reservationId);

                adminDynamoDbReservedDateService.removeAllDatesRelatedToReservationId(reservationId); 
            }
        } else {
            throw new RuntimeException("Error updating reservation in S3");
        }
    }
}
