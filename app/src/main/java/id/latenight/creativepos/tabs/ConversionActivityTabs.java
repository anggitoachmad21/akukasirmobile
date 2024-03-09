
package id.latenight.creativepos.tabs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.ConversionAdapter;
import id.latenight.creativepos.model.Conversion;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class ConversionActivityTabs extends AppCompatActivity implements ConversionAdapter.AdapterListener {

    private EditText note, date;
    private SearchableSpinner spinnerIngredient;
    private ArrayList<String> ingredientSpinnerList;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private SessionManager session;
    private JsonArrayRequest request;

    private List<String> units;
    private RequestQueue requestQueue;
    private List<Conversion> list_ingredient;
    private RecyclerView recyclerIngredient;
    private ConversionAdapter ingredientAdapter;
    private ProgressDialog loadingDialog;

    private String unit_name = "";

    private EditText notes;

    private Animation slideUp, slideDown;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        session = new SessionManager(this);

        note = findViewById(R.id.note);
        spinnerIngredient = findViewById(R.id.ingredient);
        ingredientSpinnerList = new ArrayList<>();
        getIngredients();

        units = new ArrayList<>();

        recyclerIngredient = findViewById(R.id.recycler_ingredient);
        recyclerIngredient.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list_ingredient = new ArrayList<>();
        ingredientAdapter = new ConversionAdapter(list_ingredient, getApplicationContext(), this);
        recyclerIngredient.setAdapter(ingredientAdapter);

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> receiveStock());
//        btnSave.setOnClickListener(v -> showSuccess("Masih DI Perbaiki...."));

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        notes = findViewById(R.id.note);

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        getUnits();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void receiveStock() {
//        String value_date = date.getText().toString();
//        String value_note = note.getText().toString();
        JSONObject obj = null;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list_ingredient.size(); i++) {
            obj = new JSONObject();
            try {
                String str = list_ingredient.get(i).getName();
                String[] splitStr = str.split(" - ");
                obj.put("id", list_ingredient.get(i).getId());
                obj.put("name", splitStr[0] + " - " +splitStr[1]);
                obj.put("price", list_ingredient.get(i).getPrice());
                obj.put("unit", list_ingredient.get(i).getUnit());
                obj.put("qty", list_ingredient.get(i).getQty());
                obj.put("ingre", list_ingredient.get(i).getIngre());
                obj.put("unit_name", list_ingredient.get(i).getUnit_text());
                obj.put("qty_new", list_ingredient.get(i).getQty_new());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(obj);
        }

        String ingredients = jsonArray.toString();
        Log.e("EXAMPLE", ingredients);
//        return;

        if(!ingredients.equals("[]")) {
            showLoading();
            StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_CONVERT_INGREDIENTS(session.getPathUrl()),
                    response -> {
                        Log.e("RESPONSE ", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                showSuccess(jsonObject.getString("message"));
                                list_ingredient.clear();
                                ingredientAdapter.notifyDataSetChanged();
                                spinnerIngredient.setSelection(ingredientSpinnerList.indexOf(getResources().getString(R.string.select_ingredient)));
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

                    params.put("user_id", session.getId());
                    params.put("ingredients", ingredients);
                    params.put("notes", String.valueOf(notes.getText()));
                    params.put("outlet_id", session.getOutlet());
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(postRequest);
        } else {
            showError("Lengkapi form diatas");
        }
    }

    private void getIngredients() {
        request = new JsonArrayRequest(URI.API_INGREDIENTS_LIST(session.getPathUrl())+session.getOutlet(), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            ingredientSpinnerList.add(""+getResources().getString(R.string.select_ingredient)+"");
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    ingredientSpinnerList.add(jsonObject.getString("name"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerIngredient.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, ingredientSpinnerList));
            spinnerIngredient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String ingredient_name = spinnerIngredient.getSelectedItem().toString();
                    boolean exist = list_ingredient.stream().map(Conversion::getName).anyMatch(ingredient_name::equals);
                    if(!exist && !ingredient_name.equals(getResources().getString(R.string.select_ingredient))) {
                        getDetailUnits(position, ingredient_name);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void getUnits()
    {
        StringRequest stringRequest = new StringRequest(URI.API_GET_UNITS(session.getPathUrl()), response -> {
            try {
                JSONObject jsonObj = new JSONObject(String.valueOf(response));
                Log.e("response", String.valueOf(jsonObj));
                JSONArray data = jsonObj.getJSONArray("data");
                for(int i=0; i<data.length(); i++){
                    JSONObject d = data.getJSONObject(i);
                    String v_unit = d.getString("unit_name");
                    units.add(v_unit);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void getDetailUnits(int position, String ingredient_name)
    {
        StringRequest stringRequest = new StringRequest(URI.API_INGREDIENTS_CONVERT(session.getPathUrl(), String.valueOf(position)), response -> {
            try {
                Log.e("Link", URI.API_INGREDIENTS_CONVERT(session.getPathUrl(), String.valueOf(position)));
                JSONObject jsonObj = new JSONObject(String.valueOf(response));
                Log.e("details", String.valueOf(jsonObj));
                list_ingredient.add(new Conversion(position, ingredient_name, 0, "unit", 0, units, jsonObj.getString("unit_name"), ingredientSpinnerList, "units", "0", "gram", "0"));
                ingredientAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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
        // code here to show dialog
        super.onBackPressed();  // optional depending on your needs
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRemoveItem(Conversion item) {

    }
}