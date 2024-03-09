package id.latenight.creativepos.tabs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class LoginActivityTabs extends AppCompatActivity {

    private TextView email, password;
    private ProgressBar progressBar;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private ImageView icLogo, mainLogo;
    private SessionManager session;

    private Animation slideUp, slideDown;

    private boolean is_tabs;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            Intent intent = new Intent(this, MenuActivityTabs.class);
            startActivity(intent);
            finish();
            // User is already logged in. Take him to main activit
        }

        icLogo = findViewById(R.id.ic_logo);
        getLogo();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

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
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.API_LOGIN(session.getPathUrl()),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String error = jsonObject.getString("error");
                        Log.e("RESPONSE ", response);
                        if(error.equals("")) {
                            Log.e("RESPONSE ", response);
                            JSONObject jsonObject_ = new JSONObject(jsonObject.getString("users"));
                            session.setId(jsonObject_.getString("id"));
                            session.setFullName(jsonObject_.getString("full_name"));
                            session.setDesignation(jsonObject_.getString("designation"));
                            session.setRole(jsonObject_.getString("role"));
                            session.setOutlet(jsonObject_.getString("outlet_id"));
                            session.setLogin(true);
                            Intent intent = new Intent(getApplicationContext(), MenuActivityTabs.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showError(getResources().getString(R.string.login_failed));
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

                String value_email = email.getText().toString();
                String value_password = password.getText().toString();

                params.put("email", value_email);
                params.put("password", value_password);
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

    public void hideAlert(View view) {
        lytAlert.setVisibility(View.INVISIBLE);
        lytAlert.startAnimation(slideDown);
    }

    private void getLogo() {
        //Log.e("URL_", URI.API_LOGO((sessionManager.getPathUrl()));
        StringRequest stringRequest = new StringRequest(URI.API_LOGO(session.getPathUrl()), response -> {
            if(response.isEmpty()) {
                icLogo.setImageResource(R.mipmap.ic_logo_foreground);
//                mainLogo.setImageResource(R.mipmap.ic_logo_foreground);
            } else {
                Glide
                .with(getApplicationContext())
                .load(response)
                .centerCrop()
                .into(icLogo);
                Glide
                .with(getApplicationContext())
                .load(response)
                .fitCenter();
//                .into(mainLogo);
            }
        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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
