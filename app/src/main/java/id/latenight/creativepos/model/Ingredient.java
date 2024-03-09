package id.latenight.creativepos.model;

public class Ingredient {
    private int id;
    private String name;
    private double price;
    private String unit;
    private double qty;
    private double alert_quantity;

    public Ingredient(int id, String name, int price,String unit, int qty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.qty = qty;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public double getQty() {
        return qty;
    }
    public double getAlertQty() {
        return alert_quantity;
    }
    public String getUnit() {
        return unit;
    }

    public void setId(int name) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setQty(double qty) {
        this.qty = qty;
    }
    public void setAlertQty(double alert_quantity) {
        this.alert_quantity = alert_quantity;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
