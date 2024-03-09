package id.latenight.creativepos.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.model.Cart;

public class DatabaseHandler extends SQLiteOpenHelper {

    // static variable
    private static final int DATABASE_VERSION = 2;

    // Database name
    private static final String DATABASE_NAME = "HEEPU";

    // table name
    private static final String TABLE_SALES = "sales";
    private static final String TABLE_CART = "cart";

    // column tables
    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";
    private static final String KEY_PRODUCT_ID = "product_id";
    private static final String KEY_PRODUCT_NAME = "product_name";
    private static final String KEY_PRODUCT_PRICE = "product_price";
    private static final String KEY_PRODUCT_QTY = "product_qty";
    private static final String KEY_PRODUCT_ORI_PRICE = "product_ori_price";

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SALES_TABLE = "CREATE TABLE " + TABLE_SALES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_DATA + " TEXT" + ")";
        db.execSQL(CREATE_SALES_TABLE);

        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_PRODUCT_ID + " INTEGER,"
                + KEY_PRODUCT_NAME + " TEXT,"
                + KEY_PRODUCT_PRICE + " INTEGER,"
                + KEY_PRODUCT_ORI_PRICE + " INTEGER,"
                + KEY_PRODUCT_QTY + " INTEGER" + ")";
        db.execSQL(CREATE_CART_TABLE);
    }

    // on Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }

    public void addSales(String id, String sales){
        SQLiteDatabase db  = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_DATA, sales);

        db.insert(TABLE_SALES, null, values);
        db.close();
    }

    public void updateNote(int id, String sales) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_DATA, sales);

        // updating row
        db.update(TABLE_SALES, values, KEY_ID + " = ? ", new String[] { String.valueOf(id) });
        db.close();
    }

    public void deleteSales(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SALES, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    public Sales getSales(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SALES, new String[] { KEY_ID,
                        KEY_DATA }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Sales sales = new Sales(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        // return contact
        return sales;
    }
    // get All Record
    public ArrayList<String> getAllSales() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_SALES;
        Cursor res =  db.rawQuery( selectQuery, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(KEY_DATA)));
            res.moveToNext();
        }
        return array_list;
    }

    public int getSalesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SALES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    public void truncate() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SALES);
        db.close();
    }

    // ------------------------ "CART" table methods ----------------//
    private int getID(int id) {
        SQLiteDatabase db  = getWritableDatabase();
        Cursor cursor = db.query(TABLE_CART, new String[] { KEY_ID, KEY_PRODUCT_ID, KEY_PRODUCT_NAME, KEY_PRODUCT_PRICE, KEY_PRODUCT_ORI_PRICE, KEY_PRODUCT_QTY }, KEY_PRODUCT_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor.moveToFirst()) //if the row exist then return the id
            return cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_ID));
        return -1;
    }
    private int getQTY(int id) {
        SQLiteDatabase db  = getWritableDatabase();
        Cursor cursor = db.query(TABLE_CART, new String[] { KEY_ID, KEY_PRODUCT_ID, KEY_PRODUCT_NAME, KEY_PRODUCT_PRICE, KEY_PRODUCT_ORI_PRICE, KEY_PRODUCT_QTY }, KEY_PRODUCT_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor.moveToFirst()) //if the row exist then return the id
            return cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_QTY));
        return 0;
    }
    public void addCart(int id, String name, int price, int ori_price, int qty){
        SQLiteDatabase db  = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_ID, id);
        values.put(KEY_PRODUCT_NAME, name);
        values.put(KEY_PRODUCT_PRICE, price);
        values.put(KEY_PRODUCT_ORI_PRICE, ori_price);
        values.put(KEY_PRODUCT_QTY, qty);

        int product_id = getID(id);
        if(product_id==-1) {
            db.insert(TABLE_CART, null, values);
        } else {
            db.update(TABLE_CART, values, KEY_PRODUCT_ID + " = ? ", new String[]{String.valueOf(id)});
        }
        db.close();
    }

    public void updateCart(int id, String name, int price, int ori_price, int qty) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_ID, id);
        values.put(KEY_PRODUCT_NAME, name);
        values.put(KEY_PRODUCT_PRICE, price);
        values.put(KEY_PRODUCT_ORI_PRICE, ori_price);
        values.put(KEY_PRODUCT_QTY, qty);

        // updating row
        db.update(TABLE_CART, values, KEY_PRODUCT_ID + " = ? ", new String[] { String.valueOf(id) });
        db.close();
    }

    public void deleteCart(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, KEY_PRODUCT_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    public boolean ifCartExists(int id)
    {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String checkQuery = "SELECT " + KEY_PRODUCT_ID + " FROM " + TABLE_CART + " WHERE " + KEY_PRODUCT_ID + "= '"+String.valueOf(id)+ "'";
        cursor = db.rawQuery(checkQuery,null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public Cart getCart(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String checkQuery = "SELECT * FROM " + TABLE_CART + " WHERE " + KEY_PRODUCT_ID + "= '"+String.valueOf(id)+ "'";
        Cursor cursor = db.rawQuery(checkQuery,null);

        if(cursor.moveToFirst()) {
            cursor.moveToFirst();

            Cart cart = new Cart(
                    cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_ID)),
                    (cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME))),
                    (cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_PRICE))),
                    (cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_ORI_PRICE))),
                    (cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_QTY)))
            );
            // return contact
            cursor.close();
            return cart;
        } else {
            cursor.close();
            return null;
        }
    }
    // get All Record
    public List<Cart> getAllCart() {
        List<Cart> carts = new ArrayList<Cart>();
        String selectQuery = "SELECT  * FROM " + TABLE_CART;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Cart cart = new Cart(
                        c.getInt(c.getColumnIndex(KEY_PRODUCT_ID)),
                        (c.getString(c.getColumnIndex(KEY_PRODUCT_NAME))),
                        (c.getInt(c.getColumnIndex(KEY_PRODUCT_PRICE))),
                        (c.getInt(c.getColumnIndex(KEY_PRODUCT_ORI_PRICE))),
                        (c.getInt(c.getColumnIndex(KEY_PRODUCT_QTY)))
                );

                // adding to product list
                carts.add(cart);
            } while (c.moveToNext());
        }

        return carts;
    }

    public int getCartCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CART;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    public void truncateCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CART);
        db.close();
    }
}
