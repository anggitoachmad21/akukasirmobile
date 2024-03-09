package id.latenight.creativepos.mobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import id.latenight.creativepos.BuildConfig;
import id.latenight.creativepos.R;
import id.latenight.creativepos.model.PaymentMethod;
import id.latenight.creativepos.adapter.PaymentMethodAdapter;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.MyApplication;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

import static id.latenight.creativepos.util.MyApplication.RC_ENABLE_BLUETOOTH;

public class OrderDetailActivity extends AppCompatActivity implements PaymentMethodAdapter.ImageAdapterListener, View.OnClickListener {

    private List<PaymentMethod> paymentMethodList;
    private ArrayList<String> logisticList;
    private PaymentMethodAdapter paymentMethodAdapter;
    private TableLayout tableLayout, tableLayoutInvoice;
    private StringRequest stringRequest;
    private RequestQueue requestQueue;
    private String order_id, header_invoice, body_invoice;
    private NumberFormat formatRupiah;
    private TextView totalOrder, type, customer, table, waiter, billNo, subtotal, subtotalAfterDiscount, discount, serviceCharge, totalPayable, ppn, notes, ppnPercent, valueLogistic, valueFeeMarketpkace, custNotes, alreadyPaid, remainingPayment, valueShippingPrice, totalShippingPrice;
    private int params_sale_id, params_subtotal, params_subtotal_after_discount, params_paid_amount, params_due_amount, params_given_amount, params_payment_method_type, params_change_amount, params_total_payable, params_shipping_price;
    private float params_ppn;
    private EditText givenAmount, changeAmount, feeMarketplace, shippingPrice;
    private SessionManager sessionManager;
    private ImageView icLogo;

    private ProgressDialog loadingDialog;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private Animation slideUp;
    private LinearLayout lytPayment, lyt_notes, lytRemaining, lytShareInvoice;
    private Spinner spinnerLogistic;

    private RelativeLayout lytPrinterNotification;
    private TextView notifPrinter;
    private Button btnPairPrinter, shareInvoice;
    private DatabaseHandler db;
    private String order_status;

    private ImageView imgInvoice, logoInvoice;
    private TextView headerInvoice, bodyInvoice;
    private LinearLayout lytInvoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Bundle extras = getIntent().getExtras();

        sessionManager = new SessionManager(this);
        String printerAddress = sessionManager.getPrinter();
        //Log.e("Printer", printerAddress);

        icLogo = findViewById(R.id.ic_logo);
        getLogo();

        logisticList = new ArrayList<>();

        formatRupiah = NumberFormat.getInstance();

        tableLayout = findViewById(R.id.tableLayout);
        tableLayoutInvoice = findViewById(R.id.tableLayoutInvoice);

        lytPayment = findViewById(R.id.lyt_payment);
        lytPrinterNotification = findViewById(R.id.lyt_printer_notification);
        notifPrinter = findViewById(R.id.notif_printer);
        btnPairPrinter = findViewById(R.id.btn_pair_printer);
        spinnerLogistic = findViewById(R.id.logistic);
        lytShareInvoice = findViewById(R.id.lyt_share_invoice);
        shareInvoice = findViewById(R.id.share_invoice);
        shareInvoice.setOnClickListener(this);

        RecyclerView recyclerPaymentMethod = findViewById(R.id.recycler_payment_method);
        recyclerPaymentMethod.setLayoutManager(new GridLayoutManager(this, 3));
        paymentMethodList = new ArrayList<>();
        paymentMethodAdapter = new PaymentMethodAdapter(paymentMethodList, getApplicationContext(), this);
        recyclerPaymentMethod.setAdapter(paymentMethodAdapter);

