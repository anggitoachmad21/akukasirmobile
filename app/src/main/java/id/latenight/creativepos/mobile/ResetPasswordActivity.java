package id.latenight.creativepos.mobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText oldPassword, newPassword;
    private ProgressBar progressBar;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private ImageView icLogo, mainLogo;
    private SessionManager session;

    private Animation slideUp, slideDown;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        session = new SessionManager(this);
        icLogo = findViewById(R.id.ic_logo);
//        mainLogo = findViewById(R.id.main_logo);

        oldPassword = findViewById(R.id.old_password);
        newPassword = findViewById(R.id.new_password);

        progressBar = findViewById(R.id.progressBar);

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
    }

    @SuppressLint("SetTextI18n")
    public void login(View view) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startAnimation(slideUp);
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_RESET_PASSWORD(session.getPathUrl()),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String error = jsonObject.getString("error");
                        Log.e("RESPONSE ", response);
                        if(error.equals("")) {
                            Log.e("RESPONSE ", response);
                            String msg = jsonObject.getString("msg");
                            showSuccess(msg);
                            oldPassword.setText("");
                            newPassword.setText("");
                        } else {
                            showError(error);
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

                String value_oldPassword = oldPassword.getText().toString();
                String value_newPassword = newPassword.getText().toString();

                params.put("user_id", session.getId());
                params.put("old_password", value_oldPassword);
                params.put("new_password", value_newPassword);
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
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.startAnimation(slideDown);
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

    public void hideAlert(View view) {
        lytAlert.setVisibility(View.INVISIBLE);
        lytAlert.startAnimation(slideDown);
    }
}
