package id.latenight.creativepos.model;

public class Product {
    private String imageurl;
    private String title;
    private String category;
    private String label;
    private int price;
    private int reseller_price;
    private int outlet_price;


    private int ingredient_stock;
    private int id;
    private String sku_number;
    private int cart_qty;

    public Product(int id, String imageurl, String title, String category, String label,  int price, int reseller_price, int outlet_price, int ingredient_stock, String sku_number, int cart_qty) {
        this.id = id;
        this.imageurl = imageurl;
        this.title = title;
        this.category = category;
        this.label = label;
        this.price = price;
        this.reseller_price = reseller_price;
        this.outlet_price = outlet_price;
        this.ingredient_stock = ingredient_stock;
        this.sku_number = sku_number;
        this.cart_qty = cart_qty;
    }

    public int getId() {
        return id;
    }
    public String getImageurl() {
        return imageurl;
    }
    public String getTitle() {
        return title;
    }
    public int getPrice() {
        return price;
    }
    public int getResellerPrice() {
        return reseller_price;
    }
    public int getOutletPrice() {
        return outlet_price;
    }
    public int getIngredientStock() {
        return ingredient_stock;
    }
    public String getSKUNumber() {
        return sku_number;
    }
    public int getCartQty() {
        return cart_qty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    public void setCartQty(int cart_qty) {
        this.cart_qty = cart_qty;
    }
}