        valueLogistic = findViewById(R.id.value_logistic);
        valueFeeMarketpkace = findViewById(R.id.value_fee_marketplace);
        valueShippingPrice = findViewById(R.id.value_shipping_price);
        billNo = findViewById(R.id.bill_no);
        type = findViewById(R.id.type);
        customer = findViewById(R.id.customer);
        table = findViewById(R.id.table);
        waiter = findViewById(R.id.waiter);
        subtotal = findViewById(R.id.subtotal);
        subtotalAfterDiscount = findViewById(R.id.subtotal_after_discount);
        ppn = findViewById(R.id.ppn);
        ppnPercent = findViewById(R.id.ppn_percent);
        discount = findViewById(R.id.discount);
        totalShippingPrice = findViewById(R.id.total_shipping_price);
        serviceCharge = findViewById(R.id.service_charge);
        totalPayable = findViewById(R.id.total_payable);
        totalOrder = findViewById(R.id.total_order);
        givenAmount = findViewById(R.id.given_amount);
        changeAmount = findViewById(R.id.change_amount);
        feeMarketplace = findViewById(R.id.fee_marketplace);
        shippingPrice = findViewById(R.id.shipping_price);
        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);
        lyt_notes = findViewById(R.id.lyt_notes);
        notes = findViewById(R.id.notes);
        custNotes = findViewById(R.id.cust_notes);
        lytRemaining = findViewById(R.id.lyt_remaining);
        alreadyPaid = findViewById(R.id.already_paid);
        remainingPayment = findViewById(R.id.remaining_payment);

        ImageButton btnCloseAlert = findViewById(R.id.btn_close_alert);
        btnCloseAlert.setOnClickListener(this);
        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Button print_bill = findViewById(R.id.print_bill);

        print_bill.setOnClickListener(this);

        db = new DatabaseHandler(this);

        if (extras != null) {
            order_id = extras.getString("order_id");
            //Log.e("DATA:", db.getSales(Integer.valueOf(order_id)).getData());
            getOderCartList(order_id);
        }
        getPaymentMethods();
        getLogistics();

        int isConnected = MyApplication.getApplication().isConnected();
        if(!sessionManager.getPrinter().isEmpty()) {
            if(isConnected != 3) {
                lytPrinterNotification.setVisibility(View.GONE);
                notifPrinter.setText(getResources().getString(R.string.unable_connect_with_printer));
                btnPairPrinter.setVisibility(View.GONE);
            } else {
                lytPrinterNotification.setVisibility(View.GONE);
            }
        }
        params_change_amount = 0;
    }

    public void scanPrinter(View view) {
        startActivity(new Intent(this, AdminActivity.class));
    }

    @SuppressLint("SetTextI18n")
    private void getOderCartList(String order_id) {
        Log.e("URL_", URI.API_DETAIL_ORDER(sessionManager.getPathUrl())+order_id);
        stringRequest = new StringRequest(URI.API_DETAIL_ORDER(sessionManager.getPathUrl())+order_id, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                Log.e("RESPONSE", jsonObject.toString());

                String orderType = "";
                if(jsonObject.getString("order_type").equals("1")) {
                    orderType = getApplicationContext().getResources().getString(R.string.dine_in);;
                } else if(jsonObject.getString("order_type").equals("2")) {
                    orderType = getApplicationContext().getResources().getString(R.string.take_away);;
                } else if(jsonObject.getString("order_type").equals("3")) {
                    orderType = getApplicationContext().getResources().getString(R.string.delivery);;
                } else {
                    orderType = getApplicationContext().getResources().getString(R.string.taken);
                }

                params_sale_id = jsonObject.getInt("sale_no");
                params_subtotal = jsonObject.getInt("sub_total");
                params_subtotal_after_discount = jsonObject.getInt("sub_total_with_discount");
                params_total_payable = jsonObject.getInt("total_payable");
                order_status = jsonObject.getString("order_status");
                params_shipping_price = jsonObject.getInt("logistic");
                if(order_status.equals("3")){
                    lytPayment.setVisibility(View.GONE);
                    feeMarketplace.setVisibility(View.GONE);
                    shippingPrice.setVisibility(View.GONE);
                    spinnerLogistic.setVisibility(View.GONE);
                    shippingPrice.setVisibility(View.GONE);
                    valueLogistic.setVisibility(View.VISIBLE);
                    valueFeeMarketpkace.setVisibility(View.VISIBLE);
                    valueShippingPrice.setVisibility(View.VISIBLE);
                    lytShareInvoice.setVisibility(View.VISIBLE);
                }
                if(order_status.equals("2")) {
                    spinnerLogistic.setVisibility(View.GONE);
                    valueLogistic.setVisibility(View.VISIBLE);
                    valueShippingPrice.setVisibility(View.VISIBLE);
                    shippingPrice.setVisibility(View.GONE);
                    lytRemaining.setVisibility(View.VISIBLE);
                    alreadyPaid.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("paid_amount")).replace(',', '.'));
                    remainingPayment.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("due_amount")).replace(',', '.'));
                    lytShareInvoice.setVisibility(View.GONE);
                }

                billNo.setText(getResources().getString(R.string.bill_no)+" "+jsonObject.getString("sale_no"));
                type.setText(orderType);
                customer.setText(jsonObject.getString("customer_name"));
                table.setText(jsonObject.getString("sale_no"));
                waiter.setText(jsonObject.getString("full_name"));
                customer.setText(jsonObject.getString("customer_name"));
                if(!jsonObject.getString("cust_notes").isEmpty()) {
                    custNotes.setText(jsonObject.getString("cust_notes"));
                } else {
                    custNotes.setVisibility(View.GONE);
                }
                totalOrder.setText(jsonObject.getString("total_items") + " Items");
                //Log.e("String", jsonObject.getString("total_payable"));
                valueFeeMarketpkace.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("fee_marketplace")).replace(',', '.'));
                valueLogistic.setText(jsonObject.getString("logistic_name"));
                valueShippingPrice.setText(String.valueOf(params_shipping_price));
                subtotal.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(params_subtotal).replace(',', '.'));
                subtotalAfterDiscount.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(params_subtotal_after_discount + params_shipping_price).replace(',', '.'));
                String discount_value = jsonObject.getString("sub_total_discount_value").replace("%", "");
                discount.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(Integer.valueOf(discount_value)).replace(',', '.'));
                totalShippingPrice.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("logistic")).replace(',', '.'));
                serviceCharge.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("fee_marketplace")).replace(',', '.'));
                totalPayable.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("total_payable")).replace(',', '.'));
                ppn.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("vat")).replace(',', '.'));
                ppnPercent.setText(10+"%");
                params_ppn = jsonObject.getInt("vat");
                header_invoice = jsonObject.getString("header_invoice");
                body_invoice = jsonObject.getString("invoice");
                Log.e("BODY INVOICE : ", jsonObject.getString("invoice"));
                JSONArray jsonTable = jsonObject.getJSONArray("tables_booked");
                StringBuilder tableName = new StringBuilder();
                String comma = "";
                for(int i=0; i < jsonTable.length(); i++) {
                    JSONObject jsonobjectTable = jsonTable.getJSONObject(i);
                    if(i != jsonTable.length() -1) {
                        comma = ",";
                    }
                    tableName.append(jsonobjectTable.getString("table_name")).append(comma);
                }

                givenAmount.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().isEmpty()) {
                            int value_change_amount;
                            try {
                                value_change_amount = Integer.parseInt(s.toString()) - jsonObject.getInt("due_amount");
                                params_change_amount = value_change_amount;
                                params_due_amount = value_change_amount;
                                if(Integer.parseInt(s.toString()) < jsonObject.getInt("due_amount")) {
                                    changeAmount.setText(getResources().getString(R.string.currency) + " 0");
                                } else {
                                    params_due_amount = 0;
                                    changeAmount.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(value_change_amount).replace(',', '.'));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                table.setText(tableName.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
//                    Log.e("tables: ", jo.toString());
                    @SuppressLint("InflateParams") View tableRow = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_order,null,false);
                    TextView history_display_no  = tableRow.findViewById(R.id.menu_name);
                    TextView history_display_date  = tableRow.findViewById(R.id.menu_price);
                    TextView history_display_orderid  = tableRow.findViewById(R.id.menu_qty);
                    TextView history_display_quantity  = tableRow.findViewById(R.id.menu_total);
                    TextView history_display_category  = tableRow.findViewById(R.id.category_label);

                    String menu_name = jo.getString("menu_name");
                    int menu_price = jo.getInt("menu_unit_price");
                    int menu_qty = jo.getInt("qty");
                    int menu_total = menu_price * menu_qty;

                    history_display_no.setText(Html.fromHtml(menu_name));
                    history_display_date.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(menu_price).replace(',', '.'));
                    history_display_orderid.setText(String.valueOf(menu_qty));
                    history_display_quantity.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(menu_total).replace(',', '.'));
                    if(jo.getString("label_name") != null) {
                        String category_and_label = "( " + jo.getString("category_name") + " - " + jo.getString("label_name") + " )";
                        history_display_category.setText(category_and_label);
                    }
                    else {
                        String category_and_label = "( " + jo.getString("category_name") + " )";
                        history_display_category.setText(category_and_label);
                    }
                    tableLayout.addView(tableRow);
                }

                if(order_status.equals("1")){
                    getPPN();
                }

                shareInvoice(jsonObject.getString("sale_no"), jsonObject.getString("date_invoice"), jsonObject.getString("name_cashier"), jsonArray, jsonObject.getInt("sub_total"), jsonObject.getInt("total_discount_amount"), jsonObject.getInt("given_amount"), jsonObject.getInt("change_amount"), jsonObject.getString("cust_notes"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);

        /*Log.e("UPDATE", URI.API_DETAIL_ORDER+order_id);
        stringRequest = new StringRequest(URI.API_DETAIL_ORDER+order_id, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                //Log.e("RESPONSE", jsonArray.toString());

                String orderType = "";
                if(jsonObject.getString("order_type").equals("1")) {
                    orderType = "Dine in";
                } else if(jsonObject.getString("order_type").equals("2")) {
                    orderType = "Take away";
                } else if(jsonObject.getString("order_type").equals("3")) {
                    orderType = "Delivery";
                }

                String order_status = jsonObject.getString("order_status");
                if(!order_status.equals("1")){
                    lytPayment.setVisibility(View.GONE);
                }

                params_sale_id = jsonObject.getInt("sale_no");
                params_subtotal = jsonObject.getInt("sub_total");
                params_subtotal_after_discount = jsonObject.getInt("sub_total_with_discount");

                billNo.setText(getResources().getString(R.string.bill_no)+" "+jsonObject.getString("sale_no"));
                type.setText(orderType);
                customer.setText(jsonObject.getString("customer_name"));
                table.setText(jsonObject.getString("sale_no"));
                waiter.setText(jsonObject.getString("user_name"));
                customer.setText(jsonObject.getString("customer_name"));
                totalOrder.setText(jsonObject.getString("total_items") + " Items");
                subtotal.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("sub_total")).replace(',', '.'));
                subtotalAfterDiscount.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("sub_total_with_discount")).replace(',', '.'));
                discount.setText(jsonObject.getString("sub_total_discount_value"));
                totalDiscount.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("total_discount_amount")).replace(',', '.'));
                serviceCharge.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("delivery_charge")).replace(',', '.'));
                totalPayable.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(jsonObject.getInt("total_payable")).replace(',', '.'));

                JSONArray jsonTable = jsonObject.getJSONArray("tables_booked");
                StringBuilder tableName = new StringBuilder();
                String comma = "";
                for(int i=0; i < jsonTable.length(); i++) {
                    JSONObject jsonobjectTable = jsonTable.getJSONObject(i);
                    if(i != jsonTable.length() -1) {
                        comma = ",";
                    }
                    tableName.append(jsonobjectTable.getString("table_name")).append(comma);
                }

                givenAmount.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().isEmpty()) {
                            int value_change_amount;
                            try {
                                value_change_amount = Integer.parseInt(s.toString()) - jsonObject.getInt("total_payable");
                                params_change_amount = value_change_amount;
                                changeAmount.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(value_change_amount).replace(',', '.'));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                table.setText(tableName.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    @SuppressLint("InflateParams") View tableRow = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_order,null,false);
                    TextView history_display_no  = tableRow.findViewById(R.id.menu_name);
                    TextView history_display_date  = tableRow.findViewById(R.id.menu_price);
                    TextView history_display_orderid  = tableRow.findViewById(R.id.menu_qty);
                    TextView history_display_quantity  = tableRow.findViewById(R.id.menu_total);

                    String menu_name = jo.getString("menu_name");
                    int menu_price = jo.getInt("menu_unit_price");
                    int menu_qty = jo.getInt("qty");
                    int menu_total = menu_price * menu_qty;

                    history_display_no.setText(menu_name);
                    history_display_date.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(menu_price).replace(',', '.'));
                    history_display_orderid.setText(String.valueOf(menu_qty));
                    history_display_quantity.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(menu_total).replace(',', '.'));
                    tableLayout.addView(tableRow);
                }

                getPPN();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);
        */
    }

    private void getLogistics() {
        Log.e("URL_", URI.API_LOGISTIC(sessionManager.getPathUrl())+sessionManager.getId());
        JsonArrayRequest request = new JsonArrayRequest(URI.API_LOGISTIC(sessionManager.getPathUrl())+sessionManager.getId(), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++) {
                try {
                    jsonObject = response.getJSONObject(i);
                    logisticList.add(jsonObject.getString("company_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerLogistic.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.logistic_item, logisticList));
            //Log.e("Payment ", String.valueOf(paymentMethodList.get(0).getName()));
        }, error -> {
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getPaymentMethods() {
        Log.e("URL_", URI.API_PAYMENT_METHOD(sessionManager.getPathUrl())+"/"+sessionManager.getId());
        //Log.e("Response", response.toString());
        JsonArrayRequest request = new JsonArrayRequest(URI.API_PAYMENT_METHOD(sessionManager.getPathUrl())+"/"+sessionManager.getId(), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++) {
                try {
                    jsonObject = response.getJSONObject(i);
                    PaymentMethod listData = new PaymentMethod(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("description"));
                    paymentMethodList.add(listData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            paymentMethodAdapter.notifyDataSetChanged();
            //params_payment_method_type = 3;

            if(!paymentMethodList.get(0).getName().equals("TUNAI")) {
                lyt_notes.setVisibility(View.VISIBLE);
                givenAmount.setText(String.valueOf(params_total_payable));
                changeAmount.setText("Rp. 0");
            } else {
                lyt_notes.setVisibility(View.GONE);
            }
            params_payment_method_type = paymentMethodList.get(0).getId();
            //Log.e("Payment ", String.valueOf(paymentMethodList.get(0).getName()));
        }, error -> {
        });
        requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(request);
    }

    @Override
    public void onImageSelected(PaymentMethod item) {
        if(!item.getName().equals("TUNAI")) {
            lyt_notes.setVisibility(View.VISIBLE);
            givenAmount.setText(String.valueOf(params_total_payable));
            changeAmount.setText("Rp. 0");
            params_change_amount = 0;
        } else {
            givenAmount.setText("");
            changeAmount.setText("");
            lyt_notes.setVisibility(View.GONE);
        }
        //Log.e("test", item.getName());
        params_payment_method_type = item.getId();
    }

    @SuppressLint("SetTextI18n")
    public void duaRibu(View view) {
        givenAmount.requestFocus();
        if(givenAmount.getText().toString().isEmpty()){
            givenAmount.setText("0");
        }
        int uang = Integer.parseInt(givenAmount.getText().toString());
        uang = uang + 2000;
        givenAmount.setText(String.valueOf(uang));
    }
    @SuppressLint("SetTextI18n")
    public void limaRibu(View view) {
        givenAmount.requestFocus();
        if(givenAmount.getText().toString().isEmpty()){
            givenAmount.setText("0");
        }
        int uang = Integer.parseInt(givenAmount.getText().toString());
        uang = uang + 5000;
        givenAmount.setText(String.valueOf(uang));
    }
    @SuppressLint("SetTextI18n")
    public void sepuluhRibu(View view) {
        givenAmount.requestFocus();
        if(givenAmount.getText().toString().isEmpty()){
            givenAmount.setText("0");
        }
        int uang = Integer.parseInt(givenAmount.getText().toString());
        uang = uang + 10000;
        givenAmount.setText(String.valueOf(uang));
    }
    @SuppressLint("SetTextI18n")
    public void duapuluhRibu(View view) {
        givenAmount.requestFocus();
        if(givenAmount.getText().toString().isEmpty()){
            givenAmount.setText("0");
        }
        int uang = Integer.parseInt(givenAmount.getText().toString());
        uang = uang + 20000;
        givenAmount.setText(String.valueOf(uang));
    }
    @SuppressLint("SetTextI18n")
    public void limapuluhRibu(View view) {
        givenAmount.requestFocus();
        if(givenAmount.getText().toString().isEmpty()){
            givenAmount.setText("0");
        }
        int uang = Integer.parseInt(givenAmount.getText().toString());
        uang = uang + 50000;
        givenAmount.setText(String.valueOf(uang));
    }
    @SuppressLint("SetTextI18n")
    public void seratusRibu(View view) {
        givenAmount.requestFocus();
        if(givenAmount.getText().toString().isEmpty()){
            givenAmount.setText("0");
        }
        int uang = Integer.parseInt(givenAmount.getText().toString());
        uang = uang + 100000;
        givenAmount.setText(String.valueOf(uang));
    }

    public void printText(String header_invoice, String invoice) {
        if(sessionManager.getEnablePrinter().equals("on")) {
            String header = header_invoice;
            String body = invoice;
            MyApplication.getApplication().sendMessage(header, body);
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
        }
    }

    public void printTextFinish(String header_invoice, JSONObject invoice, JSONArray menus)
    {
        if(sessionManager.getEnablePrinter().equals("on")) {
            String header = header_invoice;
            String top = "", body = "", footer= "", bottom_footer = "", bodys = "";
            List<String> mStrings = new ArrayList<String>();
            try {
                top =
                        "[C]"+invoice.getString("name_outlet")+"\n"+
                        "[C]"+invoice.getString("alamat")+"\n"+
                        "[C]Telp. "+invoice.getString("telepon")+"\n"+
                        "[C]--------------------------------\n" +
                        "[L]No. Invoice[R]" + invoice.getString("no_invoice") + "\n" +
                        "[L]Tanggal[R]" + invoice.getString("tanggal") + "\n" +
                        "[L]Nama Kasir[R]" + invoice.getString("name_kasir") + "\n" +
                        "[C]--------------------------------\n" +
                        "[C]\n";

                for (int i= 0; i<menus.length(); i++) {
                    JSONObject menu = menus.getJSONObject(i);
                    String cutting = menu.getString("menu_price_per_unit").replace("Rp. ", "");
                    mStrings.add(
                            "{L}<font size='normal'>"+menu.getString("menu_name")+"</font>\n\n"+
                            "{L}"+menu.getString("qty")+" X "+ cutting.replace(",", "") +"{R}" + menu.getString("menu_price_per_unit") + "\n \n");
                }
                if(invoice.getInt("status_paid") == 1) {
                    footer =
                            "[C]--------------------------------\n" +
                            "[L]Harga[R]" + invoice.getString("harga") + "\n \n" +
                            "[L]Diskon[R]" + invoice.getString("diskon") + "\n \n" +
                            "[L]Ongkir[R]" + invoice.getString("ongkir") + "\n";
                }
                else if(invoice.getInt("status_paid") == 2)
                {
                    footer =
                            "[C]--------------------------------\n" +
                            "[L]Harga[R]" + invoice.getString("harga") + "\n \n" +
                            "[L]Ongkir[R]" + invoice.getString("ongkir") + "\n";
                }
                else if(invoice.getInt("status_paid") == 3)
                {
                    footer =
                            "[C]--------------------------------\n" +
                            "[L]Harga[R]" + invoice.getString("harga") + "\n \n" +
                            "[L]Diskon[R]" + invoice.getString("diskon") + "\n \n";
                }
                else{
                    footer =
                            "[C]--------------------------------\n" +
                            "[L]Harga[R]" + invoice.getString("harga") + "\n";
                }

                bottom_footer =
                        "[C]--------------------------------\n" +
                        "[L]Pembayaran[R]" + invoice.getString("pembayaran") + "\n \n" +
                        "[L]Kembalian[R]" + invoice.getString("kembalian") + "\n \n" +
                        "[L]Catatan :[R]" + invoice.getString("catatan") + "\n \n \n"+
                        "[L]"+invoice.getString("invoice_footer")+"\n \n"+
                        "[L]"+invoice.getString("bottom_footer")+"\n";

                String obj1 = String.valueOf(mStrings).replace("[", "");
                String obj2 = obj1.replace("]", "");
                String obj3 = obj2.replace(", ", "");
                String obj4 = obj3.replace("{C}", "[C]");
                String obj5 = obj4.replace("{L}", "[L]");
                String obj6 = obj5.replace("{R}", "[R]");

                bodys = top + obj6 + footer + bottom_footer;
                try {
                    MyApplication.getApplication().printNew(bodys);
                }
                catch (Exception e)
                {
                    Toast.makeText(this, "No printer was connected!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
        }
    }

    public void printTextNew(String header_invoice, JSONObject invoice, JSONArray menus) {
        if(sessionManager.getEnablePrinter().equals("on")) {
            String header = header_invoice;
            String top = "", body = "", footer= "", bottom_footer = "", bodys = "";
            List<String> mStrings = new ArrayList<String>();
            try {
                top =
                        "[L]Tanggal[R]" + invoice.getString("tanggal") + "\n" +
                        "[C]--------------------------------\n" +
                        "[C]\n";

            for (int i= 0; i<menus.length(); i++) {
                JSONObject menu = menus.getJSONObject(i);
                 String cutting = menu.getString("menu_price_per_unit").replace("Rp. ", "");
                 mStrings.add(
                        "{L}<font size='normal'>"+menu.getString("menu_name")+"</font>\n \n" +
                        "{L}"+menu.getString("qty")+" X "+ cutting.replace(",", "") +"{R}" + menu.getString("menu_price_per_unit") + "\n \n");
            }
            if(invoice.getInt("status_menus") == 1) {
                footer =
                        "[C]--------------------------------\n" +
                        "[L]Harga[R]" + invoice.getString("harga") + "\n" +
                        "[C]--------------------------------\n" +
                        "[L]Diskon[R]" + invoice.getString("diskon") + "\n" +
                        "[C]--------------------------------\n" +
                        "[L]Ongkir[R]" + invoice.getString("ongkir") + "\n";
            }
            else if(invoice.getInt("status_menus") == 2)
            {
                footer =
                        "[C]--------------------------------\n" +
                        "[L]Harga[R]" + invoice.getString("harga") + "\n" +
                        "[C]--------------------------------\n" +
                        "[L]Ongkir[R]" + invoice.getString("ongkir") + "\n";
            }
            else if(invoice.getInt("status_menus") == 3)
            {
                footer =
                        "[C]--------------------------------\n" +
                        "[L]Harga[R]" + invoice.getString("harga") + "\n";
            }
            else{
                footer =
                        "[C]--------------------------------\n" +
                        "[L]Harga[R]" + invoice.getString("harga") + "\n";
            }

            bottom_footer =
                    "[C]-------------"+ invoice.getString("status_paid") +"-------------\n";

            String obj1 = String.valueOf(mStrings).replace("[", "");
            String obj2 = obj1.replace("]", "");
            String obj3 = obj2.replace(", ", "");
            String obj4 = obj3.replace("{C}", "[C]");
            String obj5 = obj4.replace("{L}", "[L]");
            String obj6 = obj5.replace("{R}", "[R]");

            bodys = top + obj6 + footer + bottom_footer;
            MyApplication.getApplication().printNew(bodys);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
        }
    }

    public void updateOrder(View view) {
        //Log.e("test", params_sale_id);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("update_order", true);
        intent.putExtra("order_id", order_id);
        startActivity(intent);
        finish();
    }

    public void splitBill(View view) {
        //Log.e("test", params_sale_id);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("split_bill", true);
        intent.putExtra("order_id", order_id);
        startActivity(intent);
        finish();
    }

    private void finishOrder() {
//        Log.e("user_id", sessionManager.getId());
//        Log.e("sale_id", String.valueOf(params_sale_id));
//        Log.e("close_order", "true");
//        Log.e("paid_amount", String.valueOf(params_total_payable));
//        Log.e("due_amount", "0");
//        Log.e("given_amount", String.valueOf(params_given_amount));
//        Log.e("change_amount", String.valueOf(params_change_amount));
//        Log.e("payment_method_type", String.valueOf(params_payment_method_type));

        int params_given_amount = Integer.parseInt(givenAmount.getText().toString());
        //if(params_given_amount > 0 && params_change_amount >= 0) {
        //if(params_given_amount >= params_total_payable) {
        showLoading();
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_FINISH_ORDER(sessionManager.getPathUrl()),
                response -> {
                    Log.e("RESPONSE ", response);
                    try {
                        hideLoading();
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject jsonObject1 = new JSONObject(jsonObject.getString("body_invoice"));
                        boolean success = jsonObject.getBoolean("success");
                        if(success) {
                            //Log.e("Enable", sessionManager.getEnablePrinter());
                            if(sessionManager.getEnablePrinter().equals("on")) {
                                printTextFinish(jsonObject.getString("header_invoice"), jsonObject1, jsonObject1.getJSONArray("menus"));
//                                printText(jsonObject.getString("header_invoice"), jsonObject.getString("invoice"));
                            }
                            db.updateNote(Integer.parseInt(order_id), jsonObject.getString("sales_information"));
                            //db.deleteSales(Integer.parseInt(order_id));
                            Intent intent = new Intent(getApplicationContext(), OrderHistoryActivity.class);
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
                String logistic_value = spinnerLogistic.getSelectedItem().toString();
                Log.e("shipping_price", String.valueOf(shippingPrice.getText()));
                params.put("fee_marketplace", String.valueOf(feeMarketplace.getText()));
                params.put("shipping_price", String.valueOf(params_shipping_price));
                params.put("logistic", logistic_value);
                params.put("user_id", sessionManager.getId());
                params.put("sale_id", String.valueOf(params_sale_id));
                params.put("close_order", "true");
                params.put("total_payable", String.valueOf(params_total_payable));
                params.put("due_amount", String.valueOf(params_due_amount));
                params.put("given_amount", String.valueOf(params_given_amount));
                params.put("change_amount", String.valueOf(params_change_amount));
                params.put("payment_method_type", String.valueOf(params_payment_method_type));
                params.put("notes", String.valueOf(notes.getText()));
                params.put("payment_status", "0");
                params.put("ppn", String.valueOf(params_ppn));
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
        //} else {
        //Toast.makeText(this, "The payment is lacking", Toast.LENGTH_SHORT).show();
        //}
        //} else {
        //Toast.makeText(this, "The payment is lacking", Toast.LENGTH_SHORT).show();
        //}
    }

    public void invoiceDirect(View view) {
        //Log.e("sale_id", String.valueOf(params_sale_id));
        //Log.e("close_order", "true");
        //Log.e("paid_amount", String.valueOf(params_total_payable));
        //Log.e("due_amount", "0");
        //Log.e("given_amount", String.valueOf(params_given_amount));
        //Log.e("change_amount", String.valueOf(params_change_amount));
        //Log.e("payment_method_type", String.valueOf(params_payment_method_type));

        showLoading();
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_INVOICE(sessionManager.getPathUrl()),
                response -> {
                    Log.e("RESPONSE ", response);
                    try {
                        hideLoading();
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject jsonObject1 = new JSONObject(jsonObject.getString("body_invoice"));
                        boolean success = jsonObject.getBoolean("success");
                        if(success) {
//                            if(sessionManager.getEnablePrinter().equals("on")) {
                                printTextNew(jsonObject.getString("header_invoice"), jsonObject1, jsonObject1.getJSONArray("menus"));
//                                printText(jsonObject.getString("header_invoice"), jsonObject.getString("invoice"));
//                            }
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

                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        String jsonError = new String(networkResponse.data);
                        //Print Error!
                        //Log.e("Error", jsonError);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("user_id", sessionManager.getId());
                params.put("sale_id", String.valueOf(params_sale_id));
                params.put("close_order", "true");
                params.put("paid_amount", String.valueOf(params_total_payable));
                params.put("due_amount", String.valueOf(params_due_amount));
                params.put("given_amount", String.valueOf(params_given_amount));
                params.put("change_amount", String.valueOf(params_change_amount));
                params.put("payment_method_type", String.valueOf(params_payment_method_type));
                params.put("notes", String.valueOf(notes.getText()));
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    @SuppressLint("SetTextI18n")
    private void getPPN() {
        //Log.e("URL_", URI.API_PPN);
        stringRequest = new StringRequest(URI.API_PPN(sessionManager.getPathUrl()), response -> {

            float value_ppn = ((params_subtotal_after_discount * Float.parseFloat(response)) / 100);
            //Log.e("PPN", String.valueOf(value_ppn));
            int payable_after_ppn = (int) (params_subtotal_after_discount + value_ppn);
            int payable_after_fee;

            shippingPrice.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int value_shipping_price = 0;
                    if(!s.toString().isEmpty()) {
                        value_shipping_price = Integer.valueOf(s.toString());
                    }

                    int subtotal_after_fee = params_subtotal + value_shipping_price;
                    int subtotal_after_discount_fee = params_subtotal_after_discount + value_shipping_price;
                    subtotal.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(subtotal_after_fee).replace(',', '.'));
                    subtotalAfterDiscount.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(subtotal_after_discount_fee).replace(',', '.'));
                    totalShippingPrice.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(value_shipping_price).replace(',', '.'));
                    params_shipping_price = value_shipping_price;

                    float value_ppn = ((subtotal_after_discount_fee * Float.parseFloat(response)) / 100);
                    int payable_after_ppn = (int) (subtotal_after_discount_fee + value_ppn);

                    String s_payable_after_ppn = formatRupiah.format(payable_after_ppn).replace(',', '.');
                    String s_value_ppn = formatRupiah.format(value_ppn).replace(',', '.');

                    params_total_payable = payable_after_ppn;
                    params_ppn = value_ppn;
                    ppn.setText(getResources().getString(R.string.currency) +" "+ s_value_ppn);
                    ppnPercent.setText(response+"%");
                    totalPayable.setText(getResources().getString(R.string.currency) +" "+ s_payable_after_ppn);
                }
            });

            givenAmount.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!s.toString().isEmpty()) {
                        Log.e("bayar", String.valueOf(params_total_payable));
                        int value_change_amount = Integer.parseInt(s.toString()) - params_total_payable;
                        params_due_amount = value_change_amount;
                        if(Integer.parseInt(s.toString()) < params_total_payable) {
                            changeAmount.setText(getResources().getString(R.string.currency) + " 0");
                        } else {
                            params_due_amount = 0;
                            changeAmount.setText(getResources().getString(R.string.currency) + " " + formatRupiah.format(value_change_amount).replace(',', '.'));
                        }
                        params_change_amount = value_change_amount;
                    }
                }
            });

            String s_payable_after_ppn = formatRupiah.format(payable_after_ppn).replace(',', '.');
            String s_value_ppn = formatRupiah.format(value_ppn).replace(',', '.');

            params_total_payable = payable_after_ppn;
            params_ppn = value_ppn;
            ppn.setText(getResources().getString(R.string.currency) +" "+ s_value_ppn);
            ppnPercent.setText(response+"%");
            totalPayable.setText(getResources().getString(R.string.currency) +" "+ s_payable_after_ppn);
        }, error -> {
        });
        requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);
    }

    public void showLoading() {
        loadingDialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
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
        //hideLoading();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.print_bill) {
            String checkGivenAmount = givenAmount.getText().toString();

            Log.e("givenamount", checkGivenAmount);
            Log.e("cangeamount", String.valueOf(params_change_amount));
            if(!checkGivenAmount.isEmpty()) {
                //if (Integer.parseInt(checkGivenAmount) >= 0 && params_change_amount >= 0) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.finish_order)
                        .setMessage(R.string.close_this_order)
                        .setIcon(R.drawable.ic_notif)
                        .setPositiveButton(R.string.finish_order, (dialog, whichButton) -> finishOrder())
                        .setNegativeButton(R.string.cancel, null).show();
                //} else {
                //showError("Pembayaran kurang");
                //}
            } else {
                showError("Lengkapi data terlebih dahulu");
            }
        }
        if (v.getId() == R.id.btn_close_alert) {
            lytAlert.setVisibility(View.GONE);
        }
        if (v.getId() == R.id.share_invoice) {
            shareInvoiceToSocialMedia();
        }
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
                sessionManager.setMainLogo(response);
            }
        }, error -> {

        });
        requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);
    }

    public void shareInvoice(String no_invoice, String date_invoice, String name_of_chasier, JSONArray jsonArray, int price, int discounts, int pay, int kembalian, String notes) throws JSONException {
        imgInvoice = findViewById(R.id.invoice_img);
        logoInvoice = findViewById(R.id.logo_invoice);
        headerInvoice = findViewById(R.id.header_invoice);
        bodyInvoice = findViewById(R.id.body_invoice);
        TextView footerInvoice = findViewById(R.id.footer_invoice);
        lytInvoice = findViewById(R.id.lyt_invoice);

        footerInvoice.setText("Terimakasih telah berbelanja di jasmine frozen\n" +
                              "Akukasir by www.akusolusi.com");

        bodyInvoice.setVisibility(View.GONE);

        TextView no_invoices = findViewById(R.id.no_invoice);
        no_invoices.setText(no_invoice);

        TextView date_invoices = findViewById(R.id.date_invoice);
        date_invoices.setText(date_invoice);

        TextView name_chasier = findViewById(R.id.name_of_cashier);
        name_chasier.setText(name_of_chasier);

        TextView note = findViewById(R.id.notes_invoice);
        note.setText(notes);

        TextView price_invoices = findViewById(R.id.price_invoice);
        price_invoices.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(price).replace(',', '.'));

        TextView discount_price = findViewById(R.id.discount_invoice);
        TextView text_discount_invoice = findViewById(R.id.text_discount_invoice);

        if(discounts != 0)
        {
            discount_price.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(discounts).replace(',', '.'));
            discount_price.setVisibility(View.VISIBLE);
            text_discount_invoice.setVisibility(View.VISIBLE);
        }
        else{
            discount_price.setVisibility(View.GONE);
            text_discount_invoice.setVisibility(View.GONE);
        }

        TextView pay_invoice = findViewById(R.id.pay_invoice);
        pay_invoice.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(pay).replace(',', '.'));

        TextView return_invoice = findViewById(R.id.return_invoice);
        return_invoice.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(kembalian).replace(',', '.'));

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            @SuppressLint("InflateParams") View tableRow = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_invoice,null,false);
            TextView history_display_no  = tableRow.findViewById(R.id.menu_name);
            TextView history_display_total  = tableRow.findViewById(R.id.menu_total);
            TextView history_display_price  = tableRow.findViewById(R.id.menu_price);

            String menu_name = jo.getString("menu_name");
            String menu_category = jo.getString("category_name");
            int menu_price = jo.getInt("menu_unit_price");
            int menu_qty = jo.getInt("qty");
            int menu_total = menu_price * menu_qty;

            history_display_no.setText(Html.fromHtml(menu_name) + "(" +Html.fromHtml(menu_category) + " )");
            history_display_price.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(menu_total).replace(',', '.'));
            history_display_total.setText(String.valueOf(menu_qty) + " X " + getResources().getString(R.string.currency) +" "+ formatRupiah.format(menu_price).replace(',', '.'));
            tableLayoutInvoice.addView(tableRow);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            headerInvoice.setText(Html.fromHtml(header_invoice, Html.FROM_HTML_MODE_LEGACY));
        } else {
            headerInvoice.setText(Html.fromHtml(header_invoice));
        }
        bodyInvoice.setText(body_invoice);

        Glide.with(this)
            .load(sessionManager.getMainLogo())
            .centerInside()
            .into(logoInvoice);

        ImageView asdf = findViewById(R.id.viewasdf);
        ImageView asd = findViewById(R.id.viewasd);
        ImageView as= findViewById(R.id.viewas);
        Glide.with(this)
                .load(R.drawable.line_invoice)
                .into(asdf);
        Glide.with(this)
                .load(R.drawable.line_invoice)
                .into(asd);
        Glide.with(this)
                .load(R.drawable.line_invoice)
                .into(as);
    }

    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private void shareInvoiceToSocialMedia() {
        Bitmap image = getBitmapFromView(lytInvoice);
        imgInvoice.setImageBitmap(image);
        imgInvoice.setVisibility(View.GONE);
        showLoading();
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Get access to the URI for the bitmap
            Uri bmpUri = getLocalBitmapUri(imgInvoice);
            if (bmpUri != null) {
                // Construct a ShareIntent with link to image
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/*");
                // Launch sharing dialog for image
                startActivity(Intent.createChooser(shareIntent, "Share Image"));
                hideLoading();
            } else {
                hideLoading();
                // ...sharing failed, handle error
            }
        }, 1000);
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
            bmpUri = FileProvider.getUriForFile(OrderDetailActivity.this, BuildConfig.APPLICATION_ID , file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}