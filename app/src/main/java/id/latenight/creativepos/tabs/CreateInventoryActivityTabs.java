package id.latenight.creativepos.tabs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Map;

import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.IngredientAdapter;
import id.latenight.creativepos.model.Ingredient;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class CreateInventoryActivityTabs extends AppCompatActivity implements IngredientAdapter.AdapterListener {

    private int inventory_id;
    private boolean update_product = false;
    private String param_group_outlet="", param_category="", param_unit="";
    private EditText inventoryName, price, minimumStock, now_stock;
    private SearchableSpinner spinnerUnit, spinnerCategory, spinnerGroupOutlet;
    private ArrayList<String> unitSpinnerList, groupOutletSpinnerList, categorySpinnerList;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private SessionManager session;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private ProgressDialog loadingDialog;
    private LinearLayout lytStatus;
    private RadioGroup radioStatus;
    private RadioButton active, inactive;
    private Animation slideUp, slideDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_inventory);

        session = new SessionManager(this);

        inventoryName = findViewById(R.id.inventory_name);
        price = findViewById(R.id.price);
        minimumStock = findViewById(R.id.minimum_stock);
        now_stock = findViewById(R.id.now_stock);
        lytStatus = findViewById(R.id.lyt_status);
        radioStatus = findViewById(R.id.radioStatus);
        active = findViewById(R.id.active);
        inactive = findViewById(R.id.inactive);

        spinnerGroupOutlet = findViewById(R.id.spinner_group_outlet);
        groupOutletSpinnerList = new ArrayList<>();
        spinnerGroupOutlet.setTitle("Pilih Group Outlet");

        spinnerUnit = findViewById(R.id.spinner_unit);
        unitSpinnerList = new ArrayList<>();
        spinnerUnit.setTitle("Pilih Unit");

        spinnerCategory = findViewById(R.id.spinner_category);
        categorySpinnerList = new ArrayList<>();
        spinnerCategory.setTitle("Pilih Kategori");

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> createProduct());

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);
        ImageButton btnCloseAlert = findViewById(R.id.btn_close_alert);
        btnCloseAlert.setOnClickListener(v -> lytAlert.setVisibility(View.GONE));
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            inventory_id = extras.getInt("inventory_id");
            getProductData(inventory_id);
        } else {
            getGroupOutlets();
            getUnits();
            getCategory();
        }
    }

    private void getProductData(int inventory_id) {
        Log.e("URL_", URI.API_DETAIL_INVENTORY(session.getPathUrl())+inventory_id);
        StringRequest stringRequest = new StringRequest(URI.API_DETAIL_INVENTORY(session.getPathUrl())+inventory_id, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                Log.e("RESPONSE", jsonObject.toString());
                inventoryName.setText(jsonObject.getString("name"));
                price.setText(String.valueOf(jsonObject.getInt("purchase_price")));
                minimumStock.setText(String.valueOf(jsonObject.getInt("alert_quantity")));

                param_unit = jsonObject.getString("unit_name");
                param_category = jsonObject.getString("category_name");
                param_group_outlet = jsonObject.getString("group_outlet");
                now_stock.setText(jsonObject.getString("qty_stock"));

                getUnits();
                getCategory();
                getGroupOutlets();

                update_product = true;
                lytStatus.setVisibility(View.VISIBLE);
                if(jsonObject.getString("del_status").equals("Live")) {
                    active.setChecked(true);
                } else {
                    inactive.setChecked(true);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void createProduct() {
        String value_inventory_name = inventoryName.getText().toString();
        String value_price = price.getText().toString();
        String value_minimum_stock = minimumStock.getText().toString();
        String value_group_outlet = spinnerGroupOutlet.getSelectedItem().toString();
        String value_unit = spinnerUnit.getSelectedItem().toString();
        String value_category = spinnerCategory.getSelectedItem().toString();
        String del_status = "Live";
        if(!active.isChecked()) {
            del_status = "Deleted";
        }

        if(!value_inventory_name.equals("")
                && !value_price.equals("") && !value_minimum_stock.equals("")
                && !value_group_outlet.equals("Pilih Group Outlet") && !value_unit.equals("Pilih Unit")
                && !value_category.equals("Pilih Kategori")) {
            showLoading();
            String URL_PRODUCT = URI.API_CREATE_INVENTORY(session.getPathUrl());
            if(update_product) {
                URL_PRODUCT = URI.API_UPDATE_INVENTORY(session.getPathUrl());
            }
            String finalDel_status = del_status;
            StringRequest postRequest = new StringRequest(Request.Method.POST, URL_PRODUCT,
                    response -> {
                        Log.e("RESPONSE ", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                if(update_product) {
                                    Intent intent = new Intent(this, IngredientListActivityTabs.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    showSuccess(jsonObject.getString("message"));
                                    inventoryName.getText().clear();
                                    price.getText().clear();
                                    minimumStock.getText().clear();
                                    spinnerGroupOutlet.setSelection(groupOutletSpinnerList.indexOf("Pilih Group Outlet"));
                                    spinnerUnit.setSelection(unitSpinnerList.indexOf("Pilih Unit"));
                                    spinnerCategory.setSelection(categorySpinnerList.indexOf("Pilih Kategori"));
                                }
                            } else {
                                showError(jsonObject.getString("message"));
                            }

                            hideLoading();
                        } catch (JSONException e) {
                            showError("Terjadi kesalahan server");
                            hideLoading();
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
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    // the POST parameters:
                    if(update_product) {
                        params.put("inventory_id", String.valueOf(inventory_id));
                        params.put("del_status", finalDel_status);
                    }
                    params.put("user_id", session.getId());
                    params.put("inventory_name", value_inventory_name);
                    params.put("price", value_price);
                    params.put("minimum_stok", value_minimum_stock);
                    params.put("group_outlet", value_group_outlet);
                    params.put("unit", value_unit);
                    params.put("category", value_category);
                    params.put("qty_stock", now_stock.getText().toString());
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(postRequest);
        } else {
            showError("Lengkapi form diatas");
        }
    }

    private void getGroupOutlets() {
        groupOutletSpinnerList.clear();
        request = new JsonArrayRequest(URI.API_GROUP_OUTLETS_LIST(session.getPathUrl()), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            groupOutletSpinnerList.add("Pilih Group Outlet");
            if(!param_group_outlet.equals("")) {
                groupOutletSpinnerList.add(param_group_outlet);
            }
            for (int i = 0; i < response.length(); i++) {
                try {
                    jsonObject = response.getJSONObject(i);
                    if (!jsonObject.getString("name").equals(param_group_outlet)) {
                        groupOutletSpinnerList.add(jsonObject.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerGroupOutlet.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, groupOutletSpinnerList));
            if(!param_group_outlet.equals("")) {
                spinnerGroupOutlet.setSelection(groupOutletSpinnerList.indexOf(param_group_outlet));
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getUnits() {
        unitSpinnerList.clear();
        Log.e("URL", URI.API_UNIT_INGREDIENT(session.getPathUrl())+session.getId());
        request = new JsonArrayRequest(URI.API_UNIT_INGREDIENT(session.getPathUrl())+session.getId(), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            unitSpinnerList.add("Pilih Unit");
            if(!param_unit.equals("")) {
                unitSpinnerList.add(param_unit);
            }
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    if(!jsonObject.getString("unit_name").equals(param_unit)) {
                        unitSpinnerList.add(jsonObject.getString("unit_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerUnit.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, unitSpinnerList));
            if(!param_unit.equals("")) {
                spinnerUnit.setSelection(unitSpinnerList.indexOf(param_unit));
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getCategory() {
        categorySpinnerList.clear();
        Log.e("URL", URI.API_CATEGORY_INGREDIENT(session.getPathUrl())+session.getId());
        request = new JsonArrayRequest(URI.API_CATEGORY_INGREDIENT(session.getPathUrl())+session.getId(), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            categorySpinnerList.add("Pilih Kategori");
            if(!param_category.equals("")) {
                categorySpinnerList.add(param_category);
            }
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    if(!jsonObject.getString("category_name").equals(param_category)) {
                        categorySpinnerList.add(jsonObject.getString("category_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerCategory.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, categorySpinnerList));
            if(!param_category.equals("")) {
                spinnerCategory.setSelection(categorySpinnerList.indexOf(param_category));
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void showLoading() {
        loadingDialog = ProgressDialog.show(this, "",
                "Mohon tunggu...", true);
        loadingDialog.show();
    }
    public void hideLoading() {
        loadingDialog.dismiss();
    }

    private void showSuccess(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        lytAlert.setBackgroundResource(R.drawable.alert_success);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    public void showError(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        lytAlert.setBackgroundResource(R.drawable.alert_error);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.error));
    }

    public void hideAlert(View view) {
        lytAlert.setVisibility(View.INVISIBLE);
        lytAlert.startAnimation(slideDown);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, IngredientListActivityTabs.class);
        startActivity(intent);
        finish();
        // code here to show dialog
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRemoveItem(Ingredient item) {

    }
}