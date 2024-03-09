package id.latenight.creativepos.model;

public class Customers {
    private int id;
    private String name;

    public Customers(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setId(int name) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
}
