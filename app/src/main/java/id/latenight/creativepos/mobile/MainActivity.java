package id.latenight.creativepos.mobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.CartAdapter;
import id.latenight.creativepos.model.Product;
import id.latenight.creativepos.adapter.ProductAdapter;
import id.latenight.creativepos.model.Cart;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.MyApplication;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static id.latenight.creativepos.util.CapitalizeText.capitalizeText;
import static id.latenight.creativepos.util.MyApplication.RC_ENABLE_BLUETOOTH;

public class MainActivity extends AppCompatActivity implements ProductAdapter.ImageAdapterListener, CartAdapter.AdapterListener, View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private List<Product> list_product;
    private List<Cart> list_cart;
    private ArrayList<String> outletList, customerList, tableList, categoryList;
    private ArrayAdapter<String> CustomerAdapter;
    private ProductAdapter productAdapter;
    private CartAdapter cartAdapter;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private RadioButton radioDinein, radioTakeaway, radioDelivery, radioTaken;
    private Button btnUseDiscount, btnRemoveDiscount;
    private ImageView btn_carts;
    private ImageButton btnAddCustomer;
    private ImageView icLogo, catatanDialog, diskonDialog;
    private ProgressBar progressBar;
    private Spinner spinnerOutlet, spinnerTable, spinnerCategory;
    private SearchableSpinner spinnerCustomer;
    private String orderType = "1";
    private TextView totalCart, username;
    private RelativeLayout lytAlert, lytCart;
    private LinearLayout lytBtnDineIn,lytBtnDelivery,lytBtnTakeAway,lytBtnTaken, lytTable, containerCart, lytOutlet;
    private TextView txtAlert;
    private Animation slideUp,slideDown;
    private ProgressDialog loadingDialog;
    private String order_id;
    private Boolean update_order = false;
    private Boolean split_bill = false;
    private NumberFormat formatRupiah;
    private EditText discount, notes, saleDate, customerEdit;

    private String discount_value;
    private SessionManager sessionManager;
    private AlertDialog dialog;
    private final String TAG = MainActivity.class.getSimpleName();
    private DatabaseHandler db;
    private String currentTotalPrice, totalPriceAfterDiscount;
    private Boolean discountUsed = false;
    private String param_outlet_name = "0";
    private String param_customer_id = "0";
    private String param_category = "0";
    private String param_subcategory = "0";
    private String param_label = "0";
    private AlertDialog.Builder dialogAddCustomer;
    private LayoutInflater inflater;
    private View dialogAddCustomerView;
    private EditText txt_name, txt_car_type, txt_handphone, txt_address, txt_catatan;
    private final int RC_CAMERA_PERM = 123;
    private String Calenders, Catatan, Diskon;
    private static final int SCAN_ACTIVITY_REQUEST_CODE = 12345;
    private BottomSheetDialog bottomSheetDialog;

    private ImageView btnBackTab;

    private TabLayout tabCategory;

    private Integer dialogStart;

    private ListView convertText;
    private EditText Customers;

    private String totalCartSave;
    private TextView cart_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();

        db = new DatabaseHandler(this);

        sessionManager = new SessionManager(this);
        String printerAddress = sessionManager.getPrinter();
        //Log.e("Printer", printerAddress);

        formatRupiah = NumberFormat.getInstance();

        icLogo = findViewById(R.id.ic_logo);
        getLogo();
        username = findViewById(R.id.username);
        username.setText(sessionManager.getFullname());
        progressBar = findViewById(R.id.progressBar);
        totalCart = findViewById(R.id.total_cart);
        Button btnOrder = findViewById(R.id.btn_order);
        discount = findViewById(R.id.discount);
        notes = findViewById(R.id.notes);
        saleDate = findViewById(R.id.sale_date);
        btnUseDiscount = findViewById(R.id.btn_use_discount);
        btnRemoveDiscount = findViewById(R.id.btn_remove_discount);
        btnAddCustomer = findViewById(R.id.btn_add_customer);
        btnOrder.setOnClickListener(this);
        btnUseDiscount.setOnClickListener(this);
        btnRemoveDiscount.setOnClickListener(this);
        btnAddCustomer.setOnClickListener(this);

        ImageView btnCalender = findViewById(R.id.btn_calender);
