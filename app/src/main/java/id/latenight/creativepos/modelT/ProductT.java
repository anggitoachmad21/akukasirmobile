package id.latenight.creativepos.modelT;

public class ProductT {
    private String imageurl;
    private String title;
    private int price;
    private int reseller_price;
    private int outlet_price;
    private int ingredient_stock;
    private int id;
    private String sku_number;
    private int cart_qty;
    private int capital_price;
    private String category_name;
    private String created_by;

    public ProductT(int id, String imageurl, String title, int price, int reseller_price, int outlet_price, int ingredient_stock, String sku_number, int cart_qty) {
        this.id = id;
        this.imageurl = imageurl;
        this.title = title;
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
    public int getCapitalPrice() {
        return cart_qty;
    }
    public String getCategoryName() {
        return category_name;
    }
    public String getCreatedBy() {
        return created_by;
    }

    public void setCartQty(int cart_qty) {
        this.cart_qty = cart_qty;
    }
    public void setCapitalPrice(int capital_price) {
        this.capital_price = capital_price;
    }
    public void setCategoryNname(String category_name) {
        this.category_name = category_name;
    }
    public void setCreatedBy(String created_by) {
        this.created_by = created_by;
    }
}
