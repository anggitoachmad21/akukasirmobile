package id.latenight.creativepos.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class OpenRegisterActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private SessionManager session;
    private EditText balance;

    private Animation slideUp, slideDown;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_register);

        session = new SessionManager(this);

        balance = findViewById(R.id.balance);
        Button openCashier = findViewById(R.id.open_cashier);

        progressBar = findViewById(R.id.progressBar);

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        ImageView close = findViewById(R.id.close);

        close.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            finish();
        });

        openCashier.setOnClickListener(v -> {
            if(balance.getText().toString().isEmpty()) {
                showError(getResources().getString(R.string.enter_your_opening_balance));
            } else {
                openCashier();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String closed = extras.getString("closed");
            if(closed.equals("true")) {
                showSuccess(getResources().getString(R.string.cashier_closed));
            }
        }
    }

    public void openCashier() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.BALANCE_OPEN_REGISTER(session.getPathUrl()),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        //Log.e("RESPONSE", response);
                        if(success.equals("true")) {
                            session.setOpenRegistration("1");
                            Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = jsonObject.getString("message");
                            showError(message);
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

                String value_balance = balance.getText().toString();

                params.put("user_id", session.getId());
                params.put("balance", value_balance);
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
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
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.double_click_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }
}
