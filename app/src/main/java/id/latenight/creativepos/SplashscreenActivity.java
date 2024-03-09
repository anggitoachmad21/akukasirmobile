package id.latenight.creativepos;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.latenight.creativepos.mobile.FirstActivity;
import id.latenight.creativepos.tabs.FirstActivityTabs;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class SplashscreenActivity extends AppCompatActivity {
    private SessionManager session;
    ImageView img;
    LinearLayout txt;
    private boolean is_tab;
    private ProgressDialog loadingDialog;
    private String token, model, manufacturer, type, device, application_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splashscreen);

        img = findViewById(R.id.image);

        img.setVisibility(View.INVISIBLE);


        txt = findViewById(R.id.text);
        txt.setVisibility(View.INVISIBLE);

        manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;
        device = Build.DEVICE;
        application_id = Build.ID;

        session = new SessionManager(this);
        if(!session.getMobileType().isEmpty()) {
            if (session.getMobileType().equals("mobile")) {
                is_tab = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                is_tab = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        penyesuaianTampilan();
    }

    public void penyesuaianTampilan() {
        if(session.getMobileType().isEmpty())
        {
            loadingDialog = ProgressDialog.show(this, "",
                    "Penyesuaian...", true);
            loadingDialog.show();

            generateToken();
            push_notif();
        }
        else{
            checkVersionMobile();
        }
    }

    public void generateToken() {
        Random r = new Random();
        int randomNumber = r.nextInt(1000000);
        token = String.valueOf(randomNumber);
    }

    public void push_notif(){
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.PUSH_APK_NOTIFICATION(),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if(jsonObject.getString("success").equals("true"))
                        {
                            Log.e("Data", jsonObject.getString("data"));
                            session.setToken(jsonObject.getString("token"));
                            session.setMobileType(jsonObject.getString("type"));
                            final Handler handlers = new Handler();
                            handlers.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ActivityCompat.finishAffinity(SplashscreenActivity.this);
                                    Intent intent = new Intent(getApplicationContext(), SplashscreenActivity.class);
                                    startActivity(intent);
                                    }
                                }, 3000);
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
                params.put("token", "APK_ON_T_"+token);
                params.put("manufacturer", manufacturer);
                params.put("model", model);
                params.put("type", "other");
                params.put("device", device);
                params.put("mobile_id", application_id);

//                params.put("token", "APK_ON_M_"+token);
//                params.put("manufacturer", manufacturer);
//                params.put("model", model);
//                params.put("type", "mobile");
//                params.put("device", device);
//                params.put("mobile_id", application_id);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    public void checkVersionMobile(){
        anim();
    }


    private void anim()
    {
        final Handler handlers = new Handler();
        handlers.postDelayed(new Runnable() {
            @Override
            public void run() {
                img.setVisibility(View.VISIBLE);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                alphaAnimation.setDuration(1500);

                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        txt.setVisibility(View.VISIBLE);
                        PathInterpolator myInterpolator = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                        ObjectAnimator animations = ObjectAnimator.ofFloat(txt, "translationY", 50f);
                        animations.setInterpolator(myInterpolator);
                        animations.start();

                        PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                        ObjectAnimator animationsd = ObjectAnimator.ofFloat(img, "translationY", -50f);
                        animationsd.setInterpolator(myInterpolatord);
                        animationsd.start();

                        final Handler handlers = new Handler();
                        handlers.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                PathInterpolator myInterpolator = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                ObjectAnimator animations = ObjectAnimator.ofFloat(txt, "translationY", 0f);
                                animations.setInterpolator(myInterpolator);
                                animations.start();

                                PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                ObjectAnimator animationsd = ObjectAnimator.ofFloat(img, "translationY", 0f);
                                animationsd.setInterpolator(myInterpolatord);
                                animationsd.start();

                                final Handler handlers = new Handler();
                                handlers.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PathInterpolator myInterpolator = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                        ObjectAnimator animations = ObjectAnimator.ofFloat(txt, "translationY", 50f);
                                        animations.setInterpolator(myInterpolator);
                                        animations.start();

                                        PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                        ObjectAnimator animationsd = ObjectAnimator.ofFloat(img, "translationY", -50f);
                                        animationsd.setInterpolator(myInterpolatord);
                                        animationsd.start();

                                        final Handler handlers = new Handler();
                                        handlers.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                PathInterpolator myInterpolator = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                                ObjectAnimator animations = ObjectAnimator.ofFloat(txt, "translationY", 0f);
                                                animations.setInterpolator(myInterpolator);
                                                animations.start();

                                                PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                                ObjectAnimator animationsd = ObjectAnimator.ofFloat(img, "translationY", 0f);
                                                animationsd.setInterpolator(myInterpolatord);
                                                animationsd.start();

                                                final Handler handlers = new Handler();
                                                handlers.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        PathInterpolator myInterpolator = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                                        ObjectAnimator animations = ObjectAnimator.ofFloat(txt, "translationY", 0f);
                                                        animations.setInterpolator(myInterpolator);
                                                        animations.start();

                                                        PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                                        ObjectAnimator animationsd = ObjectAnimator.ofFloat(img, "translationY", 0f);
                                                        animationsd.setInterpolator(myInterpolatord);
                                                        animationsd.start();

                                                        final Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                PathInterpolator myInterpolator = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                                                ObjectAnimator animations = ObjectAnimator.ofFloat(txt, "translationY", 2000f);
                                                                animations.setInterpolator(myInterpolator);
                                                                animations.start();

                                                                PathInterpolator myInterpolatord = new PathInterpolator(0.5f, 0.7f, 0.1f, 1.0f);
                                                                ObjectAnimator animationsd = ObjectAnimator.ofFloat(img, "translationY", -2000f);
                                                                animationsd.setInterpolator(myInterpolatord);
                                                                animationsd.start();

                                                                final Handler handler = new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if(is_tab == false) {
                                                                            startActivity(new Intent(getApplicationContext(), FirstActivity.class));
                                                                            finish();
                                                                        }
                                                                        else{
                                                                            startActivity(new Intent(getApplicationContext(), FirstActivityTabs.class));
                                                                            finish();
                                                                        }
                                                                    }
                                                                }, 100);
                                                            }
                                                        }, 1500);
                                                    }
                                                }, 100);
                                            }
                                        }, 100);
                                    }
                                }, 100);
                            }
                        }, 100);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                img.startAnimation(alphaAnimation);
            }
        }, 1000);
    }
}
