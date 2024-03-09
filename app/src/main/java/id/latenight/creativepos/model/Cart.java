package id.latenight.creativepos.model;

public class Cart {
    private int id;
    private String name;
    private int price;
    private int ori_price;
    private int qty;

    public Cart(int id, String name, int price,int ori_price, int qty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.ori_price = ori_price;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getPrice() {
        return price;
    }
    public int getQty() {
        return qty;
    }
    public int getOriPrice() {
        return ori_price;
    }

    public void setId(int name) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
}
