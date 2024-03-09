package id.latenight.creativepos.tabs;

import static id.latenight.creativepos.util.MyApplication.RC_ENABLE_BLUETOOTH;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import id.latenight.creativepos.R;
import id.latenight.creativepos.mobile.AdminActivity;
import id.latenight.creativepos.util.MyApplication;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class DailyReportDetailTabs extends AppCompatActivity {

    private EditText date;
    TextView title, content;
    String s_title, s_content, s_report;
    String param_date = "";
    private SessionManager sessionManager;
    private ImageView icLogo;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report_detail);

        sessionManager = new SessionManager(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            s_title = extras.getString("title");
            s_content = extras.getString("content");
            s_report = extras.getString("report");
        }
        icLogo = findViewById(R.id.ic_logo);
        getLogo();
        title = findViewById(R.id.title);
        content = findViewById(R.id.content);

        date = findViewById(R.id.date);
        Calendar myCalendar = Calendar.getInstance();

        date.setOnClickListener((View.OnClickListener) v -> new DatePickerDialog(DailyReportDetailTabs.this, (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dateFormat = "dd-MM-yyyy";
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            date.setText(sdf.format(myCalendar.getTime()));
        },
        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
        myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            title.setText(Html.fromHtml(s_title, Html.FROM_HTML_MODE_LEGACY));
            content.setText(Html.fromHtml(s_content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            title.setText(Html.fromHtml(s_title));
            content.setText(Html.fromHtml(s_content));
        }
    }

    public void filterReport(View view) {
        showLoading();
        param_date = String.valueOf(date.getText());
        Log.e("URL", param_date);

        String url = s_report;
        getReport(url, param_date);
    }

    public void getReport(String url, String date) {
        showLoading();
        StringRequest stringRequest = new StringRequest(url+"/"+date, response -> {
            Log.e("URL", url);
            if(sessionManager.getEnablePrinter().equals("on")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    printText(jsonObject.getString("header_invoice"), jsonObject.getString("invoice"));
                    Intent intent = new Intent(this, DailyReportDetailTabs.class);
                    intent.putExtra("title", jsonObject.getString("header_invoice"));
                    intent.putExtra("content", jsonObject.getString("invoice"));
                    intent.putExtra("report", url);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            hideLoading();
        }, error -> {
        });
        RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);
    }

    public void getDailyReport(View view) {
        showLoading();
        StringRequest stringRequest = new StringRequest(URI.API_DAILY_REPORT(sessionManager.getPathUrl())+sessionManager.getId()+param_date, response -> {
            Log.e("URL", response);
            if(sessionManager.getEnablePrinter().equals("on")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    printText(jsonObject.getString("header_invoice"), jsonObject.getString("invoice"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            hideLoading();
        }, error -> {
        });
        RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);
    }

    public void getDailyReportProduct(View view) {
        showLoading();
        StringRequest stringRequest = new StringRequest(URI.API_DAILY_PRODUCT_REPORT(sessionManager.getPathUrl())+sessionManager.getOutlet()+param_date, response -> {
            Log.e("URL", response);
            if(sessionManager.getEnablePrinter().equals("on")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    printText(jsonObject.getString("header_invoice"), jsonObject.getString("invoice"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            hideLoading();
        }, error -> {
        });
        RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);
    }

    public void printText(String header_invoice, String invoice) {
        if(sessionManager.getEnablePrinter().equals("on")) {
            String header = header_invoice;
            String body = invoice;
            MyApplication.getApplication().dailyReport(header, body);
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
        }
    }

    public void showLoading() {
        loadingDialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        loadingDialog.show();
    }
    public void hideLoading() {
        loadingDialog.dismiss();
    }

    private void getLogo() {
        //Log.e("URL_", URI.API_LOGO((sessionManager.getPathUrl()));
        StringRequest stringRequest = new StringRequest(URI.API_LOGO(sessionManager.getPathUrl()), response -> {
            if(response.isEmpty()) {
                icLogo.setImageResource(R.mipmap.ic_logo_foreground);
            } else {
                Glide
                        .with(this)
                        .load(response)
                        .fitCenter()
                        .into(icLogo);
            }
        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
        finish();
    }
}