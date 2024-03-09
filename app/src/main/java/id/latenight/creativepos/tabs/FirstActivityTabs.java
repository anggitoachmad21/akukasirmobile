package id.latenight.creativepos.tabs;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.latenight.creativepos.BuildConfig;
import id.latenight.creativepos.mobile.LoginActivity;
import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class FirstActivityTabs extends AppCompatActivity implements View.OnClickListener {

    private SessionManager session;
    private boolean doubleBackToExitPressedOnce = false;
    private ProgressDialog loadingDialog;
    private EditText editUrl;
    private ImageButton btnOpenApp;

    private boolean is_tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        session = new SessionManager(this);
        if (session.getMobileType().equals("mobile")) {
            is_tab = false;
        } else {
            is_tab = true;
        }

        if(!session.getPathUrl().isEmpty()) {
            if(is_tab == false) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Intent intent = new Intent(this, LoginActivityTabs.class);
                startActivity(intent);
                finish();
            }
        }

        LinearLayout lyt_utama = findViewById(R.id.lyt_utama);
        lyt_utama.setVisibility(View.GONE);

        ImageView imgButton = findViewById(R.id.open_app);
        imgButton.setVisibility(View.GONE);

        ImageView imgLeft = findViewById(R.id.imageView);
        imgLeft.setVisibility(View.GONE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lyt_utama.setVisibility(View.VISIBLE);
                ImageView imageUtama = findViewById(R.id.image_utama);
                imageUtama.setVisibility(View.INVISIBLE);

                PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                ObjectAnimator animationsd = ObjectAnimator.ofFloat(imageUtama, "translationY", -500f);
                animationsd.setInterpolator(myInterpolatord);
                animationsd.start();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                        ObjectAnimator animationsd = ObjectAnimator.ofFloat(imageUtama, "translationY", 0f);
                        animationsd.setInterpolator(myInterpolatord);
                        animationsd.start();
                        imageUtama.setVisibility(View.VISIBLE);
                        imgButton.setVisibility(View.VISIBLE);

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                ObjectAnimator animationsd = ObjectAnimator.ofFloat(imgLeft, "translationX", -100f);
                                animationsd.setInterpolator(myInterpolatord);
                                animationsd.start();

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                        ObjectAnimator animationsd = ObjectAnimator.ofFloat(imgLeft, "translationX", -20f);
                                        animationsd.setInterpolator(myInterpolatord);
                                        animationsd.start();
                                        imgLeft.setVisibility(View.VISIBLE);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                                ObjectAnimator animationsd = ObjectAnimator.ofFloat(imgLeft, "translationX", 0f);
                                                animationsd.setInterpolator(myInterpolatord);
                                                animationsd.start();
                                            }
                                        }, 200);
                                    }
                                }, 200);
                            }
                        }, 1000);
                    }
                }, 100);
            }
        }, 1000);

        editUrl = findViewById(R.id.url);
        btnOpenApp = findViewById(R.id.open_app);
        btnOpenApp.setOnClickListener(this);
    }

    private void checkValidationUrl() {
        showLoading();
        String url = String.valueOf(editUrl.getText());
        StringRequest stringRequest = new StringRequest("http://"+url+"/api/checkValidationUrl", response -> {
            Log.e("RESPONSE", response);
            if(response.equals("1")) {
                Log.e("VALIDATION", "valid");
                session.setPathUrl(url);
                UpdateCondition(url);
            }
        }, error -> {
            Log.e("VALIDATION", "invalid");
            hideLoading();
        });
        RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);
    }

    public void UpdateCondition(String url)
    {
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.UPDATE_SET_APK(),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if(jsonObject.getString("success").equals("true"))
                        {
                            hideLoading();
                            Log.e("Data", jsonObject.getString("data"));
                            Intent intent = new Intent(this, LoginActivityTabs.class);
                            startActivity(intent);
                        }
                        else{
                            Log.e("NOTIFICATION", "GAGAL");
                        }
                    } catch (JSONException e) {
                        Log.e( "NOTIFICATION ", "GAGAL");
                    }
                },
                error -> {
                    error.printStackTrace();

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
                params.put("token", session.getToken());
                params.put("versionCode", BuildConfig.VERSION_NAME);
                params.put("url", url);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    public void showLoading() {
        loadingDialog = ProgressDialog.show(this, "",
                "Mohon tunggu...", true);
        loadingDialog.show();
    }
    public void hideLoading() {
        loadingDialog.dismiss();
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.open_app) {
            checkValidationUrl();
        }
    }
}