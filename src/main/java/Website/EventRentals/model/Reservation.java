package Website.EventRentals.model;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reservation {
    private String status;
    private String reservationId;
    private String name;
    private List<String> dates;
    private String pickupNotes;

    @JsonProperty("items")
    private List<ReservedItem> items;

    private String email;
    private String phoneNumber;
    private String customerNotes;
    private double price;
    private double deposit;
    private String reservedOn;
    private String invoiceStatus;
    private String paymentStatus;
    private String depositStatus;
    private String myNotes;

    // Getter and Setter for reservationId
    public String getReservationId() {
        return this.reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    // Getter and Setter for status
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getter and Setter for name
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for dates
    public List<String> getDates() {
        return this.dates;
    }

    public void setDates(List<String> dates) { // conversion to string is handled in frontend
        this.dates = dates;
    }

    public String getStartDate() {
        return this.dates != null && !this.dates.isEmpty() ? this.dates.get(0) : null;
    }

    public String getEndDate() {
        return this.dates != null && this.dates.size() > 1 ? this.dates.get(1) : null;
    }

    // Getter and Setter for pickupNotes
    public String getPickupNotes() {
        return this.pickupNotes;
    }

    public void setPickupNotes(String pickupNotes) {
        this.pickupNotes = pickupNotes;
    }

    // Getter and Setter for items
    public List<ReservedItem> getItems() {
        return this.items;
    }

    @JsonIgnore
    public List <String> getItemIds() {
        System.out.println("getItemIds() called. Items: " + (items == null ? "null" : items.size()));
        return this.items.stream().map(ReservedItem::getProductId).toList();
    }

    public int getNumberOfItems() {
        return this.items != null ? this.items.size() : 0;
    }

    public void setItems(List<ReservedItem> items) {
        this.items = items;
    }

    // Getter and Setter for email
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and Setter for phoneNumber
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Getter and Setter for customerNotes
    public String getCustomerNotes() {
        return this.customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    // Getter and Setter for price
    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Getter and Setter for deposit
    public double getDeposit() {
        return this.deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    // Getter and Setter for reservedOn
    public String getReservedOn() {
        return this.reservedOn;
    }

    public void setReservedOn(String reservedOn) {
        this.reservedOn = reservedOn;
    }

    // Getter and Setter for invoiceStatus
    public String getInvoiceStatus() {
        return this.invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    // Getter and Setter for paymentStatus
    public String getPaymentStatus() {
        return this.paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    // Getter and Setter for depositStatus
    public String getDepositStatus() {
        return this.depositStatus;
    }

    public void setDepositStatus(String depositStatus) {
        this.depositStatus = depositStatus;
    }

    // Getter and Setter for myNotes
    public String getMyNotes() {
        return this.myNotes;
    }

    public void setMyNotes(String myNotes) {
        this.myNotes = myNotes;
    }
}