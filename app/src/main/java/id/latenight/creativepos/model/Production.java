package id.latenight.creativepos.model;

public class Production {
    private String id;
    private String product_name;
    private String date;
    private String unit_name;
    private String prediction;
    private String notes;

    public Production(String id, String product_name, String date, String unit_name, String prediction, String notes) {
        this.id = id;
        this.product_name = product_name;
        this.date = date;
        this.unit_name = unit_name;
        this.prediction = prediction;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }
    public String getProductName() {
        return product_name;
    }
    public String getDate() {
        return date;
    }
    public String getUnitName() {
        return unit_name;
    }
    public String getPrediction() {
        return prediction;
    }
    public String getNotes() {
        return notes;
    }
}
