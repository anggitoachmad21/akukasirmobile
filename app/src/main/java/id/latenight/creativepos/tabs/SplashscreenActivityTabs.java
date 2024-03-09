package id.latenight.creativepos.tabs;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import id.latenight.creativepos.mobile.FirstActivity;
import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;

public class SplashscreenActivityTabs extends AppCompatActivity {
    private SessionManager session;
    ImageView img;
    LinearLayout txt;
    private boolean is_tab;
    private ProgressDialog loadingDialog;
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

        session = new SessionManager(this);
        if(!session.getMobileType().isEmpty()) {
            if (session.getMobileType().equals("mobile")) {
                is_tab = false;
            } else {
                is_tab = true;
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

            final Handler handlers = new Handler();
            handlers.postDelayed(new Runnable() {
                @Override
                public void run() {
                    session.setMobileType("other");
                    ActivityCompat.finishAffinity(SplashscreenActivityTabs.this);
                    Intent intent = new Intent(getApplicationContext(), SplashscreenActivityTabs.class);
                    startActivity(intent);
                }
            }, 5000);
        }
        else{
            checkVersionMobile();
        }
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
