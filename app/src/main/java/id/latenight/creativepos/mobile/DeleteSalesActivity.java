package id.latenight.creativepos.mobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Order;
import id.latenight.creativepos.adapter.OrderAdapter;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class DeleteSalesActivity extends AppCompatActivity implements OrderAdapter.OrderAdapterListener {

    private List<Order> orderHistoryList;
    private OrderAdapter orderHistoryAdapter;
    private ProgressBar progressBar;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private Animation slideUp;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_sales);
        sessionManager = new SessionManager(this);

        progressBar = findViewById(R.id.progressBar);

        RecyclerView recyclerOrderHistory = findViewById(R.id.recycler_order_history);
        recyclerOrderHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerOrderHistory.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        orderHistoryList = new ArrayList<>();
        orderHistoryAdapter = new OrderAdapter(orderHistoryList, getApplicationContext(), this);
        recyclerOrderHistory.setAdapter(orderHistoryAdapter);

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

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);
        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        getOrderHistory();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOrderSelected(Order item, String Click) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_sales)
                .setMessage(getResources().getString(R.string.are_you_sure))
                .setIcon(R.drawable.ic_notif)
                .setPositiveButton(R.string.delete_sales, (dialog, whichButton) -> deleteSales(item.getSaleNo(), orderHistoryList.indexOf(item)))
                .setNegativeButton(R.string.cancel, null).show();
    }

    @SuppressLint("SetTextI18n")
    private void getOrderHistory() {
        Log.e("URL_", URI.API_ALL_ORDER(sessionManager.getPathUrl()));
        progressBar.setVisibility(View.VISIBLE);
        JsonArrayRequest request = new JsonArrayRequest(URI.API_ALL_ORDER(sessionManager.getPathUrl())+'/'+sessionManager.getId(), response -> {
            JSONObject jsonObject;
            Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++) {
                try {
                    jsonObject = response.getJSONObject(i);
                    Order listData = new Order(jsonObject.getString("id"), jsonObject.getString("sale_no"), jsonObject.getString("total_payable"), jsonObject.getString("sale_date"), jsonObject.getString("order_time"), jsonObject.getString("order_type"), jsonObject.getString("customer_name"), jsonObject.getString("order_status"), jsonObject.getString("tables_booked"), jsonObject.getString("sub_total"), jsonObject.getString("sub_total_discount_value"), jsonObject.getString("total_discount_amount"), jsonObject.getString("logistic"));
                    orderHistoryList.add(listData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            orderHistoryAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }, error -> progressBar.setVisibility(View.GONE));
        RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(request);
    }

    @SuppressLint("SetTextI18n")
    private void deleteSales(String sale_id, int position) {
        //Log.e("PARAM", sale_id);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_DELETE_SALES(sessionManager.getPathUrl()),
                response -> {
                    Log.e("RESPONSE ", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if(success) {
                            showSuccess(jsonObject.getString("message"));
                            orderHistoryList.remove(position);
                            orderHistoryAdapter.notifyItemRemoved(position);
                        } else {
                            showError(jsonObject.getString("message"));
                        }

                    } catch (JSONException e) {
                        showError("Terjadi kesalahan server");
                    }
                },
                error -> {
                    error.printStackTrace();
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
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();

                params.put("sale_id", sale_id);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    public void showError(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.error));
        progressBar.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        lytAlert.setBackgroundResource(R.drawable.alert_success);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.colorAccent));
        progressBar.setVisibility(View.GONE);
    }

    public void hideLoading(View view) {
        lytAlert.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        super.onBackPressed();
    }
}