//        btnCalender.setOnClickListener(this);

        Calenders = "";
        Calendar myCalendar = Calendar.getInstance();
        btnCalender.setOnClickListener(v -> new DatePickerDialog(MainActivity.this, (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dateFormat = "dd-MM-yyyy";
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            Calenders = sdf.format(myCalendar.getTime());
        },
        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
        myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        Log.e("onCreate: ", Calenders);
        Button btnPay = findViewById(R.id.btn_pay);
        btnPay.setOnClickListener(this);
        RecyclerView recyclerMenu = findViewById(R.id.recycler_menu);
        //recyclerMenu.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list_product = new ArrayList<>();
        customerList = new ArrayList<>();
        tableList = new ArrayList<>();
        categoryList = new ArrayList<>();
        productAdapter = new ProductAdapter(list_product, getApplicationContext(), this);
        recyclerMenu.setAdapter(productAdapter);

        lytBtnDineIn = findViewById(R.id.lyt_btn_dine_in);
        lytBtnDelivery = findViewById(R.id.lyt_btn_delivery);
        lytBtnTakeAway = findViewById(R.id.lyt_btn_take_away);
        lytBtnTaken = findViewById(R.id.lyt_btn_taken);
        lytTable = findViewById(R.id.lyt_table);
        lytOutlet = findViewById(R.id.lyt_outlet);

        Catatan = "";

        radioDinein = findViewById(R.id.radioDinein);
        radioTakeaway = findViewById(R.id.radioTakeaway);
        radioDelivery = findViewById(R.id.radioDelivery);
        radioTaken = findViewById(R.id.radioTaken);

        if(sessionManager.getRole().equals("Admin")) {
            lytOutlet.setVisibility(View.VISIBLE);
        }
        Log.e("Outlet ID ", sessionManager.getOutlet());
        if(sessionManager.getOutlet().equals("3")) {
            lytBtnTaken.setVisibility(View.VISIBLE);
            lytBtnDineIn.setVisibility(View.GONE);
            lytBtnTakeAway.setVisibility(View.GONE);
            lytTable.setVisibility(View.GONE);
            radioTaken.setChecked(true);
        } else if(sessionManager.getOutlet().equals("10")) {
            lytBtnTaken.setVisibility(View.GONE);
            lytBtnDineIn.setVisibility(View.GONE);
            lytBtnTakeAway.setVisibility(View.GONE);
            lytTable.setVisibility(View.GONE);
            radioDelivery.setChecked(true);
        }

        db.truncateCart();
        RecyclerView recyclerCart = findViewById(R.id.recycler_cart);
        recyclerCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerCart.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        list_cart = new ArrayList<>();
        cartAdapter = new CartAdapter(list_cart, getApplicationContext(), this);
        recyclerCart.setAdapter(cartAdapter);

//        spinnerCustomer = findViewById(R.id.customer);
//        spinnerCustomer.setTitle("Pilih Pelanggan");

        Customers = findViewById(R.id.customers);
        ImageView icon_image_customer = findViewById(R.id.icon_list_customer);
        Customers.setOnClickListener(v -> {
            dialogCustomers();
        });
        icon_image_customer.setOnClickListener(v -> {
            dialogCustomers();
        });

        discount_value = "";

        catatanDialog = findViewById(R.id.catatan_dialog);
        TextView catatan_text = findViewById(R.id.catatan_text);
        catatanDialog.setOnClickListener(v -> {
            dialogCatatan();
        });
        catatan_text.setOnClickListener(v -> {
            dialogCatatan();
        });

        diskonDialog = findViewById(R.id.diskon_dialog);
        TextView diskon_text = findViewById(R.id.diskon_text);
        diskonDialog.setOnClickListener(v -> {
            dialogDiskon();
        });
        diskon_text.setOnClickListener(v -> {
            dialogDiskon();
        });

        spinnerTable = findViewById(R.id.table);
        spinnerCategory = findViewById(R.id.category);

        spinnerOutlet = findViewById(R.id.spinner_outlet);
        outletList = new ArrayList<>();

        SearchView searchView = findViewById(R.id.mSearch);
        searchView.setFocusable(false);
        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                productAdapter.getFilter().filter(query);
                return false;
            }
        });

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);
        ImageButton btnCloseAlert = findViewById(R.id.btn_close_alert);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        ImageButton btnScanBarcode = findViewById(R.id.btn_scan_barcode);
        btnScanBarcode.setOnClickListener(this);

        btnCloseAlert.setOnClickListener(this);

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        lytCart = findViewById(R.id.lyt_cart);
        btn_carts = findViewById(R.id.ic_cart);
        cart_text = findViewById(R.id.ic_cart_text);
        btn_carts.setOnClickListener(this);
        cart_text.setOnClickListener(this);
