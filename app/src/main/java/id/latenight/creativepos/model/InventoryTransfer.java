package id.latenight.creativepos.model;

public class InventoryTransfer {
    private String id;
    private String transfer_unique_id;
    private String date;
    private String total_qty;
    private String notes;
    private String destination_outlet;

    public InventoryTransfer(String id, String transfer_unique_id, String date, String total_qty, String notes, String destination_outlet) {
        this.id = id;
        this.transfer_unique_id = transfer_unique_id;
        this.date = date;
        this.total_qty = total_qty;
        this.notes = notes;
        this.destination_outlet = destination_outlet;
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
    public String getDestinationOutlet() {
        return destination_outlet;
    }
}
