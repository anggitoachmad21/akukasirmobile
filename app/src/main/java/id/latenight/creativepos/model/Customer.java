package id.latenight.creativepos.model;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private int paybale;

    public Customer(int id, String name, String phone, int paybale) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.paybale = paybale;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
    public int getPayable() {
        return paybale;
    }

    public void setId(int name) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
}