//        lytCart.setOnClickListener(this);
        containerCart = findViewById(R.id.container_cart);

        if (extras != null) {
            update_order = extras.getBoolean("update_order");
            split_bill = extras.getBoolean("split_bill");
            order_id = extras.getString("order_id");

            if(update_order) {
                getOderCartList(order_id);
                btnOrder.setText(getResources().getString(R.string.update_order));
            }
            if(split_bill) {
                getOderCartList(order_id);
                btnOrder.setText(getResources().getString(R.string.split_bill));
            }
        }

        dialogStart = 0;

        btnBackTab = findViewById(R.id.btn_back_tab);
        tabCategory = findViewById(R.id.tab_category);

        testCategory();

        if(!update_order) {
            dialogStart = 0;
            getCustomers("");
            getOutlets();
            getTables();
        }
        getCategories();
    }

    private void testCategory()
    {
        tabCategory.removeAllTabs();
        tabCategory.addTab(tabCategory.newTab().setText("Semua"));
        tabCategory.addTab(tabCategory.newTab().setText("Beras"));
        tabCategory.addTab(tabCategory.newTab().setText("Gula"));
        tabCategory.addTab(tabCategory.newTab().setText("Bumbu"));
        tabCategory.addTab(tabCategory.newTab().setText("Bumbu"));
        tabCategory.addTab(tabCategory.newTab().setText("Bumbu"));
        tabCategory.addTab(tabCategory.newTab().setText("Bumbu"));
        tabCategory.addTab(tabCategory.newTab().setText("Bumbu"));
    }


    // untuk mengosongi edittext
    private void kosong(){
        txt_name.setText(null);
        txt_handphone.setText(null);
        txt_address.setText(null);
    }

    private void kosongCatatan()
    {
        txt_catatan.setText(null);
    }


    private void dialogCustomers()
    {
        String names[] ={"A","B","C","D"};
        dialogAddCustomer = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
        dialogAddCustomer.setCancelable(true);

        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_list_customer, null);
        dialogAddCustomer.setView(convertView);
        convertText = convertView.findViewById(R.id.lv);
        getCustomers("");

        AlertDialog dlg = dialogAddCustomer.create();
        dialogStart = 1;

        SearchView searchView = convertView.findViewById(R.id.mSearchList);
        searchView.setFocusable(false);
        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                CustomerAdapter.getFilter().filter(query);
                return false;
            }
        });

        convertText.setOnItemClickListener((adapterView, view, which, l) -> {
            Log.d(TAG, "showAssignmentsList: " + customerList.get(which).toString());
            param_customer_id = customerList.get(which).toString();
            EditText Customers = findViewById(R.id.customers);
            Customers.setText(customerList.get(which).toString());
            getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            dlg.dismiss();
        });

        ImageView close = (ImageView) convertView.findViewById(R.id.close_list_customer);
        close.setOnClickListener(v ->{
            dlg.dismiss();
        });

        Button add = (Button) convertView.findViewById(R.id.add_costumer_list);
        add.setOnClickListener(v -> {
            addCustomerForm();
            dlg.dismiss();
        });
        dlg.show();
    }

    private void dialogCatatan()
    {
        dialogAddCustomer = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
        inflater = getLayoutInflater();
        dialogAddCustomerView = inflater.inflate(R.layout.form_add_catatan, null);
        dialogAddCustomer.setView(dialogAddCustomerView);
        dialogAddCustomer.setCancelable(true);

        txt_catatan        = dialogAddCustomerView.findViewById(R.id.catatan_text);
        Button button_save = (Button) dialogAddCustomerView.findViewById(R.id.add_customers);
        ImageView close = (ImageView) dialogAddCustomerView.findViewById(R.id.close_list_customer);

        txt_catatan.setText(Catatan);
        final AlertDialog mAlertDialog = dialogAddCustomer.create();
        mAlertDialog.setOnShowListener(dialog -> {
            Button add = button_save;
            add.setOnClickListener(view -> {
                String catatan_text         = txt_catatan.getText().toString();

                Catatan = String.valueOf(txt_catatan.getText());
                dialog.dismiss();

            });
            close.setOnClickListener(v -> {
                dialog.dismiss();
            });
        });
        mAlertDialog.show();
    }
    private void addCustomerForm() {
        dialogAddCustomer = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
        inflater = getLayoutInflater();
        dialogAddCustomerView = inflater.inflate(R.layout.form_add_customer, null);
        dialogAddCustomer.setView(dialogAddCustomerView);
        dialogAddCustomer.setCancelable(true);
//        dialogAddCustomer.setTitle(getResources().getString(R.string.add_customer));

        txt_name        = dialogAddCustomerView.findViewById(R.id.name);
        txt_handphone   = dialogAddCustomerView.findViewById(R.id.handphone);
        txt_address     = dialogAddCustomerView.findViewById(R.id.address);
        Button button_save = (Button) dialogAddCustomerView.findViewById(R.id.add_customers);
        ImageView close = (ImageView) dialogAddCustomerView.findViewById(R.id.close_list_customer);

        kosong();

        final AlertDialog mAlertDialog = dialogAddCustomer.create();
        mAlertDialog.setOnShowListener(dialog -> {
            Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button add = button_save;
            add.setOnClickListener(view -> {
                String name         = txt_name.getText().toString();
                String handphone    = txt_handphone.getText().toString();
                String address      = txt_address.getText().toString();

                Log.e("Result","Nama : " + name + "\n" + "HP : " + handphone + "\n" + "Alamat : " + address + "\n");

                if(name.isEmpty()) {
                    txt_name.setHintTextColor(getResources().getColor(R.color.error));
                    txt_name.setBackgroundResource(R.drawable.border_error);
                } else if(handphone.isEmpty()) {
                    txt_handphone.setHintTextColor(getResources().getColor(R.color.error));
                    txt_handphone.setBackgroundResource(R.drawable.border_error);
                } else if(!name.isEmpty() && !handphone.isEmpty()) {
                    addCustomer(name,handphone,address);
                    dialog.dismiss();
                }
            });
            close.setOnClickListener(v -> {
                dialog.dismiss();
            });
        });
        mAlertDialog.show();
    }

    private void addCustomer(String name, String handphone, String address) {
        showLoading();
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_ADD_CUSTOMER(sessionManager.getPathUrl()),
                response -> {
                    Log.e("RESPONSE ", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String msg_name = jsonObject.getString("name");
                        String msg = jsonObject.getString("msg");
                        showSuccess(msg);
                        getCustomers(msg_name);
                    } catch (JSONException e) {
                        showError("Terjadi kesalahan server");
                    }
                },
                error -> {
                    error.printStackTrace();
                    hideLoading();
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        String jsonError = new String(networkResponse.data);
                        // Print Error!
                        Log.e("Error", jsonError);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("name", name);
                params.put("phone", handphone);
                params.put("address", address);
                params.put("user_id", sessionManager.getId());
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioDinein:
                if (checked)
                    radioDinein.setChecked(true);
                radioTakeaway.setChecked(false);
                radioDelivery.setChecked(false);
                radioTaken.setChecked(false);
                orderType = "1";
                break;
            case R.id.radioTakeaway:
                if (checked)
                    radioDinein.setChecked(false);
                radioTakeaway.setChecked(true);
                radioDelivery.setChecked(false);
                radioTaken.setChecked(false);
                orderType = "2";
                break;
            case R.id.radioDelivery:
                if (checked)
                    radioDinein.setChecked(false);
                radioTakeaway.setChecked(false);
                radioDelivery.setChecked(true);
                radioTaken.setChecked(false);
                orderType = "2";
                break;
            case R.id.radioTaken:
                if (checked)
                    radioDinein.setChecked(false);
                radioTakeaway.setChecked(false);
                radioDelivery.setChecked(false);
                radioTaken.setChecked(true);
                orderType = "3";
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void getOderCartList(String order_id) {
        //Log.e("UPDATE", URI.API_DETAIL_ORDER+order_id);
        progressBar.setVisibility(View.VISIBLE);
        //Log.e("RESPONSE", jsonArray.toString());
        StringRequest stringRequest = new StringRequest(URI.API_DETAIL_ORDER(sessionManager.getPathUrl()) + order_id, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                Log.e("getOderCartList: ", jsonObject.toString());

                String getDiscount = jsonObject.getString("sub_total_discount_value").replace("%", "");

                discount_value = getDiscount;
                Diskon = getDiscount;
                if(Integer.parseInt(getDiscount) != 0)
                {
                    discountUsed = true;
                }
                else
                {
                    discountUsed = false;
                }

                if(jsonObject.getString("cust_notes") != null) {
                      Catatan = jsonObject.getString("cust_notes");
                } else {
                      Catatan = "";
                }

//                spinnerOutlet.setSelection(customerList.indexOf(jsonObject.getString("outlet_name")));

                Customers.setText(jsonObject.getString("customer_name"));

                JSONArray jsonTable = jsonObject.getJSONArray("tables_booked");
                JSONObject jsonobjectTable = jsonTable.getJSONObject(0);
                spinnerTable.setSelection(tableList.indexOf(jsonobjectTable.getString("table_name")));

                JSONArray jsonArray = jsonObject.getJSONArray("items");
                //Log.e("RESPONSE", jsonArray.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    db.addCart(jo.getInt("food_menu_id"), jo.getString("menu_name"), jo.getInt("menu_unit_price"), jo.getInt("menu_unit_price"), jo.getInt("qty"));
                    Cart listData = new Cart(jo.getInt("food_menu_id"), jo.getString("menu_name"), jo.getInt("menu_unit_price"), jo.getInt("menu_unit_price"), jo.getInt("qty"));
                    list_cart.add(listData);
                }
                cartAdapter.notifyDataSetChanged();
                int totalPrice = 0;
                for (int i = 0; i < cartAdapter.getItemCount(); i++) {
                    list_cart.get(i).setPrice(list_cart.get(i).getOriPrice() * list_cart.get(i).getQty());
                    totalPrice += list_cart.get(i).getPrice();
                }
                int rupiah = totalPrice;
                int total_after_discount = rupiah;

                if(discountUsed == true)
                {
                    total_after_discount = total_after_discount - Integer.parseInt(jsonObject.getString("sub_total_discount_value"));
                }
                String after_discount = formatRupiah.format(total_after_discount).replace(',', '.');
                totalCart.setText(getResources().getString(R.string.currency) + " " + after_discount);
                totalCartSave = getResources().getString(R.string.currency) +" "+ formatRupiah.format(totalPrice).replace(',', '.');

                progressBar.setVisibility(View.GONE);

                getCustomers(jsonObject.getString("customer_name"));
                getOutlets();
                getTables();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> progressBar.setVisibility(View.GONE));
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getMenuList(String api_customer_id, String api_category, String api_sub_category, String api_label) {
        String params = "/"+param_outlet_name+"/"+api_customer_id+"/"+api_category+"/"+api_sub_category+"/"+api_label;
        String API_MENU = URI.API_MENU(sessionManager.getPathUrl())+sessionManager.getId()+params;
        Log.e("URL_", API_MENU);
        progressBar.setVisibility(View.VISIBLE);
        request = new JsonArrayRequest(API_MENU, response -> {
            JSONObject jsonObject;
//            Log.e("Response", response.toString());
            list_product.clear();
            productAdapter.notifyDataSetChanged();
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    int cart_qty = 0;
                    //if(update_order) {
                        if(db.getCart(jsonObject.getInt("id")) != null) {
                            Log.e("Row", String.valueOf(i));
                            Cart cart = db.getCart(jsonObject.getInt("id"));
                            cart_qty = cart.getQty();
                        }
                    //}
                    Product listData = new Product(jsonObject.getInt("id"), jsonObject.getString("photo"), jsonObject.getString("name"), jsonObject.getString("category_name"), jsonObject.getString("label_name"), jsonObject.getInt("sale_price"), jsonObject.getInt("reseller_price"), jsonObject.getInt("outlet_price"), jsonObject.getInt("ingredient_stock"), jsonObject.getString("sku_number"), cart_qty);
                    list_product.add(listData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            productAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }, error -> progressBar.setVisibility(View.GONE));
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getOutlets() {
        outletList.clear();
        request = new JsonArrayRequest(URI.API_OUTLETS_LIST(sessionManager.getPathUrl()), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            //outletList.add("" + getResources().getString(R.string.select_outlet) + "");
            for (int i = 0; i < response.length(); i++) {
                try {
                    jsonObject = response.getJSONObject(i);
                    outletList.add(jsonObject.getString("outlet_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerOutlet.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, outletList));
            spinnerOutlet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // your code here
                    Log.e("Spinner", spinnerOutlet.getSelectedItem().toString());
                    param_outlet_name = spinnerOutlet.getSelectedItem().toString();
                    getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getCustomers(String val_customer_name) {
        //Log.e("URL_", URI.API_CUSTOMER);
        request = new JsonArrayRequest(URI.API_CUSTOMER(sessionManager.getPathUrl())+"/"+sessionManager.getId(), response -> {
            JSONObject jsonObject;
            customerList.clear();
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    customerList.add(jsonObject.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            if(val_customer_name == "") {
////                convertText.setSelection(customerList.indexOf(val_customer_name));
//                Customers.setText(val_customer_name);
//            }

            if(dialogStart == 0)
            {
                param_customer_id = customerList.get(0).toString();
                if(val_customer_name != "") {
                    EditText Customers = (EditText) findViewById(R.id.customers);
                    Customers.setText(val_customer_name);
                }
                else{
                    EditText Customers = (EditText) findViewById(R.id.customers);
                    Customers.setText(param_customer_id);
                }
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            }

            if(dialogStart == 1)
            {
//                convertText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, customerList));
                CustomerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, customerList);
                convertText.setAdapter(CustomerAdapter);
            }



//            convertText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                    // your code here
//                    Log.e("Spinner", convertText.getSelectedItem().toString());
//                    param_customer_id = convertText.getSelectedItem().toString();
//                    EditText Customers = findViewById(R.id.customers);
//                    getMenuList(param_customer_id, param_category, param_subcategory, param_label);
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parentView) {
//                    // your code here
//                }
//
//            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getCategories() {
        // Log.e("URL_", URI.API_MAIN_CATEGORIES(sessionManager.getPathUrl())+sessionManager.getId());
        request = new JsonArrayRequest(URI.API_MAIN_CATEGORIES(sessionManager.getPathUrl())+sessionManager.getId(), response -> {
            JSONObject jsonObject;
            // Log.e("Response", response.toString());
//            param_category = "0";
            param_subcategory = "0";
            param_label = "0";
            categoryList.clear();
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    String title_new = jsonObject.getString("category_name").toLowerCase();
                    String capitalize = capitalizeText(title_new);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        categoryList.add(String.valueOf(Html.fromHtml(capitalize, Html.FROM_HTML_MODE_LEGACY)));
                    } else {
                        categoryList.add(String.valueOf(Html.fromHtml(capitalize)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerCategory.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, categoryList));
            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // your code here
                    param_category =  spinnerCategory.getSelectedItem().toString();
                    Log.e("tab", param_category);
                    if(param_category.equals("Semua Produk")) {
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    } else {
                        getSubCategories(param_category);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getSubCategories(String category) {
         Log.e("URL_", URI.API_CATEGORIES(sessionManager.getPathUrl())+sessionManager.getId()+"/"+category);
        request = new JsonArrayRequest(URI.API_CATEGORIES(sessionManager.getPathUrl())+sessionManager.getId()+"/"+category, response -> {
            JSONObject jsonObject;
            // Log.e("Response", response.toString());
            if(response.length() > 0) {
                categoryList.clear();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        String title_new = jsonObject.getString("category_name").toLowerCase();
                        String capitalize = capitalizeText(title_new);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            categoryList.add(String.valueOf(Html.fromHtml(capitalize, Html.FROM_HTML_MODE_LEGACY)));
                        } else {
                            categoryList.add(String.valueOf(Html.fromHtml(capitalize)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            } else {
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            }

            spinnerCategory.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, categoryList));
            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // your code here
                    param_subcategory = spinnerCategory.getSelectedItem().toString();
                    Log.e("sub_tab", param_subcategory);
                    if(param_subcategory.equals("Semua Produk")) {
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    } else if(param_subcategory.equals("Kembali")) {
                        getCategories();
                    } else {
                        getLabels(param_subcategory);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getLabels(String category) {
         Log.e("URL_", URI.API_LABELS(sessionManager.getPathUrl())+sessionManager.getId()+"/"+category);
        request = new JsonArrayRequest(URI.API_LABELS(sessionManager.getPathUrl())+sessionManager.getId()+"/"+category, response -> {
            JSONObject jsonObject;
            // Log.e("Response", response.toString());
            if(response.length() > 0) {
                categoryList.clear();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        String title_new = jsonObject.getString("label_name").toLowerCase();
                        String capitalize = capitalizeText(title_new);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            categoryList.add(String.valueOf(Html.fromHtml(capitalize, Html.FROM_HTML_MODE_LEGACY)));
                        } else {
                            categoryList.add(String.valueOf(Html.fromHtml(capitalize)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            } else {
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            }

            spinnerCategory.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, categoryList));
            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // your code here
                    param_label = spinnerCategory.getSelectedItem().toString();
                    Log.e("sub_tab", param_label);
                    if(param_label.equals("Semua Produk")) {
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    } else if(param_label.equals("Kembali")) {
                        getSubCategories(param_category);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getTables() {
        //Log.e("URL_", URI.API_TABLE);
        request = new JsonArrayRequest(URI.API_TABLE(sessionManager.getPathUrl()), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    tableList.add(jsonObject.getString("name"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerTable.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, tableList));
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    @Override
    public void onImageSelected(Product item) {
        if(item.getIngredientStock() == 0) {
            showLoading();
            showError(getResources().getString(R.string.out_of_stock));
        } else {
            addItem(item);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateQty(Product item, int qty) {
        list_cart.clear();
        cartAdapter.notifyDataSetChanged();
        db.addCart(item.getId(), item.getTitle(), item.getPrice(), item.getPrice(), qty);
        if(qty < 1) {
            db.deleteCart(item.getId());
        }
        int totalPrice = 0;
        int hasil_discount = 0;
        List<Cart> allCarts = db.getAllCart();
        for (Cart cart : allCarts) {
            Cart listData = new Cart(cart.getId(), cart.getName(), cart.getPrice(), cart.getOriPrice(), cart.getQty());
            list_cart.add(listData);
            Log.e("KERANJANG DB", cart.getName() +" | "+ cart.getQty() +" | "+ cart.getQty()*cart.getPrice());
            totalPrice += cart.getQty()*cart.getPrice();
        }
        cartAdapter.notifyDataSetChanged();
        hasil_discount = totalPrice;
        if(discountUsed == true)
        {
            hasil_discount = totalPrice - Integer.parseInt(Diskon);
        }
        totalCartSave = getResources().getString(R.string.currency) +" "+ formatRupiah.format(totalPrice).replace(',', '.');
        String rupiah = formatRupiah.format(hasil_discount);
        totalCart.setText(getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
    }

    @SuppressLint("SetTextI18n")
    private void addItem(Product item) {
        String selectedCustomer = Customers.getText().toString();
        int valuePrice;
        if(selectedCustomer.equals("Outlet")) {
            valuePrice = item.getOutletPrice();
        } else if(selectedCustomer.equals("Mitra")) {
            valuePrice = item.getResellerPrice();
        } else {
            valuePrice = item.getPrice();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateCartQty(Cart item, int product_quantity) {
        db.addCart(item.getId(), item.getName(), item.getPrice(), item.getPrice(), product_quantity);
        if(product_quantity < 1) {
            db.deleteCart(item.getId());
        }
        list_cart.clear();
        int totalPrice = 0;
        int hasil_discount = 0;
        List<Cart> allCarts = db.getAllCart();
        for (Cart cart : allCarts) {
            Cart listData = new Cart(cart.getId(), cart.getName(), cart.getPrice(), cart.getOriPrice(), cart.getQty());
            list_cart.add(listData);
            Log.e("KERANJANG Cart", cart.getName() +" | "+ cart.getQty() +" | "+ cart.getQty()*cart.getPrice());
            totalPrice += cart.getQty()*cart.getPrice();
        }
        hasil_discount = totalPrice;
        if(discountUsed == true)
        {
            hasil_discount = totalPrice - Integer.parseInt(Diskon);
        }
        String rupiah = formatRupiah.format(hasil_discount);
        totalCartSave = getResources().getString(R.string.currency) +" "+ formatRupiah.format(totalPrice).replace(',', '.');
        totalCart.setText(getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRemoveItem(Cart item) {
        list_cart.clear();
        cartAdapter.notifyDataSetChanged();
        db.deleteCart(item.getId());
        int totalPrice = 0;
        List<Cart> allCarts = db.getAllCart();
        for (Cart cart : allCarts) {
            Cart listData = new Cart(cart.getId(), cart.getName(), cart.getPrice(), cart.getOriPrice(), cart.getQty());
            list_cart.add(listData);
            totalPrice += cart.getQty()*cart.getPrice();
        }
        cartAdapter.notifyDataSetChanged();
        String rupiah = formatRupiah.format(totalPrice);
        totalCartSave = getResources().getString(R.string.currency) +" "+ formatRupiah.format(totalPrice).replace(',', '.');
        totalCart.setText(getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
    }

    @Override
    public void onRemove(Product item) {
        list_cart.clear();
        cartAdapter.notifyDataSetChanged();
        int totalPrice = 0;
        int hasil_discount;
        List<Cart> allCarts = db.getAllCart();
        for (Cart cart : allCarts) {
            Cart listData = new Cart(cart.getId(), cart.getName(), cart.getPrice(), cart.getOriPrice(), cart.getQty());
            list_cart.add(listData);
            Log.e("KERANJANG", cart.getName() +" "+ cart.getQty()*cart.getPrice());
            totalPrice += cart.getQty()*cart.getPrice();
        }
        hasil_discount = totalPrice;
        if(discountUsed == true)
        {
            hasil_discount = totalPrice - Integer.parseInt(Diskon);
        }
        cartAdapter.notifyDataSetChanged();
        if(hasil_discount < 0)
        {
            hasil_discount = 0;
        }
        String rupiah = formatRupiah.format(hasil_discount);
        totalCartSave = getResources().getString(R.string.currency) +" "+ formatRupiah.format(totalPrice).replace(',', '.');
        totalCart.setText(getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_order) {
            if(list_cart.size() > 0) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                if(update_order) {
                    alertDialog.setPositiveButton(R.string.update_order, (dialog, whichButton) -> placeOrder(false));
                    alertDialog.setTitle(R.string.update_order);
                    alertDialog.setMessage(getResources().getString(R.string.sure_to_update_order));
                } else if(split_bill) {
                    alertDialog.setPositiveButton(R.string.split_bill, (dialog, whichButton) -> placeOrder(false));
                    alertDialog.setTitle(R.string.split_bill);
                    alertDialog.setMessage(getResources().getString(R.string.sure_to_update_order));
                } else {
//                    alertDialog.setPositiveButton(R.string.place_order, (dialog, whichButton) -> placeOrder(false));
                    alertDialog.setPositiveButton(R.string.place_order, (dialog, whichButton) -> payOrder());
                    alertDialog.setNeutralButton(R.string.save_order, (dialog, whichButton) -> placeOrder(false));
                    alertDialog.setTitle(R.string.save_order);
                    alertDialog.setMessage(getResources().getString(R.string.process_order_to_kitchet));
                }


                alertDialog.setIcon(R.drawable.ic_notif);
                alertDialog.setNegativeButton(R.string.cancel, null);
                AlertDialog orderConfirm = alertDialog.show();
                orderConfirm.show();
                Button placeorder = orderConfirm.getButton(DialogInterface.BUTTON_POSITIVE);
                placeorder.setTextColor(getResources().getColor(R.color.black));
                placeorder.setAllCaps(false);
                Button holdorder = orderConfirm.getButton(DialogInterface.BUTTON_NEUTRAL);
                holdorder.setTextColor(getResources().getColor(R.color.button_filled_blue_gradient));
                holdorder.setAllCaps(false);
                Button cancel = orderConfirm.getButton(DialogInterface.BUTTON_NEGATIVE);
                cancel.setTextColor(getResources().getColor(R.color.error));
                cancel.setAllCaps(false);
            }
        }

        if(v.getId() == R.id.btn_close_alert){
            lytAlert.setVisibility(View.GONE);
        }

        if(v.getId() == R.id.btn_back){
            super.onBackPressed();
        }

        if(v.getId() == R.id.btn_pay){
            payOrder();
        }

//        if(v.getId() == R.id.btn_use_discount){
//            useDiscount();
//        }

        if(v.getId() == R.id.btn_remove_discount){
            removeDiscount();
        }

        if(v.getId() == R.id.btn_add_customer){
            addCustomerForm();
        }

        if(v.getId() == R.id.btn_scan_barcode){
            scanBarcode();
        }

        if(v.getId() == R.id.ic_cart){
            expandCart();
        }

        if(v.getId() == R.id.ic_cart_text){
            expandCart();
        }
    }

    private  void dialogDiskon()
    {
        dialogAddCustomer = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
        inflater = getLayoutInflater();
        dialogAddCustomerView = inflater.inflate(R.layout.form_add_diskon, null);
        dialogAddCustomer.setView(dialogAddCustomerView);
        dialogAddCustomer.setCancelable(true);

        EditText txt_diskon       = (EditText) dialogAddCustomerView.findViewById(R.id.diskon_text);
        LinearLayout background_diskon = (LinearLayout) dialogAddCustomerView.findViewById(R.id.top);
        Button button_save = (Button) dialogAddCustomerView.findViewById(R.id.add_customers);
        Button remove_diskon = (Button) dialogAddCustomerView.findViewById(R.id.removeDiskon);
        ImageView close = (ImageView) dialogAddCustomerView.findViewById(R.id.close_list_customer);
        

        if(discountUsed == true)
        {
            button_save.setVisibility(View.GONE);
            remove_diskon.setVisibility(View.VISIBLE);
            txt_diskon.setText(Diskon);
        }

        if(discountUsed == false)
        {
            remove_diskon.setVisibility(View.GONE);
            txt_diskon.setText(null);
            button_save.setVisibility(View.VISIBLE);
        }

        final AlertDialog mAlertDialog = dialogAddCustomer.create();
        mAlertDialog.setOnShowListener(dialog -> {
            Button add = button_save;
            Button remove = remove_diskon;
            txt_diskon.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    add.setVisibility(View.VISIBLE);
                    remove.setVisibility(View.GONE);
                }
            });
            add.setOnClickListener(view -> {
                String catatan_text         = txt_diskon.getText().toString();

                if(catatan_text.isEmpty()) {
                    txt_diskon.setHintTextColor(getResources().getColor(R.color.error));
                    txt_diskon.setBackgroundResource(R.drawable.border_error);
                } else if(!catatan_text.isEmpty()) {
                    currentTotalPrice = totalCart.getText().toString();
                    currentTotalPrice = currentTotalPrice.replace("Rp. ", "").replace(".","");
                    String rupiah = formatRupiah.format(Integer.valueOf(currentTotalPrice)).replace(',', '.');

                    int val = Integer.parseInt(rupiah.replace(".",""));
                    if(val == 0)
                    {
                        showLoading();
                        showSuccess(("You Cannot Add Discount"));
                        return;
                    }

                    if(val < Integer.parseInt(catatan_text))
                    {
                        showLoading();
                        showSuccess(("You Cannot Add Discount"));
                        return;
                    }

                    useDiscount(catatan_text);
                    discount_value = catatan_text;
                    TextView diskon_txt = findViewById(R.id.diskon_text);
                    diskon_txt.setTextColor(getResources().getColor(R.color.text_bluesky));
                    dialog.dismiss();
                }
            });

            remove.setOnClickListener(v ->{
                removeDiscount();
                TextView diskon_txt = findViewById(R.id.diskon_text);
                diskon_txt.setTextColor(getResources().getColor(R.color.black));
                dialog.dismiss();
            });
            close.setOnClickListener(v -> {
                dialog.dismiss();
            });
        });
        mAlertDialog.show();
    }
    private void useDiscount(String discount_value) {
//        String discount_value = discount.getText().toString();
        if(!discount_value.equals("")) {
            showLoading();
            discount_value = discount_value.replace("Rp. ", "").replace(".","");

            currentTotalPrice = totalCartSave;
            currentTotalPrice = currentTotalPrice.replace("Rp. ", "").replace(".","");

            int afterDiscount = Integer.valueOf(currentTotalPrice) - Integer.valueOf(discount_value);
            String rupiah = formatRupiah.format(afterDiscount).replace(',', '.');
            Diskon = discount_value;
            totalCart.setText(getResources().getString(R.string.currency) + " " + rupiah);
            discountUsed = true;
            showSuccess(getResources().getString(R.string.discount_added));
        }
    }

    private void removeDiscount() {
        showLoading();
        currentTotalPrice = totalCart.getText().toString();
        currentTotalPrice = currentTotalPrice.replace("Rp. ", "").replace(".","");
        String rupiah = formatRupiah.format(Integer.valueOf(currentTotalPrice)).replace(',', '.');
        int u;
        u = Integer.parseInt(rupiah.replace(".","")) + Integer.parseInt(discount_value);
        if(Integer.parseInt(rupiah.replace(".","")) == 0)
        {
            u = 0;
        }
        totalCart.setText(getResources().getString(R.string.currency) + " " + formatRupiah.format(u).replace(',', '.'));
        discountUsed = false;
        showSuccess(getResources().getString(R.string.discount_remove));
    }

    @SuppressLint("InflateParams")
    public void PrintKOT(String invoice_kitchen, String invoice_bar, String invoice_table) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_print_kot, null);
        builder.setView(dialogView);
        //dialog.setCancelable(false);
        builder.setIcon(R.drawable.ic_notif);
        builder.setTitle(getResources().getString(R.string.print_order_list));

        Button kitchen = dialogView.findViewById(R.id.kitchen);
        Button bar = dialogView.findViewById(R.id.bar);
        Button table = dialogView.findViewById(R.id.table);
        Button close = dialogView.findViewById(R.id.close);

        kitchen.setOnClickListener(v -> buttonPrintKot(invoice_kitchen));
        bar.setOnClickListener(v -> buttonPrintKot(invoice_bar));
        table.setOnClickListener(v -> buttonPrintKot(invoice_table));

        dialog = builder.show();


        close.setOnClickListener(v -> dialog.dismiss());
    }

    private void buttonPrintKot(String invoice) {
        //Log.e("STRUK", invoice);
        if(sessionManager.getEnablePrinter().equals("on")) {
            printText(invoice);
        }
    }

    public void printText(String invoice) {
        if(sessionManager.getEnablePrinter().equals("on")) {
            String header = "";
            String body = invoice;
            MyApplication.getApplication().sendMessage(header, body);
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
        }
    }

    @SuppressLint("SetTextI18n")
    private void placeOrder(boolean is_hold) {
        String URL_POST;
        if(is_hold) {
            URL_POST = URI.API_HOLD_ORDER(sessionManager.getPathUrl());
        } else {
            URL_POST = URI.API_PLACE_ORDER(sessionManager.getPathUrl());
        }
        Log.e("POST ", URL_POST);
        if(list_cart.size() > 0) {
            showLoading();
            StringRequest postRequest = new StringRequest(Request.Method.POST, URL_POST,
                    response -> {
                        Log.e("RESPONSE ", response);
                        try {
                            hideLoading();
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                Log.e("RESPONSE ", jsonObject.getString("sale_no") + " | " + jsonObject.getString("sales_information"));
                                if (update_order) {
                                    db.deleteSales(Integer.parseInt(jsonObject.getString("sale_no")));
                                }
                                db.addSales(jsonObject.getString("sale_no"), jsonObject.getString("sales_information"));
                                showSuccess(jsonObject.getString("message"));
                                if (update_order) {
                                    Intent intent = new Intent(getApplicationContext(), OrderHistoryActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (split_bill) {
                                    Intent intent = new Intent(getApplicationContext(), OrderHistoryActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    //PrintKOT(jsonObject.getString("kitchen"), jsonObject.getString("bar"), jsonObject.getString("table"));
                                }
                                discount.setText("");
                                notes.setText("");
                                totalCart.setText("Rp. 0");
                                db.truncateCart();
                                list_cart.clear();
                                cartAdapter.notifyDataSetChanged();
                                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                            } else {
                                showError(jsonObject.getString("message"));
                            }

                        } catch (JSONException e) {
                            showError("Terjadi kesalahan server");
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        hideLoading();
                        showError("Terjadi kesalahan server");

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            // Print Error!
                            Log.e("Error", jsonError);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    // the POST parameters:

                    String customer_id = Customers.getText().toString();
                    String outlet_name = spinnerOutlet.getSelectedItem().toString();
                    String sale_date = Calenders;
                    String waiter_id = sessionManager.getId();
                    String orders_table;
                    if (sessionManager.getOutlet().equals("3")) {
                        orders_table = "VIP 1";
                    } else {
                        orders_table = spinnerTable.getSelectedItem().toString();
                    }

                    String value_discount = discount_value;
                    String value_notes = Catatan;

                    JSONArray array = new JSONArray();
                    for (int i = 0; i < list_cart.size(); i++) {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("id", list_cart.get(i).getId());
                            obj.put("qty", list_cart.get(i).getQty());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        array.put(obj);
                    }

                    if (update_order) {
                        params.put("sale_id", order_id);
                    } else if (split_bill) {
                        params.put("new_sale_id", order_id);
                        params.put("split_bill", split_bill.toString());
                    }
                    params.put("outlet_name", outlet_name);
                    params.put("sale_date", sale_date);
                    params.put("user_id", sessionManager.getId());
                    params.put("customer_id", customer_id);
                    params.put("waiter_id", waiter_id);
                    params.put("order_type", orderType);
                    params.put("sub_total", totalCart.getText().toString());
                    params.put("orders_table", orders_table);
                    if(discountUsed) {
                        params.put("discount", value_discount);
                    }
                    params.put("notes", value_notes);
                    params.put("total_items_in_cart", String.valueOf(list_cart.size()));
                    params.put("items", array.toString());
                    return params;
                }
            };
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Volley.newRequestQueue(getApplicationContext()).add(postRequest);
        }
    }

    @SuppressLint("SetTextI18n")
    private void payOrder() {
        String URL_POST;
        URL_POST = URI.API_PLACE_ORDER(sessionManager.getPathUrl());
        Log.e("POST ", URL_POST);
        Log.e("POST", String.valueOf(list_cart.size()));
        if(list_cart.size() > 0) {
            showLoading();
            StringRequest postRequest = new StringRequest(Request.Method.POST, URL_POST,
                    response -> {
                        Log.e("RESPONSE ", response);
                        try {
                            hideLoading();
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                Log.e("RESPONSE ", jsonObject.getString("sale_no") + " | " + jsonObject.getString("sales_information"));
                                db.addSales(jsonObject.getString("sale_no"), jsonObject.getString("sales_information"));
                                showSuccess(jsonObject.getString("message"));
                                list_cart.clear();
                                cartAdapter.notifyDataSetChanged();
                                discount.setText("");
                                notes.setText("");
                                totalCart.setText("Rp. 0");
                                Intent intent = new Intent(this, OrderDetailActivity.class);
                                intent.putExtra("order_id", jsonObject.getString("sale_no"));
                                startActivity(intent);
                                finish();
                            } else {
                                showError(jsonObject.getString("message"));
                            }

                        } catch (JSONException e) {
                            showError("Terjadi kesalahan server");
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        hideLoading();
                        showError("Terjadi kesalahan server");

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            // Print Error!
                            Log.e("Error", jsonError);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    // the POST parameters:

                    String customer_id = Customers.getText().toString();
                    String outlet_name = "";
                    String sale_date = "";
                    if(sessionManager.getRole().equals("Admin")) {
                        outlet_name = spinnerOutlet.getSelectedItem().toString();
                        sale_date = saleDate.getText().toString();
                    }
                    String waiter_id = sessionManager.getId();
                    String orders_table;
                    if (sessionManager.getOutlet().equals("3")) {
                        orders_table = "VIP 1";
                    } else {
                        orders_table = "";
                    }

                    String value_discount = discount_value;
                    String value_notes = Catatan;

                    JSONArray array = new JSONArray();
                    for (int i = 0; i < list_cart.size(); i++) {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("id", list_cart.get(i).getId());
                            obj.put("qty", list_cart.get(i).getQty());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        array.put(obj);
                    }

                    if (update_order) {
                        params.put("sale_id", order_id);
                    } else if (split_bill) {
                        params.put("new_sale_id", order_id);
                        params.put("split_bill", split_bill.toString());
                    }
                    params.put("outlet_name", outlet_name);
                    params.put("sale_date", sale_date);
                    params.put("user_id", sessionManager.getId());
                    params.put("customer_id", customer_id);
                    params.put("waiter_id", waiter_id);
                    params.put("order_type", orderType);
                    params.put("sub_total", totalCart.getText().toString());
                    params.put("orders_table", orders_table);
                    if(discountUsed) {
                        params.put("discount", value_discount);
                    }
                    params.put("notes", value_notes);
                    params.put("total_items_in_cart", String.valueOf(list_cart.size()));
                    params.put("items", array.toString());
                    return params;
                }
            };
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Volley.newRequestQueue(getApplicationContext()).add(postRequest);
        }
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    private void scanBarcode() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            Intent intent = new Intent(this, ScanActivity.class);
            startActivityForResult(intent, SCAN_ACTIVITY_REQUEST_CODE);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera),
                    RC_CAMERA_PERM, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        Toast.makeText(this, R.string.camera_permission_granted, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == SCAN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK
                // get String data from Intent
                String barcode = data.getStringExtra("barcode");
                // set text view with string
                Log.e("BARCODE", barcode);
                Integer foundIndex = null;
                for (int i = 0; i < list_product.size(); i++) {
                    if (list_product.get(i).getSKUNumber().equalsIgnoreCase(barcode)) {
                        foundIndex = i;
                        break; // we stop iteration because we got our result
                    }
                }
                boolean found = (foundIndex!=null);
                if (found) {
                    Product product = list_product.get(foundIndex);
                    onImageSelected(product);
                } else {
                    showLoading();
                    showError(getResources().getString(R.string.product_not_found));
                }
            }
        }
    }

    public void showLoading() {
        loadingDialog = ProgressDialog.show(this, "",
                "Mohon tunggu...", true);
        loadingDialog.show();
    }
    public void hideLoading() {
        loadingDialog.dismiss();
    }

    public void showError(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.error));
        hideLoading();
        new Handler().postDelayed(() -> {
            lytAlert.setVisibility(View.GONE);
            lytAlert.startAnimation(slideDown);
        }, 1000);
    }

    private void showSuccess(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        lytAlert.setBackgroundResource(R.drawable.alert_success);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.colorAccent));
        hideLoading();
        new Handler().postDelayed(() -> {
            lytAlert.setVisibility(View.GONE);
            lytAlert.startAnimation(slideDown);
        }, 1000);
    }

    private void getLogo() {
        //Log.e("URL_", URI.API_LOGO((sessionManager.getPathUrl()));
        StringRequest stringRequest = new StringRequest(URI.API_LOGO(sessionManager.getPathUrl()), response -> {
            if(response.isEmpty()) {
                icLogo.setImageResource(R.mipmap.ic_logo_foreground);
            } else {
                Glide
                        .with(this)
                        .load(response)
                        .centerCrop()
                        .into(icLogo);
            }
        }, error -> {

        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void expandCart() {
        String total = String.valueOf(totalCart.getText());
        //Log.e("TOTAL", total);
        List<Cart> allCarts = db.getAllCart();
        for (Cart cart : allCarts) {
            Log.e("CART", cart.getName() +" "+ cart.getQty());
        }

        if (bottomSheetDialog == null) {
            bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View v = inflater.inflate(R.layout.layout_bottomsheetdialog, findViewById(R.id.cart_container), false);
            bottomSheetDialog.setContentView(v);

            FrameLayout frameLayout = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (frameLayout != null) {
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(frameLayout);
                bottomSheetBehavior.setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            viewCart(v);

            v.findViewById(R.id.imageClose).setOnClickListener(view -> {
                Log.e("CLOSE", "BOTTOMDIALOG");
                bottomSheetDialog.dismiss();
            });
        }
        bottomSheetDialog.show();
    }

    private void viewCart(View v) {
        RecyclerView recyclerCart = v.findViewById(R.id.recycler_cart);
        recyclerCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerCart.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerCart.setAdapter(cartAdapter);

        list_cart.clear();
        List<Cart> allCarts = db.getAllCart();
        for (Cart cart : allCarts) {
            Cart listData = new Cart(cart.getId(), cart.getName(), cart.getPrice(), cart.getOriPrice(), cart.getQty());
            list_cart.add(listData);
            Log.e("KERANJANG", cart.getName() +" "+ cart.getQty());
        }
        cartAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        super.onBackPressed();  // optional depending on your needs
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
