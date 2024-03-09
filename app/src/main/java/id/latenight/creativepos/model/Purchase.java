package id.latenight.creativepos.model;

public class Purchase {
    private String id;
    private String reference_no;
    private String date;
    private String supplier_name;
    private String total_purchase;
    private String notes;

    public Purchase(String id, String reference_no, String date, String supplier_name, String total_purchase, String notes) {
        this.id = id;
        this.reference_no = reference_no;
        this.date = date;
        this.supplier_name = supplier_name;
        this.total_purchase = total_purchase;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }
    public String getReferenceNo() {
        return reference_no;
    }
    public String getDate() {
        return date;
    }
    public String getSupplierName() {
        return supplier_name;
    }
    public String getTotalPurchase() {
        return total_purchase;
    }
    public String getNotes() {
        return notes;
    }
}
