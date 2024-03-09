package id.latenight.creativepos.mobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Purchase;
import id.latenight.creativepos.adapter.PurchaseAdapter;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class PurchaseOrderListActivity extends AppCompatActivity implements PurchaseAdapter.PurchaseAdapterListener {

    private List<Purchase> orderHistoryList;
    private PurchaseAdapter purchaseAdapter;
    private SearchableSpinner spinnerOutlet;
    private ArrayList<String> outletSpinnerList;
    private ProgressBar progressBar;
    private EditText startDate, endDate;
    private TextView totalSalesToday;
    private NumberFormat formatRupiah;
    private SessionManager sessionManager;
    private String param_start_date="0", param_end_date = "0", param_outlet_id="0";
    private JsonArrayRequest request;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_order_list);

        sessionManager = new SessionManager(this);
        formatRupiah = NumberFormat.getInstance();

        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select date");
        MaterialDatePicker<Pair<Long, Long>> pickerRange = materialDateBuilder.build();

        progressBar = findViewById(R.id.progressBar);
        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        Button btnAddPurchase = findViewById(R.id.btn_add_purchase);
        totalSalesToday = findViewById(R.id.total_sales_today);

        spinnerOutlet = findViewById(R.id.spinner_outlet);
        spinnerOutlet.setTitle(getResources().getString(R.string.select_outlet));
        outletSpinnerList = new ArrayList<>();

        startDate.setOnClickListener(v -> pickerRange.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        endDate.setOnClickListener(v -> pickerRange.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        btnAddPurchase.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PurchaseOrderActivity.class);
            startActivity(intent);
        });

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
        });

        RecyclerView recyclerOrderHistory = findViewById(R.id.recycler_order_history);
        recyclerOrderHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerOrderHistory.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        orderHistoryList = new ArrayList<>();
        purchaseAdapter = new PurchaseAdapter(orderHistoryList, getApplicationContext(), this);
        recyclerOrderHistory.setAdapter(purchaseAdapter);

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
                purchaseAdapter.getFilter().filter(query);
                return false;
            }
        });

        getOrderHistory();
        getOutlets();

        SwipeRefreshLayout swLayout = findViewById(R.id.swlayout);
        swLayout.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary);
        swLayout.setOnRefreshListener(() -> {
            getOrderHistory();
            swLayout.setRefreshing(false);
        });

        if(sessionManager.getRole().equals("Admin")) {
            spinnerOutlet.setVisibility(View.VISIBLE);
        }

        ImageButton filter = findViewById(R.id.filter);
        filter.setOnClickListener(v -> {
            if(!spinnerOutlet.getSelectedItem().toString().equals(getResources().getString(R.string.select_outlet))) {
                param_outlet_id = spinnerOutlet.getSelectedItem().toString();
            }
            getOrderHistory();
        });
    }

    @SuppressLint("SetTextI18n")
    private void getOrderHistory() {
        orderHistoryList.clear();
        Log.e("URL_", URI.API_PURCHASE_LIST(sessionManager.getPathUrl()) + sessionManager.getId() + "/" + param_start_date + "/" + param_end_date + "/" + param_outlet_id);
        progressBar.setVisibility(View.VISIBLE);
        JsonArrayRequest request = new JsonArrayRequest(URI.API_PURCHASE_LIST(sessionManager.getPathUrl()) + sessionManager.getId() + "/" + param_start_date + "/" + param_end_date + "/" + param_outlet_id, response -> {
            Log.e("Response", response.toString());
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++) {
                try {
                    jsonObject = response.getJSONObject(i);
                    Purchase listData = new Purchase(
                        jsonObject.getString("id"),
                        jsonObject.getString("reference_no"),
                        jsonObject.getString("date"),
                        jsonObject.getString("supplier_name"),
                        jsonObject.getString("grand_total"),
                        jsonObject.getString("note")
                    );
                    orderHistoryList.add(listData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            purchaseAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            int totalPrice = 0;
            for (int i = 0; i < orderHistoryList.size(); i++) {
                totalPrice += Float.parseFloat(orderHistoryList.get(i).getTotalPurchase());
            }

            totalSalesToday.setText(getResources().getString(R.string.currency) + " " + formatRupiah.format(totalPrice).replace(',', '.'));
        }, error -> progressBar.setVisibility(View.GONE));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getOutlets() {
        request = new JsonArrayRequest(URI.API_OUTLETS_LIST(sessionManager.getPathUrl()), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            outletSpinnerList.add(""+getResources().getString(R.string.select_outlet)+"");
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    outletSpinnerList.add(jsonObject.getString("outlet_name"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerOutlet.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, outletSpinnerList));
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDelete(Purchase item, int position) {
        Log.e("ID", item.getId());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Hapus pengeluaran?");
        alertDialogBuilder
                .setMessage("Klik Ya untuk menghapus!")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialog, id) -> {
                    deletePurchase(item.getId(), position);
                })
                .setNegativeButton("Tidak", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onOrderSelected(Purchase item) {
        Intent intent = new Intent(this, PurchaseOrderDetailActivity.class);
        intent.putExtra("purchase_id", item.getId());
        startActivity(intent);
    }


    private void deletePurchase(String id, int position) {
        StringRequest stringRequest = new StringRequest(URI.API_DELETE_PURCHASE(sessionManager.getPathUrl())+id, response -> {
            Log.e("Response", response);
            purchaseAdapter.removeItem(position);
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}