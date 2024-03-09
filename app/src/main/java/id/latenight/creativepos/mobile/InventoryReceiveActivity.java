package id.latenight.creativepos.mobile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.IngredientAdapter;
import id.latenight.creativepos.model.Ingredient;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class InventoryReceiveActivity extends AppCompatActivity implements IngredientAdapter.AdapterListener {

    private EditText note, date;
    private SearchableSpinner spinnerIngredient;
    private ArrayList<String> ingredientSpinnerList;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private SessionManager session;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private List<Ingredient> list_ingredient;
    private RecyclerView recyclerIngredient;
    private IngredientAdapter ingredientAdapter;
    private ProgressDialog loadingDialog;

    private Animation slideUp, slideDown;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_receive);

        session = new SessionManager(this);

        note = findViewById(R.id.note);
        date = findViewById(R.id.date);
        spinnerIngredient = findViewById(R.id.ingredient);
        ingredientSpinnerList = new ArrayList<>();
        getIngredients();

        Calendar myCalendar = Calendar.getInstance();

        date.setOnClickListener(v -> new DatePickerDialog(InventoryReceiveActivity.this, (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dateFormat = "dd-MM-yyyy";
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            date.setText(sdf.format(myCalendar.getTime()));
        },
                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        recyclerIngredient = findViewById(R.id.recycler_ingredient);
        recyclerIngredient.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list_ingredient = new ArrayList<>();
        ingredientAdapter = new IngredientAdapter(list_ingredient, getApplicationContext(), this);
        recyclerIngredient.setAdapter(ingredientAdapter);

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> receiveStock());

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void receiveStock() {
        String value_date = date.getText().toString();
        String value_note = note.getText().toString();

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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(obj);
        }

        String ingredients = jsonArray.toString();

        if(!ingredients.equals("[]") && !value_date.equals("") && !value_note.equals("")) {
            showLoading();
            StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_RECEIVED_STOCK(session.getPathUrl()),
                    response -> {
                        Log.e("RESPONSE ", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                showSuccess(jsonObject.getString("message"));
                                list_ingredient.clear();
                                ingredientAdapter.notifyDataSetChanged();
                                date.getText().clear();
                                note.getText().clear();
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

                    String value_note = note.getText().toString();
                    String value_date = date.getText().toString();

                    params.put("user_id", session.getId());
                    params.put("note", value_note);
                    params.put("date", value_date);
                    params.put("ingredients", ingredients);
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
                    boolean exist = list_ingredient.stream().map(Ingredient::getName).anyMatch(ingredient_name::equals);
                    if(!exist && !ingredient_name.equals(getResources().getString(R.string.select_ingredient))) {
                        list_ingredient.add(new Ingredient(position, ingredient_name, 0, "unit", 0));
                    }
                    ingredientAdapter.notifyDataSetChanged();
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
    public void onRemoveItem(Ingredient item) {

    }
}