package id.latenight.creativepos.tabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import id.latenight.creativepos.adapter.CustomerSpinnerAdapter;
import id.latenight.creativepos.adapterT.CartAdapterTabs;
import id.latenight.creativepos.adapterT.CustomerSpinnerAdapterT;
import id.latenight.creativepos.adapterT.ProductAdapterTabs;
import id.latenight.creativepos.model.Customer;
import id.latenight.creativepos.model.Cart;
import id.latenight.creativepos.model.Product;
import id.latenight.creativepos.modelT.CartT;
import id.latenight.creativepos.modelT.ProductT;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.MyApplication;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static id.latenight.creativepos.util.CapitalizeText.capitalizeText;
import static id.latenight.creativepos.util.MyApplication.RC_ENABLE_BLUETOOTH;

public class MainActivityTabs extends AppCompatActivity implements ProductAdapterTabs.ImageAdapterListener, CartAdapterTabs.AdapterListener, View.OnClickListener, EasyPermissions.PermissionCallbacks, CustomerSpinnerAdapterT.CustomerAdapterListener {
    private List<ProductT> list_product;
    private List<CartT> list_cart;
    private List<Customer> customerList;
    private ArrayList<String> tableList;
    private ProductAdapterTabs productAdapter;
    private CartAdapterTabs cartAdapter;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private RadioButton radioDinein, radioTakeaway, radioDelivery, radioTaken;
    private Button btnUseDiscount, btnRemoveDiscount;
    private Button btnAddCustomer;
    private ImageView icLogo;
    private ProgressBar progressBar;
    private Spinner spinnerTable;
    private String orderType = "1";
    private TextView totalCart, username, selectCustomer;
    private RelativeLayout lytAlert;
    private LinearLayout lytBtnDineIn,lytBtnDelivery,lytBtnTakeAway,lytBtnTaken, lytTable;
    private TextView txtAlert;
    private Animation slideUp,slideDown;
    private ProgressDialog loadingDialog;
    private String order_id;
    private Boolean update_order = false;
    private Boolean split_bill = false;
    private NumberFormat formatRupiah;
    private EditText discount, notes;
    private SessionManager sessionManager;
    private AlertDialog dialog;
    private final String TAG = MainActivityTabs.class.getSimpleName();
    private DatabaseHandler db;
    private String currentTotalPrice;
    private Boolean discountUsed = false;
    private ImageView btnBackTab;
    private TabLayout tabCategory;
    private RecyclerView recyclerMenu;
    private String param_customer_id = "0";
    private String param_category = "0";
    private String param_subcategory = "0";
    private String param_label = "0";
    private AlertDialog.Builder dialogAddCustomer;
    private LayoutInflater inflater;
    private View dialogAddCustomerView;
    private Spinner spinner_customer_category;
    private EditText txt_name, txt_handphone, txt_address, txt_email, txt_transaksi;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String month_parse, day_parse;
    private final int RC_CAMERA_PERM = 123;
    private int param_page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);

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
        btnUseDiscount = findViewById(R.id.btn_use_discount);
        btnRemoveDiscount = findViewById(R.id.btn_remove_discount);
        btnAddCustomer = findViewById(R.id.btn_add_customer);
        btnOrder.setOnClickListener(this);
        btnUseDiscount.setOnClickListener(this);
        btnRemoveDiscount.setOnClickListener(this);
        btnAddCustomer.setOnClickListener(this);

        Button btnPay = findViewById(R.id.btn_pay);
        btnPay.setOnClickListener(this);
        recyclerMenu = findViewById(R.id.recycler_menu);
        recyclerMenu.setLayoutManager(new GridLayoutManager(this, 4));
        //recyclerMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list_product = new ArrayList<>();
        customerList = new ArrayList<>();
        tableList = new ArrayList<>();
        productAdapter = new ProductAdapterTabs(list_product, getApplicationContext(), this);
