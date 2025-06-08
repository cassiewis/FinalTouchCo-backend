package Website.EventRentals.service;

import java.util.Map;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Website.EventRentals.repositories.DynamoDbReservedDateRepository;
import Website.EventRentals.shared.model.ReservedDate;

@Service
public class AdminDynamoDbReservedDateService {

    private final DynamoDbReservedDateRepository reservedDateRepository;

    @Autowired
    public AdminDynamoDbReservedDateService(DynamoDbReservedDateRepository reservedDateRepository) {
        this.reservedDateRepository = reservedDateRepository;
    }

    public ReservedDate addReservedDate(String productId, String date, String reservationId, String status) {
        try {
            ReservedDate reservedDate = new ReservedDate(productId, date, reservationId, status);
            reservedDateRepository.save(reservedDate);
            return reservedDate;
        } catch (Exception e) {
            throw e; // Re-throw the exception after logging
        }
    }

    public void deleteReservedDate(String productId, String date) {
        if (itemExists(productId, date)) {
            reservedDateRepository.delete(productId, date);
        } else {
            throw new IllegalArgumentException("Reserved date not found for productId: " + productId + " and date: " + date);
        }
    }

    public boolean itemExists(String productId, String date) {
        ReservedDate reservedDate = reservedDateRepository.get(productId, date);
        return reservedDate != null;
    }

    public List<ReservedDate> queryByReservationId(String reservationId) {
        return reservedDateRepository.queryByReservationId(reservationId);
    }
}