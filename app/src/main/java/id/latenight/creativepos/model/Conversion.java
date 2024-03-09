package id.latenight.creativepos.model;

import java.util.List;

public class Conversion {
    private int id;
    private String name;
    private double price;
    private String unit;
    private double qty;

    private String unit_name;
    private double alert_quantity;

    private List<String> list;
    private List<String> IngredientList;

    private String unit_text, ingre, satuan, qty_new;

    public Conversion(int id, String name, int price, String unit, int qty, List<String> list, String unit_name, List<String> IngredientList, String unit_text, String ingre, String satuan, String qty_new) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.qty = qty;
        this.list = list;
        this.unit_name = unit_name;
        this.IngredientList = IngredientList;
        this.unit_text = unit_text;
        this.ingre = ingre;
        this.satuan = satuan;
        this.qty_new = qty_new;
    }

    public String getQty_new() {
        return qty_new;
    }

    public void setQty_new(String qty_new) {
        this.qty_new = qty_new;
    }

    public String getUnit_text() {
        return unit_text;
    }

    public void setUnit_text(String unit_text) {
        this.unit_text = unit_text;
    }

    public String getIngre() {
        return ingre;
    }

    public void setIngre(String ingre) {
        this.ingre = ingre;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
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

    public String getUnit_name() {
        return unit_name;
    }

    public List<String> getList() {
        return list;
    }

    public List<String> getIngredientList() {
        return IngredientList;
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
    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }
    public void setList(List<String> list) {
        this.list = list;
    }
    public void setIngredientList(List<String> IngredientList) {
        this.IngredientList = IngredientList;
    }
}
