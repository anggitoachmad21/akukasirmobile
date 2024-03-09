package id.latenight.creativepos.mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Order;
import id.latenight.creativepos.adapter.OrderAdapter;
import id.latenight.creativepos.tabs.MenuActivityTabs;
import id.latenight.creativepos.tabs.OrderDetailActivityTabs;
import id.latenight.creativepos.tabs.OrderHistoryActivityTabs;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class OrderRunningActivity extends AppCompatActivity implements OrderAdapter.OrderAdapterListener {

    private List<Order> runningOrderList;
    private OrderAdapter runningOrderAdapter;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private ProgressBar progressBar;
    private NumberFormat formatRupiah;
    private TextView totalSalesToday;
    private DatabaseHandler db;
    private String currentData;
    private SessionManager sessionManager;
    private EditText startDate, endDate;
    private String param_start_date="0", param_end_date = "0", param_outlet_id="0";

    private boolean is_tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_running);

        db = new DatabaseHandler(this);
        sessionManager = new SessionManager(this);

        formatRupiah = NumberFormat.getInstance();

        progressBar = findViewById(R.id.progressBar);
        totalSalesToday = findViewById(R.id.total_sales_today);

        RecyclerView recyclerRunningOrder = findViewById(R.id.recycler_running_order);
        recyclerRunningOrder.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerRunningOrder.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        runningOrderList = new ArrayList<>();
        runningOrderAdapter = new OrderAdapter(runningOrderList, getApplicationContext(), this);
        recyclerRunningOrder.setAdapter(runningOrderAdapter);

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
                runningOrderAdapter.getFilter().filter(query);
                return false;
            }
        });

        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select date");
        MaterialDatePicker<Pair<Long, Long>> pickerRange = materialDateBuilder.build();

        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        startDate.setOnClickListener(v -> pickerRange.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        endDate.setOnClickListener(v -> pickerRange.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));

        pickerRange.addOnPositiveButtonClickListener(selection -> {
            Pair selectedDates = pickerRange.getSelection();
            final Pair<Date, Date> rangeDate = new Pair<>(new Date((Long) selectedDates.first), new Date((Long) selectedDates.second));
            Date start_date = rangeDate.first;
            Date end_date = rangeDate.second;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleFormat = new SimpleDateFormat("dd-MM-yyyy");
            startDate.setText(simpleFormat.format(start_date));
            endDate.setText(simpleFormat.format(end_date));

            param_start_date = (simpleFormat.format(start_date));
            param_end_date = (simpleFormat.format(end_date));
            downloadRunningOrder();
        });
        //db.truncate();
//        Date d = Calendar.getInstance().getTime();
//        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        currentData = formatter.format(d);

        if(sessionManager.getMobileType().equals("mobile")){
            is_tabs = false;
        }
        else{
            is_tabs = true;
        }
        downloadRunningOrder();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOrderSelected(Order item, String click) {
        if(click == "Long")
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setNegativeButton("Keluar", null);
            alertDialog.setPositiveButton("Batalkan", (dialog, whichButton) -> deleteSales(item.getSaleNo(), runningOrderList.indexOf(item)));
            alertDialog.setNeutralButton("Proses", (dialog, whichButton) -> prosesOrder(item.getSaleNo()));
            alertDialog.setTitle("Batalkan Pesanan");
            alertDialog.setMessage("Ingin Membatalkan Pesanan Ini ?");
            AlertDialog orderConfirm = alertDialog.show();
            orderConfirm.show();
            return;
        }
        if(is_tabs == false) {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", item.getSaleNo());
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, OrderDetailActivityTabs.class);
            intent.putExtra("order_id", item.getSaleNo());
            startActivity(intent);
        }
    }

    private void prosesOrder(String item)
    {
        if(is_tabs == false) {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", item);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, OrderDetailActivityTabs.class);
            intent.putExtra("order_id", item);
            startActivity(intent);
        }
    }

    private void deleteSales(String sale_id, int position)
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_sales)
                .setMessage(getResources().getString(R.string.are_you_sure))
                .setIcon(R.drawable.ic_notif)
                .setPositiveButton(R.string.delete_sales, (dialog, whichButton) -> deleteSale(sale_id, position))
                .setNegativeButton(R.string.cancel, null).show();
    }


    private void deleteSale(String sale_id, int position) {
        //Log.e("PARAM", sale_id);
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_DELETE_SALES(sessionManager.getPathUrl()),
                response -> {
                    Log.e("RESPONSE ", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if(success) {
                            runningOrderList.remove(position);
                            runningOrderAdapter.notifyItemRemoved(position);
                        } else {
                            Log.e("deleteSales: ", "Terjadi Kesalahan Server");
                        }

                    } catch (JSONException e) {

                        Log.e("deleteSales: ", "Terjadi Kesalahan Server");
                    }
                },
                error -> {
                    error.printStackTrace();
                    Log.e("deleteSales: ", "Terjadi Kesalahan Server");
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

                params.put("sale_id", sale_id);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }
    private void downloadRunningOrder() {
        Log.e("URL_", URI.API_NEW_ORDER(sessionManager.getPathUrl())+'/'+sessionManager.getId()+"?start_date="+param_start_date+"&end_date="+param_end_date);
        progressBar.setVisibility(View.VISIBLE);
        runningOrderList.clear();
        request = new JsonArrayRequest(URI.API_NEW_ORDER(sessionManager.getPathUrl())+'/'+sessionManager.getId()+"?start_date="+param_start_date+"&end_date="+param_end_date, response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            db.truncate();
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    Order listData = new Order(jsonObject.getString("id"), jsonObject.getString("sale_no"), jsonObject.getString("total_payable"), jsonObject.getString("sale_date"), jsonObject.getString("order_time"), jsonObject.getString("order_type"), jsonObject.getString("customer_name"), jsonObject.getString("order_status"), jsonObject.getString("tables_booked"), jsonObject.getString("sub_total"), jsonObject.getString("sub_total_discount_value"), jsonObject.getString("total_discount_amount"), jsonObject.getString("logistic"));
                    runningOrderList.add(listData);
                    db.addSales(jsonObject.getString("sale_no"), String.valueOf(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            runningOrderAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }, error -> progressBar.setVisibility(View.GONE));
        requestQueue= Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(request);
    }

    public void historyOrderList(View view) {
        if(is_tabs == false) {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, OrderHistoryActivityTabs.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed()
    {
        if(is_tabs == false) {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(this, MenuActivityTabs.class);
            startActivity(intent);
            finish();
        }
    }
}