//        recyclerMenu.setAdapter(productAdapter);
        recyclerMenu.setAdapter(productAdapter);
        recyclerMenu.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (! recyclerView.canScrollVertically(1) && dy > 0){ //1 for down
                    param_page = param_page + 1;
                    getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                }
                else if (! recyclerView.canScrollVertically(-1) && dy < 0){ //1 for down
                    param_page = param_page - 1;
                    if(param_page < 1)
                    {
                        param_page =1;
                        Log.e("onScrolled: ", "Eror");
                        Context context = getApplicationContext();
                        Toast toast = Toast.makeText(context, "No More", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else{
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    }

                }
            }
        });

        lytBtnDineIn = findViewById(R.id.lyt_btn_dine_in);
        lytBtnDelivery = findViewById(R.id.lyt_btn_delivery);
        lytBtnTakeAway = findViewById(R.id.lyt_btn_take_away);
        lytBtnTaken = findViewById(R.id.lyt_btn_taken);
        lytTable = findViewById(R.id.lyt_table);

        radioDinein = findViewById(R.id.radioDinein);
        radioTakeaway = findViewById(R.id.radioTakeaway);
        radioDelivery = findViewById(R.id.radioDelivery);
        radioTaken = findViewById(R.id.radioTaken);

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
        cartAdapter = new CartAdapterTabs(list_cart, getApplicationContext(), this);
        recyclerCart.setAdapter(cartAdapter);

        selectCustomer = findViewById(R.id.customer);
        spinnerTable = findViewById(R.id.table);

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
                param_page = 1;
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
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
        txt_transaksi     = findViewById(R.id.date_transaksi);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        txt_transaksi.setText(sdf.format(cal.getTime()));

        txt_transaksi.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            if((monthOfYear + 1) < 10)
                            {
                                month_parse = "0";
                            }
                            else{
                                month_parse = "";
                            }

                            if(dayOfMonth < 10){
                                day_parse = "0";
                            }
                            else{
                                day_parse = "";
                            }
                            txt_transaksi.setText(day_parse+dayOfMonth + "-" + month_parse+(monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });
        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

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

        btnBackTab = findViewById(R.id.btn_back_tab);
        tabCategory = findViewById(R.id.tab_category);

        if(!update_order) {
            getCustomers("");
            getTables();
        }
    }

    // untuk mengosongi edittext
    private void kosong(){
        txt_name.setText(null);
        txt_handphone.setText(null);
        txt_address.setText(null);
        txt_email.setText(null);
    }
    private void addCustomerForm() {
        dialogAddCustomer = new AlertDialog.Builder(MainActivityTabs.this, R.style.AlertDialogStyle);
        inflater = getLayoutInflater();
        dialogAddCustomerView = inflater.inflate(R.layout.form_add_customer, null);
        dialogAddCustomer.setView(dialogAddCustomerView);
        dialogAddCustomer.setCancelable(true);
        dialogAddCustomer.setTitle(getResources().getString(R.string.add_customer));

        spinner_customer_category = (Spinner) dialogAddCustomerView.findViewById(R.id.spinner_customer_category);
        txt_name        = (EditText) dialogAddCustomerView.findViewById(R.id.name);
        txt_handphone   = (EditText) dialogAddCustomerView.findViewById(R.id.handphone);
        txt_address     = (EditText) dialogAddCustomerView.findViewById(R.id.address);
        txt_email       = (EditText) dialogAddCustomerView.findViewById(R.id.email);

        kosong();

        dialogAddCustomer.setPositiveButton(getResources().getString(R.string.save), null);
        dialogAddCustomer.setNegativeButton(getResources().getString(R.string.cancel), null);

        final AlertDialog mAlertDialog = dialogAddCustomer.create();
        mAlertDialog.setOnShowListener(dialog -> {
            Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view -> {
                String category     = spinner_customer_category.getSelectedItem().toString();
                String name         = txt_name.getText().toString();
                String handphone    = txt_handphone.getText().toString();
                String address      = txt_address.getText().toString();
                String email        = txt_email.getText().toString();

                Log.e("Result","Nama : " + name + "\n" + "HP : " + handphone + "\n" + "Alamat : " + address + "\n");

                if(category.isEmpty()) {
                    spinner_customer_category.setBackgroundResource(R.drawable.border_error);
                } else if(name.isEmpty()) {
                    txt_name.setHintTextColor(getResources().getColor(R.color.error));
                    txt_name.setBackgroundResource(R.drawable.border_error);

//                } else if(address.isEmpty()) {
//                    txt_address.setHintTextColor(getResources().getColor(R.color.error));
//                    txt_address.setBackgroundResource(R.drawable.border_error);
//                }
                } else if(handphone.isEmpty()) {
                    txt_handphone.setHintTextColor(getResources().getColor(R.color.error));
                    txt_handphone.setBackgroundResource(R.drawable.border_error);
                } else if(!category.isEmpty() && !name.isEmpty() && !handphone.isEmpty()) {
                    addCustomer(category,name,handphone,address,email);
                    dialog.dismiss();
                }
            });
        });
        mAlertDialog.show();
    }

    private void addCustomer(String category, String name, String handphone, String address, String email) {
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
                params.put("email", email);
                params.put("category", category);
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

                String getDiscount = jsonObject.getString("sub_total_discount_value").replace("%", "");
                discount.setText(getDiscount);

                if(!jsonObject.getString("notes").equals("null")) {
                    notes.setText(jsonObject.getString("notes"));
                } else {
                    notes.setText("");
                }

                selectCustomer.setText(jsonObject.getString("customer_name"));

                JSONArray jsonTable = jsonObject.getJSONArray("tables_booked");
                JSONObject jsonobjectTable = jsonTable.getJSONObject(0);
                spinnerTable.setSelection(tableList.indexOf(jsonobjectTable.getString("table_name")));

                JSONArray jsonArray = jsonObject.getJSONArray("items");
                //Log.e("RESPONSE", jsonArray.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    db.addCart(jo.getInt("food_menu_id"), jo.getString("menu_name"), jo.getInt("menu_unit_price"), jo.getInt("menu_unit_price"), jo.getInt("qty"));
                    CartT listData = new CartT(jo.getInt("food_menu_id"), jo.getString("menu_name"), jo.getInt("menu_unit_price"), jo.getInt("menu_unit_price"), jo.getInt("qty"));
                    list_cart.add(listData);
                }
                cartAdapter.notifyDataSetChanged();
                int totalPrice = 0;
                for (int i = 0; i < cartAdapter.getItemCount(); i++) {
                    list_cart.get(i).setPrice(list_cart.get(i).getOriPrice() * list_cart.get(i).getQty());
                    totalPrice += list_cart.get(i).getPrice();
                }
                String rupiah = formatRupiah.format(totalPrice).replace(',', '.');
                totalCart.setText(getResources().getString(R.string.currency) + " " + rupiah);
                progressBar.setVisibility(View.GONE);

                getCustomers("");
                getTables();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> progressBar.setVisibility(View.GONE));
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getMenuList(String api_customer_id, String api_category, String api_sub_category, String api_label) {
//        String params = "/"+api_customer_id+"/"+api_category+"/"+api_sub_category+"/"+api_label;
        String params = "/0"+"/"+api_customer_id+"/"+api_category+"/"+api_sub_category+"/"+api_label;
        String API_MENU = URI.API_MENU(sessionManager.getPathUrl())+sessionManager.getId()+params+"?page="+param_page;
        Log.e("URL_", API_MENU);
        progressBar.setVisibility(View.VISIBLE);
        request = new JsonArrayRequest(API_MENU, response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
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
                    ProductT listData = new ProductT(jsonObject.getInt("id"), jsonObject.getString("photo"), jsonObject.getString("name"), jsonObject.getInt("sale_price"), jsonObject.getInt("reseller_price"), jsonObject.getInt("outlet_price"), jsonObject.getInt("ingredient_stock"), jsonObject.getString("sku_number"), cart_qty);
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

    private void getCustomers(String val_customer_name) {
        ////Log.e("URL_", URI.API_CUSTOMER);
        request = new JsonArrayRequest(URI.API_CUSTOMER(sessionManager.getPathUrl())+"/"+sessionManager.getId(), response -> {
            JSONObject jsonObject;
            ////Log.e("Response", response.toString());
            String initialCustomer = "";
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    Customer customer = new Customer(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("phone"), jsonObject.getInt("payable"));
                    customerList.add(customer);

                    if(i==0) {
                        initialCustomer = customerList.get(i).getName();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(!initialCustomer.equals("")) {
                list_cart.clear();
                cartAdapter.notifyDataSetChanged();
                param_customer_id = initialCustomer;
                selectCustomer.setText(initialCustomer);
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            }
            getCategories();
            if(!val_customer_name.isEmpty()) {
                selectCustomer.setText(val_customer_name);
            }
            selectCustomer.setOnClickListener(view -> {
                showCustomAlertDialogBoxForUserList(customerList);
            });
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
            tabCategory.removeAllTabs();
            btnBackTab.setVisibility(View.GONE);
            param_category = "0";
            param_subcategory = "0";
            param_label = "0";
            tabCategory.addTab(tabCategory.newTab().setText("Semua Produk"));
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    String title_new = jsonObject.getString("category_name").toLowerCase();
                    String capitalize = capitalizeText(title_new);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tabCategory.addTab(tabCategory.newTab().setText(Html.fromHtml(capitalize, Html.FROM_HTML_MODE_LEGACY)));
                    } else {
                        tabCategory.addTab(tabCategory.newTab().setText(Html.fromHtml(capitalize)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tabCategory.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    param_category = tab.getText().toString();
                    param_page = 1;
                    //Log.e("tab", param_category);
                    if(param_category.equals("Semua Produk")) {
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    } else {
                        getSubCategories(param_category);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    param_category = tab.getText().toString();
                    //Log.e("tab", param_category);
                    param_page = 1;
                    if(param_category.equals("Semua Produk")) {
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    } else {
                        getSubCategories(param_category);
                    }
                }
            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getSubCategories(String category) {
        // Log.e("URL_", URI.API_CATEGORIES(sessionManager.getPathUrl())+sessionManager.getId()+"/"+category);
        request = new JsonArrayRequest(URI.API_CATEGORIES(sessionManager.getPathUrl())+sessionManager.getId()+"/"+category, response -> {
            JSONObject jsonObject;
            // Log.e("Response", response.toString());
            btnBackTab.setVisibility(View.VISIBLE);
            btnBackTab.setOnClickListener(v -> getCategories());
            if(response.length() > 0) {
                tabCategory.removeAllTabs();
                tabCategory.clearOnTabSelectedListeners();
                tabCategory.addTab(tabCategory.newTab().setText("Semua Produk"));
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        String title_new = jsonObject.getString("category_name").toLowerCase();
                        String capitalize = capitalizeText(title_new);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tabCategory.addTab(tabCategory.newTab().setText(Html.fromHtml(capitalize, Html.FROM_HTML_MODE_LEGACY)));
                        } else {
                            tabCategory.addTab(tabCategory.newTab().setText(Html.fromHtml(capitalize)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                param_page = 1;
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            } else {
                param_page = 1;
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            }

            tabCategory.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    param_subcategory = tab.getText().toString();
                    param_page = 1;
                    //Log.e("sub_tab", param_subcategory);
                    if(param_subcategory.equals("Semua Produk")) {
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    } else {
                        getLabels(param_subcategory);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    param_page = 1;
                    param_subcategory = tab.getText().toString();
                    //Log.e("sub_tab", param_category);
                    if(param_subcategory.equals("Semua Produk")) {
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    } else {
                        getLabels(param_subcategory);
                    }
                }
            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getLabels(String category) {
        // Log.e("URL_", URI.API_LABELS(sessionManager.getPathUrl())+sessionManager.getId()+"/"+category);
        request = new JsonArrayRequest(URI.API_LABELS(sessionManager.getPathUrl())+sessionManager.getId()+"/"+category, response -> {
            JSONObject jsonObject;
            // Log.e("Response", response.toString());
            btnBackTab.setVisibility(View.VISIBLE);
            btnBackTab.setOnClickListener(v -> getCategories());
            if(response.length() > 0) {
                tabCategory.removeAllTabs();
                tabCategory.clearOnTabSelectedListeners();
                tabCategory.addTab(tabCategory.newTab().setText("Semua Produk"));
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        String title_new = jsonObject.getString("label_name").toLowerCase();
                        String capitalize = capitalizeText(title_new);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tabCategory.addTab(tabCategory.newTab().setText(Html.fromHtml(capitalize, Html.FROM_HTML_MODE_LEGACY)));
                        } else {
                            tabCategory.addTab(tabCategory.newTab().setText(Html.fromHtml(capitalize)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                param_page = 1;
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                tabCategory.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        param_page = 1;
                        param_label = tab.getText().toString();
                        //Log.e("label", param_label);
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        param_page = 1;
                        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
                    }
                });
            } else {
                param_page = 1;
                getMenuList(param_customer_id, param_category, param_subcategory, param_label);
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getTables() {
        ////Log.e("URL_", URI.API_TABLE);
        request = new JsonArrayRequest(URI.API_TABLE(sessionManager.getPathUrl()), response -> {
            JSONObject jsonObject;
            ////Log.e("Response", response.toString());
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

    }

    @Override
    public void onRemove(Product item) {

    }

    @Override
    public void onUpdateQty(Product item, int qty) {

    }

    @Override
    public void onImageSelected(ProductT item) {
        if(item.getIngredientStock() == 0) {
            showLoading();
            showError(getResources().getString(R.string.out_of_stock));
        } else {
            addItem(item);
        }
    }

    @SuppressLint("SetTextI18n")
    private void addItem(ProductT item) {
        CartT listData = new CartT(item.getId(), item.getTitle(), item.getPrice(), item.getPrice(), 1);
        list_cart.add(listData);
        cartAdapter.notifyItemChanged(-1);

        int totalPrice = 0;
        for (int i = 0; i < cartAdapter.getItemCount(); i++) {
            totalPrice += list_cart.get(i).getPrice();
        }
        String rupiah = formatRupiah.format(totalPrice);
        totalCart.setText(getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateCartQty(CartT item, int product_quantity) {
        int totalPrice = 0;
        for (int i = 0; i < cartAdapter.getItemCount(); i++) {
            totalPrice += list_cart.get(i).getPrice();
            //Log.e("TOTAL  "+i+": ", String.valueOf(list_cart.get(i).getName()));
        }

        String rupiah = formatRupiah.format(totalPrice);
        totalCart.setText(getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRemoveItem(List<CartT> item) {
        int totalPrice = 0;
        for (int i = 0; i < item.size(); i++) {
            totalPrice += list_cart.get(i).getPrice();
        }

        String rupiah = formatRupiah.format(totalPrice);
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
                    alertDialog.setPositiveButton(R.string.place_order, (dialog, whichButton) -> placeOrder(false));
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

        if(v.getId() == R.id.btn_use_discount){
            useDiscount();
        }

        if(v.getId() == R.id.btn_remove_discount){
            removeDiscount();
        }

        if(v.getId() == R.id.btn_add_customer){
            addCustomerForm();
        }

        if(v.getId() == R.id.btn_scan_barcode){
            scanBarcode();
        }
    }

    private void useDiscount() {
        String discount_value = discount.getText().toString();
        if(!discount_value.equals("")) {
            showLoading();
            discount_value = discount_value.replace("Rp. ", "").replace(".","");

            currentTotalPrice = totalCart.getText().toString();
            currentTotalPrice = currentTotalPrice.replace("Rp. ", "").replace(".","");

            int afterDiscount = Integer.valueOf(currentTotalPrice) - Integer.valueOf(discount_value);
            String rupiah = formatRupiah.format(afterDiscount).replace(',', '.');
            totalCart.setText(getResources().getString(R.string.currency) + " " + rupiah);
            discountUsed = true;
            btnUseDiscount.setVisibility(View.GONE);
            btnRemoveDiscount.setVisibility(View.VISIBLE);
            showSuccess(getResources().getString(R.string.shipping_price_added));
        }
    }

    private void removeDiscount() {
        String rupiah = formatRupiah.format(Integer.valueOf(currentTotalPrice)).replace(',', '.');
        totalCart.setText(getResources().getString(R.string.currency) + " " + rupiah);
        discountUsed = false;
        btnRemoveDiscount.setVisibility(View.GONE);
        btnUseDiscount.setVisibility(View.VISIBLE);
        showSuccess(getResources().getString(R.string.shipping_price_deleted));
    }

    @SuppressLint("InflateParams")
    public void PrintKOT(String invoice_kitchen, String invoice_bar, String invoice_table) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityTabs.this);
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
                                    Intent intent = new Intent(getApplicationContext(), OrderHistoryActivityTabs.class);
                                    startActivity(intent);
                                    finish();
                                } else if (split_bill) {
                                    Intent intent = new Intent(getApplicationContext(), OrderHistoryActivityTabs.class);
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

                    String customer_id = selectCustomer.getText().toString();
                    String waiter_id = sessionManager.getId();
                    String orders_table;
                    if (sessionManager.getOutlet().equals("3")) {
                        orders_table = "VIP 1";
                    } else {
                        orders_table = "";
                    }

                    String value_discount = discount.getText().toString();
                    String value_notes = notes.getText().toString();

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
                    params.put("user_id", sessionManager.getId());
                    params.put("customer_id", customer_id);
                    params.put("waiter_id", waiter_id);
                    params.put("order_type", orderType);
                    params.put("sale_date", txt_transaksi.getText().toString());
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
                                Intent intent = new Intent(this, OrderDetailActivityTabs.class);
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

                    String customer_id = selectCustomer.getText().toString();
                    String waiter_id = sessionManager.getId();
                    String orders_table;
                    if (sessionManager.getOutlet().equals("3")) {
                        orders_table = "VIP 1";
                    } else {
                        orders_table = "";
                    }

                    String value_discount = discount.getText().toString();
                    String value_notes = notes.getText().toString();

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
                    params.put("user_id", sessionManager.getId());
                    params.put("customer_id", customer_id);
                    params.put("sale_date", txt_transaksi.getText().toString());
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

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // check that it is the SecondActivity with an OK result
//        if (requestCode == SCAN_ACTIVITY_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) { // Activity.RESULT_OK
//                // get String data from Intent
//                String barcode = data.getStringExtra("barcode");
//                // set text view with string
//                Log.e("BARCODE", barcode);
//                Integer foundIndex = null;
//                for (int i = 0; i < list_product.size(); i++) {
//                    if (list_product.get(i).getSKUNumber().equalsIgnoreCase(barcode)) {
//                        foundIndex = i;
//                        break; // we stop iteration because we got our result
//                    }
//                }
//                boolean found = (foundIndex!=null);
//                if (found) {
//                    Product product = list_product.get(foundIndex);
//                    onImageSelected(product);
//                } else {
//                    showLoading();
//                    showError(getResources().getString(R.string.product_not_found));
//                }
//            }
//        }
//    }

    // Pass list of your model as arraylist
    private void showCustomAlertDialogBoxForUserList(List<Customer> customer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityTabs.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.customer_spinner_dialog, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.lv_assignment_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        CustomerSpinnerAdapterT customerSpinnerAdapter = new CustomerSpinnerAdapterT(customer, getApplicationContext(), this);

        SearchView searchView = dialogView.findViewById(R.id.search_customer);
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
                customerSpinnerAdapter.getFilter().filter(query);
                return false;
            }
        });

        recyclerView.setAdapter(customerSpinnerAdapter);
        dialog = builder.show();
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
        new Handler().postDelayed(new Runnable() {
            public void run() {
                lytAlert.setVisibility(View.GONE);
                lytAlert.startAnimation(slideDown);
            }
        }, 1000);
    }

    private void showSuccess(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        lytAlert.setBackgroundResource(R.drawable.alert_success);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.colorAccent));
        hideLoading();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                lytAlert.setVisibility(View.GONE);
                lytAlert.startAnimation(slideDown);
            }
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

    @Override
    public void onCustomerSelected(Customer item) {
        selectCustomer.setText(item.getName());
        dialog.dismiss();

        list_cart.clear();
        cartAdapter.notifyDataSetChanged();
        param_customer_id = item.getName();
        getMenuList(param_customer_id, param_category, param_subcategory, param_label);
    }
}
