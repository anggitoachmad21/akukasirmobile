package id.latenight.creativepos.tabs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.latenight.creativepos.mobile.ExpenseActivity;
import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class ExpenseFormActivityTabs extends AppCompatActivity {

    private EditText note, amount, date;
    private Spinner category;
    private ArrayList<String> categoryList;
    private ProgressBar progressBar;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private ImageView icLogo;
    private SessionManager session;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;

    private Animation slideUp, slideDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_form);

        session = new SessionManager(this);

        icLogo = findViewById(R.id.ic_logo);

        note = findViewById(R.id.note);
        amount = findViewById(R.id.amount);
        date = findViewById(R.id.date);
        category = findViewById(R.id.category);
        categoryList = new ArrayList<>();
        getCategories();

        Calendar myCalendar = Calendar.getInstance();

        date.setOnClickListener((View.OnClickListener) v -> new DatePickerDialog(ExpenseFormActivityTabs.this, (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dateFormat = "dd-MM-yyyy";
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            date.setText(sdf.format(myCalendar.getTime()));
        },
        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
        myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        progressBar = findViewById(R.id.progressBar);

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
    }

    @SuppressLint("SetTextI18n")
    public void createExpense(View view) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startAnimation(slideUp);
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_CREATE_EXPENSE(session.getPathUrl())+session.getId(),
                response -> {
                    Log.e("RESPONSE ", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String error = jsonObject.getString("error");
                        if(error.equals("")) {
                            showSuccess(jsonObject.getString("msg"));
                            Intent intent = new Intent(getApplicationContext(), ExpenseActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showError(jsonObject.getString("msg"));
                        }

                    } catch (JSONException e) {
                        showError("Terjadi kesalahan server");
                        progressBar.setVisibility(View.INVISIBLE);
                        progressBar.startAnimation(slideDown);
                    }
                },
                error -> {
                    error.printStackTrace();
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.startAnimation(slideDown);

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
                // the POST parameters:

                String value_note = note.getText().toString();
                String value_amount = amount.getText().toString();
                String value_date = date.getText().toString();
                String value_category = String.valueOf(category.getSelectedItem());

                params.put("note", value_note);
                params.put("amount", value_amount);
                params.put("date", value_date);
                params.put("category", value_category);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    private void getCategories() {
        //Log.e("URL_", URI.API_EXPENSE_CATEGORIES(session.getPathUrl())+"/"+session.getId());
        request = new JsonArrayRequest(URI.API_EXPENSE_CATEGORIES(session.getPathUrl())+"/"+session.getId(), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    categoryList.add(jsonObject.getString("name"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            category.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, categoryList));
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(request);
    }



    private void showSuccess(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        lytAlert.setBackgroundResource(R.drawable.alert_success);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.colorAccent));
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.startAnimation(slideDown);
    }

    public void showError(String message) {
        lytAlert.setVisibility(View.VISIBLE);
        lytAlert.startAnimation(slideUp);
        lytAlert.setBackgroundResource(R.drawable.alert_error);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.error));
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.startAnimation(slideDown);
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
}
