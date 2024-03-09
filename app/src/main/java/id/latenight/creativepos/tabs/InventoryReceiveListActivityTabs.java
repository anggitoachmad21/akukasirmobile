package id.latenight.creativepos.tabs;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import id.latenight.creativepos.mobile.InventoryReceiveDetailActivity;
import id.latenight.creativepos.mobile.MenuActivity;
import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.InventoryReceiveAdapter;
import id.latenight.creativepos.model.InventoryReceive;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class InventoryReceiveListActivityTabs extends AppCompatActivity implements InventoryReceiveAdapter.InventoryReceiveAdapterListener {

    private List<InventoryReceive> inventoryReceiveList;
    private InventoryReceiveAdapter inventoryReceiveAdapter;
    private SearchableSpinner spinnerOutlet;
    private ArrayList<String> outletSpinnerList;
    private ProgressBar progressBar;
    private EditText startDate, endDate;
    private NumberFormat formatRupiah;
    private SessionManager sessionManager;
    private String param_start_date="0", param_end_date = "0", param_outlet_id="0";
    private JsonArrayRequest request;
    private RequestQueue requestQueue;

    private boolean is_tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_receive_list);

        sessionManager = new SessionManager(this);
        formatRupiah = NumberFormat.getInstance();

        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select date");
        MaterialDatePicker<Pair<Long, Long>> pickerRange = materialDateBuilder.build();

        progressBar = findViewById(R.id.progressBar);
        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        Button btnAddInventoryReceive = findViewById(R.id.btn_add_purchase);

        spinnerOutlet = findViewById(R.id.spinner_outlet);
        spinnerOutlet.setTitle(getResources().getString(R.string.select_outlet));
        outletSpinnerList = new ArrayList<>();

        if(sessionManager.getMobileType().equals("mobile"))
        {
            is_tabs = false;
        }
        else{
            is_tabs = true;
        }

        startDate.setOnClickListener(v -> pickerRange.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        endDate.setOnClickListener(v -> pickerRange.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        btnAddInventoryReceive.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), InventoryReceiveActivityTabs.class);
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
        inventoryReceiveList = new ArrayList<>();
        inventoryReceiveAdapter = new InventoryReceiveAdapter(inventoryReceiveList, getApplicationContext(), this);
        recyclerOrderHistory.setAdapter(inventoryReceiveAdapter);

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
                inventoryReceiveAdapter.getFilter().filter(query);
                return false;
            }
        });

        getInventoryReceiveList();
        getOutlets();

        SwipeRefreshLayout swLayout = findViewById(R.id.swlayout);
        swLayout.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary);
        swLayout.setOnRefreshListener(() -> {
            getInventoryReceiveList();
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
            getInventoryReceiveList();
        });
    }

    @SuppressLint("SetTextI18n")
    private void getInventoryReceiveList() {
        inventoryReceiveList.clear();
        Log.e("URL_", URI.API_INVENTORY_RECEIVE_LIST(sessionManager.getPathUrl()) + sessionManager.getId() + "/" + param_start_date + "/" + param_end_date + "/" + param_outlet_id);
        progressBar.setVisibility(View.VISIBLE);
        JsonArrayRequest request = new JsonArrayRequest(URI.API_INVENTORY_RECEIVE_LIST(sessionManager.getPathUrl()) + sessionManager.getId() + "/" + param_start_date + "/" + param_end_date + "/" + param_outlet_id, response -> {
            Log.e("Response", response.toString());
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++) {
                try {
                    jsonObject = response.getJSONObject(i);
                    InventoryReceive listData = new InventoryReceive(
                        jsonObject.getString("id"),
                        jsonObject.getString("transfer_unique_id"),
                        jsonObject.getString("date"),
                        jsonObject.getString("total_qty"),
                        jsonObject.getString("notes")
                    );
                    inventoryReceiveList.add(listData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            inventoryReceiveAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
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

    @Override
    public void onOrderSelected(InventoryReceive item) {
        if(is_tabs == false) {
            Intent intent = new Intent(this, InventoryReceiveDetailActivity.class);
            intent.putExtra("receive_id", item.getId());
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, InventoryReceiveDetailActivityTabs.class);
            intent.putExtra("receive_id", item.getId());
            startActivity(intent);
        }
    }
}