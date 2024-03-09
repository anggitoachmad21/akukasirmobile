package id.latenight.creativepos.tabs;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.latenight.creativepos.R;
import id.latenight.creativepos.tabs.MenuActivityTabs;
import id.latenight.creativepos.util.DeviceActivity;
import id.latenight.creativepos.util.MyApplication;
import id.latenight.creativepos.util.MyBluetoothPrintersConnections;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

import static id.latenight.creativepos.util.MyApplication.RC_ENABLE_BLUETOOTH;
import static id.latenight.creativepos.util.MyApplication.RC_CONNECT_DEVICE;

public class AdminActivityTabs extends AppCompatActivity implements View.OnClickListener {

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ProgressDialog loadingDialog;
    private SessionManager sessionManager;
    private TextView pairDevice;

    private ProgressBar progressBar;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private RadioButton scanPrinter;
    private RadioButton resetPassword,
            closeChasier, summaryReport, summaryReportGraphic,
            summaryReportDailyUser, deleteSales, summaryReportDaily, summaryReportStore, incomeStatement;

    private LinearLayout lyt_admin ,lyt_admin2, lyt_admin3, lyt_report;
    private Animation slideUp, slideDown;

    private BluetoothConnection connection;

    private boolean is_tabs;

    private EditText excep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        sessionManager = new SessionManager(this);

        if(sessionManager.getMobileType().equals("mobile")){
            is_tabs = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            is_tabs = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        String printerAddress = sessionManager.getPrinter();
        Log.e("Printer", printerAddress);
        Log.e("Enabler", sessionManager.getEnablePrinter());

//        Button deleteSales = findViewById(R.id.delete_sales);
        pairDevice = findViewById(R.id.pair_device);

        scanPrinter = findViewById(R.id.scan_printer);
        scanPrinter.setOnClickListener(this);

        progressBar = findViewById(R.id.progressBar);

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);

        excep = findViewById(R.id.exception);

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        int isConnected = MyApplication.getApplication().isConnected();
        if(!sessionManager.getPrinter().isEmpty()) {
            if(isConnected != 3) {
                //MyApplication.getApplication().setupBluetoothConnection(sessionManager.getPrinter());
                Log.e("Check Connection", String.valueOf(isConnected));
            } else {
                pairDevice.setText(getResources().getString(R.string.connected_with_printer));
            }
        }

        if(mBluetoothAdapter == null) {
            pairDevice.setText(getResources().getString(R.string.turn_on_bluetooth));
            sessionManager.setEnablePrinter("off");
        }

        RadioButton closeCashier = findViewById(R.id.close_cashier);
        closeCashier.setOnClickListener(this);

        RadioButton testPrint = findViewById(R.id.test_print);
        testPrint.setOnClickListener(v -> {
            String body = "Printer Ready";
            MyApplication.getApplication().testPrint(body);
        });

        Switch enablePrint = findViewById(R.id.enable_print);

        if (sessionManager.getEnablePrinter().equals("on")) {
            enablePrint.setChecked(true);
        }

        enablePrint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
                sessionManager.setEnablePrinter("on");
                Log.e("Enable", "ENABLE");
            }
            else {
                Log.e("Enable", "DISABLE");
                sessionManager.setEnablePrinter("off");
            }
        });


