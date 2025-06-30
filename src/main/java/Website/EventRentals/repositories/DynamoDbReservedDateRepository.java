package Website.EventRentals.repositories;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import Website.EventRentals.shared.model.ReservedDate;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class DynamoDbReservedDateRepository {

    private final DynamoDbTable<ReservedDate> reservedDateTable;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public DynamoDbReservedDateRepository(@Qualifier("adminDynamoDbEnhancedClient") DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.reservedDateTable = dynamoDbEnhancedClient.table("ProductReservations", TableSchema.fromBean(ReservedDate.class));
    }

    public ReservedDate save(ReservedDate reservedDate) {
        reservedDateTable.putItem(reservedDate);
        return reservedDate;
    }

    public ReservedDate get(String productId, String date) {
        return reservedDateTable.getItem(r -> r.key(k -> k.partitionValue(productId).sortValue(date)));
    }

    public void delete(String productId, String date) {
        reservedDateTable.deleteItem(r -> r.key(k -> k.partitionValue(productId).sortValue(date)));
    }

    public List<String> getDatesByProductId(String productId) {
        return reservedDateTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(productId))))
                .items()
                .stream()
                .map(ReservedDate::getDate) // Extract dates
                .collect(Collectors.toList());
    }

    public List<String> getProductIdsByDate(String date) {
        return reservedDateTable.index("date-productId-index") // Use the GSI name
                .query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(date))))
                .stream() // Stream over the pages
                .flatMap(page -> page.items().stream()) // Extract items from each page
                .map(ReservedDate::getProductId) // Extract product IDs
                .collect(Collectors.toList());
    }

    public List<ReservedDate> queryByReservationId(String reservationId) {
        return reservedDateTable.index("reservationId-productId-index")
            .query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(reservationId)))).stream() // Stream over the pages
            .flatMap(page -> page.items().stream()) // Extract items from each page
            .collect(Collectors.toList());
    }

}