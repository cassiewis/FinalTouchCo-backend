package Website.EventRentals.service;
import Website.EventRentals.repositories.DynamoDbReservedDateRepository;
import Website.EventRentals.shared.model.ReservedDate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

@Service
public class DynamoDbReservedDateService {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<ReservedDate> reservedDateTable;
    private final DynamoDbReservedDateRepository reservedDateRepository;

    @Autowired
    public DynamoDbReservedDateService(DynamoDbEnhancedClient generalDynamoDbEnhancedClient, 
                                        DynamoDbReservedDateRepository reservedDateRepository) {
        this.dynamoDbEnhancedClient = generalDynamoDbEnhancedClient;
        this.reservedDateTable = dynamoDbEnhancedClient.table("ProductReservations", TableSchema.fromBean(ReservedDate.class));
        this.reservedDateRepository = reservedDateRepository;
    }

    // Fetch all product reservations from DynamoDB
    public List<ReservedDate> getAllReservedDates() {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        return StreamSupport.stream(reservedDateTable.scan(scanRequest).items().spliterator(), false)
                .collect(Collectors.toList());
    }

    public List<String> getDatesByProductId(String productId) {
        List<String> list =  reservedDateRepository.getDatesByProductId(productId);
        System.out.println("Cassie Reserved dates for product " + productId + ": " + list);
        return list;
    }

    // Fetch all products avaliable for a specific date
    public List<String> getProductIdsByDate(String date) {
        List<String> list = reservedDateRepository.getProductIdsByDate(date);
        System.out.println("Cassie Available products for date " + date + ": " + list);
        return list;
    }


    public boolean itemExists(String productId, String date) {
        ReservedDate reservedDate = reservedDateRepository.get(productId, date);
        return reservedDate != null;
    }
}