//        Button incomeStatement = findViewById(R.id.income_statement);
//        Button summaryReportStore = findViewById(R.id.summary_report_store);
//      Button summaryReportAllOutlet = findViewById(R.id.summary_report_all_outlet);
//      Button summaryReportGraphic = findViewById(R.id.summary_report_graphic);
        resetPassword = findViewById(R.id.reset_password);
        closeChasier = findViewById(R.id.close_cashier);
        summaryReport = findViewById(R.id.summary_report_all_outlet);
        summaryReportGraphic = findViewById(R.id.summary_report_graphic);
        summaryReportDaily = findViewById(R.id.summary_report_daily);
        summaryReportStore = findViewById(R.id.summary_report_store);
        incomeStatement = findViewById(R.id.income_statement);
        RadioButton summaryReportDailys = findViewById(R.id.summary_report_dailys);
        deleteSales = findViewById(R.id.delete_sales);

        resetPassword.setOnClickListener(this);
        closeCashier.setOnClickListener(this);
        summaryReport.setOnClickListener(this);
        summaryReportGraphic.setOnClickListener(this);
        summaryReportDaily.setOnClickListener(this);
        summaryReportStore.setOnClickListener(this);

        summaryReportDaily.setOnClickListener(this);
        incomeStatement.setOnClickListener(this);
        deleteSales.setOnClickListener(this);

        lyt_admin = findViewById(R.id.lyt_admin);
        lyt_admin2 = findViewById(R.id.lyt_admin2);
        lyt_admin3 = findViewById(R.id.lyt_admin3);
        Log.e("Role", sessionManager.getRole());
        if(sessionManager.getRole().equals("Admin")) {
            lyt_admin.setVisibility(View.VISIBLE);
            lyt_admin2.setVisibility(View.VISIBLE);
            lyt_admin3.setVisibility(View.VISIBLE);
            lyt_report = findViewById(R.id.lyt_summary_report_daily_user);
            lyt_report.setVisibility(View.GONE);
//            incomeStatement.setVisibility(View.VISIBLE);
//            summaryReportAllOutlet.setVisibility(View.VISIBLE);
//            summaryReportGraphic.setVisibility(View.VISIBLE);
        }
        RadioButton rangkumanDaily = findViewById(R.id.summary_report_dailys);
        rangkumanDaily.setOnClickListener(this);

    }

    public void resetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivityTabs.class);
        startActivity(intent);
    }

    public void getDailyReportProduct(View view) {
        showLoading();
        StringRequest stringRequest = new StringRequest(URI.API_DAILY_PRODUCT_REPORT_LIST(sessionManager.getPathUrl())+sessionManager.getOutlet(), response -> {
            Log.e("URL", response);
            if(sessionManager.getEnablePrinter().equals("on")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    printText(jsonObject.getString("header_invoice"), jsonObject.getString("invoice"));
                    Intent intent = new Intent(this, DailyReportDetailTabs.class);
                    intent.putExtra("title", jsonObject.getString("header_invoice"));
                    intent.putExtra("content", jsonObject.getString("invoice"));
                    intent.putExtra("report", URI.API_DAILY_PRODUCT_REPORT_LIST(sessionManager.getPathUrl())+sessionManager.getOutlet());
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
        StringRequest stringRequest = new StringRequest(URI.API_DAILY_REPORT_LIST(sessionManager.getPathUrl())+sessionManager.getId(), response -> {
            Log.e("URL", response);
            if(sessionManager.getEnablePrinter().equals("on")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    printText(jsonObject.getString("header_invoice"), jsonObject.getString("invoice"));
                    Intent intent = new Intent(this, DailyReportDetailTabs.class);
                    intent.putExtra("title", jsonObject.getString("header_invoice"));
                    intent.putExtra("content", jsonObject.getString("invoice"));
                    intent.putExtra("report", URI.API_DAILY_REPORT_LIST(sessionManager.getPathUrl())+sessionManager.getId());
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

    public void summaryReport() {
        Intent intent = new Intent(this, SummaryReportActivityTabs.class);
        startActivity(intent);
    }

    public void summaryReportGraphic() {
        Intent intent = new Intent(this, SummaryReportGraphicActivityTabs.class);
        startActivity(intent);
    }

    public void summaryReportAllOutlet() {
        Intent intent = new Intent(this, SummaryReportAllOutletActivityTabs.class);
        startActivity(intent);
    }

    public void summaryReportDaily() {
        Intent intent = new Intent(this, SummaryReportActivityTabs.class);
        startActivity(intent);
    }

    public void incomeStatement() {
        Intent intent = new Intent(this, IncomeStatementActivityTabs.class);
        startActivity(intent);
    }

    public void deleteSales() {
        Intent intent = new Intent(this, DeleteSalesActivityTabs.class);
        startActivity(intent);
    }

    public void closeCashier() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI.BALANCE_CLOSE_REGISTER(sessionManager.getPathUrl()),
                response -> {
                    Log.e("RESPONSE", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        if(success.equals("true")) {
                            sessionManager.setOpenRegistration("0");
                            Intent intent = new Intent(getApplicationContext(), OpenRegisterActivityTabs.class);
                            intent.putExtra("closed", "true");
                            startActivity(intent);
                            finish();
                        } else {
                            showError(getResources().getString(R.string.error_message));
                        }

                    } catch (JSONException e) {
                        showError(getResources().getString(R.string.error_message));
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
                        showError("Anda tidak dapat menutup laci kasir");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:

                params.put("user_id", sessionManager.getId());
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    Log.i("TAG", "onActivityResult: bluetooth aktif");
                    pairDevice.setText(getResources().getString(R.string.disconnected_with_printer));
                } else {
                    Log.i("TAG", "onActivityResult: bluetooth harus aktif untuk menggunakan fitur ini");
                }
                break;
            case RC_CONNECT_DEVICE:
                Log.e("resultCode", String.valueOf(resultCode));
                Log.e("RESULT_OK", String.valueOf(RESULT_OK));
                if (resultCode == RESULT_OK) {
                    int isConnected = MyApplication.getApplication().isConnected();
                    if(isConnected != 3) {
                        Log.e("Check Connection", String.valueOf(isConnected));
                        String address = data.getExtras().getString(DeviceActivity.EXTRA_DEVICE_ADDRESS);
                        MyApplication.getApplication().setupBluetoothConnection(address);
                        pairDevice.setText(getResources().getString(R.string.connected_with_printer));
                    }
                }
                break;
        }
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

    public void scanPrinter() {
        connection = MyBluetoothPrintersConnections.selectFirstPaired();
        if(connection == null)
        {
            startActivity(new Intent().setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
        }
        else{
            Toast.makeText(this, "Printer Terhubung", Toast.LENGTH_SHORT).show();
        }
//        if(sessionManager.getEnablePrinter().equals("on")) {
//            startActivityForResult(new Intent(this, DeviceActivity.class), RC_CONNECT_DEVICE);
//        } else {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
//        }
    }

    public void showLoading() {
        loadingDialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        loadingDialog.show();
    }
    public void hideLoading() {
        loadingDialog.dismiss();
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(this, MenuActivityTabs.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scan_printer) {
            scanPrinter();
        }
        if (v.getId() == R.id.reset_password) {
            resetPassword();
        }
        if (v.getId() == R.id.summary_report_all_outlet) {
            summaryReportAllOutlet();
        }
        if (v.getId() == R.id.close_cashier) {
            closeCashier();
        }
        if (v.getId() == R.id.summary_report_graphic) {
            summaryReportGraphic();
        }
        if (v.getId() == R.id.summary_report_daily) {
            summaryReportDaily();
        }
        if (v.getId() == R.id.summary_report_store) {
            summaryReport();
        }
        if (v.getId() == R.id.summary_report_dailys) {
            summaryReportDaily();
        }
        if (v.getId() == R.id.income_statement) {
            incomeStatement();
        }
        if (v.getId() == R.id.delete_sales) {
            deleteSales();
        }
        if (v.getId() == R.id.summary_report_dailys) {
            summaryReportDaily();
        }
    }
}
