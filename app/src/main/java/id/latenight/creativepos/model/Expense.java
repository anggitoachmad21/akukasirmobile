package id.latenight.creativepos.model;

public class Expense {
    private String id;
    private String note;
    private String date;
    private String amount;
    private String category;
    private String pic;

    public Expense(String id, String note, String date, String amount, String category, String pic) {
        this.id = id;
        this.note = note;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.pic = pic;
    }

    public String getId() {
        return id;
    }
    public String getNote() {
        return note;
    }
    public String getDate() {
        return date;
    }
    public String getAmount() {
        return amount;
    }
    public String getCategory() {
        return category;
    }
    public String getPic() {
        return pic;
    }
}
