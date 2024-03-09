package id.latenight.creativepos.mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Order;
import id.latenight.creativepos.adapter.OrderAdapter;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class OrderHistoryActivity extends AppCompatActivity implements OrderAdapter.OrderAdapterListener {

    private List<Order> orderHistoryList;
    private OrderAdapter orderHistoryAdapter;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = new DatabaseHandler(this);
        sessionManager = new SessionManager(this);

        formatRupiah = NumberFormat.getInstance();

        progressBar = findViewById(R.id.progressBar);
        totalSalesToday = findViewById(R.id.total_sales_today);

        RecyclerView recyclerOrderHistory = findViewById(R.id.recycler_order_history);
        recyclerOrderHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerOrderHistory.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        orderHistoryList = new ArrayList<>();
        orderHistoryAdapter = new OrderAdapter(orderHistoryList, getApplicationContext(), this);
        recyclerOrderHistory.setAdapter(orderHistoryAdapter);

        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select date");
        MaterialDatePicker<Pair<Long, Long>> pickerRange = materialDateBuilder.build();

        getOrderHistory();

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
            getOrderHistory();
        });

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
                orderHistoryAdapter.getFilter().filter(query);
                return false;
            }
        });

        //db.truncate();
//        Date d = Calendar.getInstance().getTime();
//        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        currentData = formatter.format(d);

//        getOrderHistory(param_start_date, param_end_date);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOrderSelected(Order item, String Click) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", item.getSaleNo());
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void getOrderHistory() {
        orderHistoryList.clear();
        Log.e("URL_", URI.API_TEN_SALES(sessionManager.getPathUrl())+'/'+sessionManager.getId()+"?start_date="+param_start_date+"&end_date="+param_end_date);
        progressBar.setVisibility(View.VISIBLE);
        request = new JsonArrayRequest(URI.API_TEN_SALES(sessionManager.getPathUrl())+'/'+sessionManager.getId()+"?start_date="+param_start_date+"&&end_date="+param_end_date, response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            db.truncate();
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    Order listData = new Order(jsonObject.getString("id"), jsonObject.getString("sale_no"), jsonObject.getString("total_payable"), jsonObject.getString("sale_date"), jsonObject.getString("order_time"), jsonObject.getString("order_type"), jsonObject.getString("customer_name"), jsonObject.getString("order_status"), jsonObject.getString("tables_booked"), jsonObject.getString("sub_total"), jsonObject.getString("sub_total_discount_value"), jsonObject.getString("total_discount_amount"),jsonObject.getString("logistic"));
                    orderHistoryList.add(listData);
                    db.addSales(jsonObject.getString("sale_no"), String.valueOf(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            orderHistoryAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            int totalPrice = 0;
            for (int i = 0; i<orderHistoryList.size(); i++)
            {
                totalPrice += Float.parseFloat(orderHistoryList.get(i).getTotalPayable());
            }

            totalSalesToday.setText(getResources().getString(R.string.currency)+" "+formatRupiah.format(totalPrice).replace(',', '.'));
        }, error -> progressBar.setVisibility(View.GONE));
        requestQueue= Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(request);
    }

    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
        super.onBackPressed();
//        Intent intent = new Intent(this, OrderRunningActivity.class);
//        startActivity(intent);
//        finish();
    }
}
