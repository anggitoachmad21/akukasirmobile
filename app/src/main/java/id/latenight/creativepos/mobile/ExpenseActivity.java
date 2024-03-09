package id.latenight.creativepos.mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Expense;
import id.latenight.creativepos.adapter.ExpenseAdapter;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class ExpenseActivity extends AppCompatActivity implements ExpenseAdapter.ExpenseAdapterListener {

    private List<Expense> expenseList;
    private ExpenseAdapter expenseAdapter;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private EditText startDate, endDate;
    private SearchableSpinner spinnerOutlet;
    private ArrayList<String> outletSpinnerList;
    private ProgressBar progressBar;
    private NumberFormat formatRupiah;
    private TextView totalSalesToday;
    private DatabaseHandler db;
    private String currentData;
    private SessionManager sessionManager;
    private String param_start_date="0", param_end_date = "0", param_outlet_id="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        db = new DatabaseHandler(this);
        sessionManager = new SessionManager(this);
        formatRupiah = NumberFormat.getInstance();

        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        spinnerOutlet = findViewById(R.id.spinner_outlet);
        spinnerOutlet.setTitle(getResources().getString(R.string.select_outlet));
        outletSpinnerList = new ArrayList<>();

        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select date");
        MaterialDatePicker<Pair<Long, Long>> pickerRange = materialDateBuilder.build();
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
        });

        progressBar = findViewById(R.id.progressBar);
        totalSalesToday = findViewById(R.id.total_sales_today);

        RecyclerView recyclerExpense = findViewById(R.id.recycler_expense);
        recyclerExpense.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerExpense.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(expenseList, getApplicationContext(), this);
        recyclerExpense.setAdapter(expenseAdapter);

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
                expenseAdapter.getFilter().filter(query);
                return false;
            }
        });

        //db.truncate();
        Date d = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        currentData = formatter.format(d);

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
    @Override
    public void onExpenseSelected(Expense item) {
//        Intent intent = new Intent(this, OrderDetailActivity.class);
//        intent.putExtra("order_id", item.getId());
//        startActivity(intent);
    }

    @Override
    public void onDelete(Expense item, int position) {
        Log.e("ID", item.getId());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Hapus pengeluaran?");
        alertDialogBuilder
                .setMessage("Klik Ya untuk menghapus!")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialog, id) -> {
                    deleteExpense(item.getId(), position);
                })
                .setNegativeButton("Tidak", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteExpense(String id, int position) {
        StringRequest stringRequest = new StringRequest(URI.API_DELETE_EXPENSE(sessionManager.getPathUrl())+id, response -> {
            Log.e("Response", response);
            expenseAdapter.removeItem(position);
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @SuppressLint("SetTextI18n")
    private void getOrderHistory() {
        expenseList.clear();
        Log.e("URL_", URI.API_EXPENSES(sessionManager.getPathUrl())+sessionManager.getId() + "/" + param_start_date + "/" + param_end_date + "/" + param_outlet_id);
        progressBar.setVisibility(View.VISIBLE);
        request = new JsonArrayRequest(URI.API_EXPENSES(sessionManager.getPathUrl())+sessionManager.getId() + "/" + param_start_date + "/" + param_end_date + "/" + param_outlet_id, response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    Expense listData = new Expense(jsonObject.getString("id"), jsonObject.getString("note"), jsonObject.getString("date"), jsonObject.getString("amount"), jsonObject.getString("category"), jsonObject.getString("pic"));
                    expenseList.add(listData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            expenseAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            int totalPrice = 0;
            for (int i = 0; i<expenseList.size(); i++)
            {
                totalPrice += Float.parseFloat(expenseList.get(i).getAmount());
            }

            totalSalesToday.setText(getResources().getString(R.string.currency)+" "+formatRupiah.format(totalPrice).replace(',', '.'));
        }, error -> progressBar.setVisibility(View.GONE));
        requestQueue= Volley.newRequestQueue(this);
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

    public void expenseForm(View view) {
        Intent intent = new Intent(this, ExpenseFormActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
}
