package id.latenight.creativepos.model;

public class InventoryReceive {
    private String id;
    private String transfer_unique_id;
    private String date;
    private String total_qty;
    private String notes;

    public InventoryReceive(String id, String transfer_unique_id, String date, String total_qty, String notes) {
        this.id = id;
        this.transfer_unique_id = transfer_unique_id;
        this.date = date;
        this.total_qty = total_qty;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }
    public String getTransferUniqueId() {
        return transfer_unique_id;
    }
    public String getDate() {
        return date;
    }
    public String getTotalQty() {
        return total_qty;
    }
    public String getNotes() {
        return notes;
    }
}
