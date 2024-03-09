package id.latenight.creativepos.tabs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.DataInventoryAdapter;
import id.latenight.creativepos.mobile.IngredientListActivity;
import id.latenight.creativepos.model.Ingredient;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class IngredientListActivityTabs extends AppCompatActivity implements DataInventoryAdapter.ImageAdapterListener {
    private List<Ingredient> list_product;
    private DataInventoryAdapter productAdapter;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private SearchableSpinner spinnerOutlet;
    private ArrayList<String> outletSpinnerList;
    private ProgressBar progressBar;
    private NumberFormat formatRupiah;
    private SessionManager sessionManager;
    private EditText productName;
    private String param_outlet_id="", param_keyword="";

    private AlertDialog.Builder dialogDelete;
    private LayoutInflater inflater;
    private View dialogDeleteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_list);


        sessionManager = new SessionManager(this);

        formatRupiah = NumberFormat.getInstance();

        progressBar = findViewById(R.id.progressBar);

        spinnerOutlet = findViewById(R.id.spinner_outlet);
        spinnerOutlet.setTitle(getResources().getString(R.string.select_outlet));
        outletSpinnerList = new ArrayList<>();
        getOutlets();

        productName = findViewById(R.id.product_name);

        RecyclerView recyclerMenu = findViewById(R.id.recycler_product);
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list_product = new ArrayList<>();
        productAdapter = new DataInventoryAdapter(list_product, getApplicationContext(), this);
        recyclerMenu.setAdapter(productAdapter);
        getProductsList();

        SwipeRefreshLayout swLayout = findViewById(R.id.swlayout);
        swLayout.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary);
        swLayout.setOnRefreshListener(() -> {
            getProductsList();
            swLayout.setRefreshing(false);
        });

        ImageButton filter = findViewById(R.id.filter);
        filter.setOnClickListener(v -> {
            if(!spinnerOutlet.getSelectedItem().toString().equals(getResources().getString(R.string.select_outlet))) {
                param_outlet_id = spinnerOutlet.getSelectedItem().toString();
            }
            param_keyword = productName.getText().toString();
            getProductsList();
        });
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

    private void getProductsList() {
        String params = "?outlet="+param_outlet_id+"&keyword="+param_keyword;
        String API_MENU = URI.API_INGREDIENT_LIST(sessionManager.getPathUrl())+sessionManager.getId()+params;
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
                    Ingredient listData = new Ingredient(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getInt("purchase_price"), jsonObject.getString("unit_name"), jsonObject.getInt("qty_stock"));
                    listData.setAlertQty(jsonObject.getInt("alert_quantity"));
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

    @Override
    public void onItemSelected(Ingredient item) {
        Intent intent = new Intent(this, CreateInventoryActivityTabs.class);
        intent.putExtra("inventory_id", item.getId());
        startActivity(intent);
    }

    @Override
    public void onRemoveItem(List<Ingredient> ingredient, int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setPositiveButton("Ya", (dialog, whichButton) -> {
                    deleteIngredient(ingredient.get(position).getId(), position);
                }
        );
        alertDialog.setTitle("Pemberitahuan !!");
        alertDialog.setMessage("Apakah anda yakin ingin hapus item ini?");
        alertDialog.setIcon(R.drawable.ic_notif);
        alertDialog.setNegativeButton("Tidak", null);
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

    public void deleteIngredient(int id, int position)
    {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(URI.API_DELETE_INVENTORY(sessionManager.getPathUrl())+id, response -> {
            try {
                JSONObject jsonObj = new JSONObject(String.valueOf(response));
                Boolean status = jsonObj.getBoolean("status");
                if(status == true) {
                    Log.e("response", jsonObj.getString("msg"));
                    list_product.remove(position);
                    productAdapter.notifyDataSetChanged();
                }
                else{
                    Log.e("response", jsonObj.getString("msg"));
                }

                progressBar.setVisibility(View.GONE);
            } catch (JSONException e) {
                progressBar.setVisibility(View.GONE);
                throw new RuntimeException(e);
            }

        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void createInventory(View view) {
        Intent intent = new Intent(this, CreateInventoryActivityTabs.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MenuInventoryActivityTabs.class);
        startActivity(intent);
    }
}