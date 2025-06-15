package Website.EventRentals.model;

public class BlockoutDates {
    private String id;
    private String reason;
    private String notes;
    private String[] dates;

    public BlockoutDates(){}

    public BlockoutDates(String id, String reason, String notes, String[] dates) {
        this.id = id;
        this.reason = reason;
        this.notes = notes;
        this.dates = dates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String[] getDates() {
        return dates;
    }
    
    public void setDates(String[] dates) {
        this.dates = dates;
    }
}