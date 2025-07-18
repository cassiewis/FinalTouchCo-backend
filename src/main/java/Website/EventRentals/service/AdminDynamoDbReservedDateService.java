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
            ReservedDate reservedDate = new ReservedDate(productId, date.substring(0, 10), reservationId, status);
            reservedDateRepository.save(reservedDate);
            return reservedDate;
        } catch (Exception e) {
            throw e; // Re-throw the exception after logging
        }
    }

    public void deleteReservedDate(String productId, String date) {
        if (itemExists(productId, date.substring(0, 10))) {
            reservedDateRepository.delete(productId, date.substring(0, 10));
        } else {
            // Do nothing since date is already not there, maybe send warning in the future?
            // throw new IllegalArgumentException("Reserved date not found for productId: " + productId + " and date: " + date.substring(0, 10));
        }
    }

    public void removeAllDatesRelatedToReservationId(String reservationId) {
        List<ReservedDate> reservedDates = reservedDateRepository.queryByReservationId(reservationId);
        for (ReservedDate reservedDate : reservedDates) {
            reservedDateRepository.delete(reservedDate.getProductId(), reservedDate.getDate());
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