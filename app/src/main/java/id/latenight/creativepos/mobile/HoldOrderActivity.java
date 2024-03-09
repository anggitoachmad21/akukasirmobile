package id.latenight.creativepos.mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Order;
import id.latenight.creativepos.adapter.OrderAdapter;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class HoldOrderActivity extends AppCompatActivity implements OrderAdapter.OrderAdapterListener {

    private List<Order> runningOrderList, orderHistoryList;
    private OrderAdapter runningOrderAdapter, orderHistoryAdapter;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private ProgressBar progressBar;
    private NumberFormat formatRupiah;
    private TextView totalSalesToday;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_order);
        sessionManager = new SessionManager(this);

        if(sessionManager.getMobileType().equals("mobile")){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

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

        getRunningOrder();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOrderSelected(Order item, String Click) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", item.getSaleNo());
        startActivity(intent);
    }

    private void getRunningOrder() {
        //Log.e("URL_", URI.API_NEW_ORDER);
        progressBar.setVisibility(View.VISIBLE);
        request = new JsonArrayRequest(URI.API_NEW_ORDER(sessionManager.getPathUrl()), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    Order listData = new Order(jsonObject.getString("id"), jsonObject.getString("sale_no"), jsonObject.getString("total_payable"), jsonObject.getString("sale_date"), jsonObject.getString("order_time"), jsonObject.getString("order_type"), jsonObject.getString("customer_name"), jsonObject.getString("order_status"), jsonObject.getString("tables_booked"), jsonObject.getString("sub_total"), jsonObject.getString("sub_total_discount_value"), jsonObject.getString("total_discount_amount"), jsonObject.getString("logistic"));
                    runningOrderList.add(listData);
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


    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        super.onBackPressed();
    }
}
