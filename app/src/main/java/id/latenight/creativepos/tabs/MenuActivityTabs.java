package id.latenight.creativepos.tabs;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BlendMode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import id.latenight.creativepos.BuildConfig;
import id.latenight.creativepos.mobile.LoginActivity;
import id.latenight.creativepos.mobile.OpenRegisterActivity;
import id.latenight.creativepos.R;
import id.latenight.creativepos.util.MyApplication;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class MenuActivityTabs extends AppCompatActivity implements View.OnClickListener {

    private SessionManager session;
    private boolean doubleBackToExitPressedOnce = false;
    private ProgressDialog loadingDialog;
    String _paramUsername = "", _versionMobile = "", _versionName = "";

    File file = null;

    private TextView total_sales, total_product, versionText;
    public static final int REQUEST_PERMISSION_CODE = 1;

    private boolean is_tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        session = new SessionManager(this);

        if(session.getEnablePrinter().equals("")) {
            session.setEnablePrinter("on");
        }

        if(session.getEnablePrinter().equals("on")) {
            session.setEnablePrinter("on");
        }

        if(!session.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        RadioButton cashier = findViewById(R.id.cashier);
        ImageView chasiers = findViewById(R.id.cashiers);
        RadioButton orderHistory = findViewById(R.id.order_history);
        RadioButton admin = findViewById(R.id.admin);
        RadioButton expense = findViewById(R.id.expense);
        RadioButton purchase = findViewById(R.id.purchase);
        RadioButton inventoryReceive = findViewById(R.id.inventory_receive);
        RadioButton inventoryProduction = findViewById(R.id.inventory_production);
        RadioButton inventory = findViewById(R.id.inventory);
        RadioButton inventoryTransfer = findViewById(R.id.inventory_transfer);
        RadioButton konveriStok = findViewById(R.id.konversi_stok);
        TextView username = findViewById(R.id.username);

        LinearLayout lytCashier = findViewById(R.id.lyt_cashier);
        LinearLayout lytHistory = findViewById(R.id.lyt_history);
        LinearLayout lytAdmin = findViewById(R.id.lyt_admin);
        LinearLayout lytExpense = findViewById(R.id.lyt_expense);
        LinearLayout lytPurchase = findViewById(R.id.lyt_purchase);
        LinearLayout lytInventoryReceive = findViewById(R.id.lyt_inventory_receive);
        LinearLayout lytProduction = findViewById(R.id.lyt_production);
        LinearLayout lytInventory = findViewById(R.id.lyt_inventory);
        LinearLayout lytInventoryTransfer = findViewById(R.id.lyt_inventory_transfer);

        ImageView btnLogout = findViewById(R.id.logout);

        if(session.getRole().equals("Admin")) {
            //lytCashier.setVisibility(View.GONE);
            //lytHistory.setVisibility(View.GONE);
            //lytInventoryReceive.setVisibility(View.GONE);
            //lytProduction.setVisibility(View.GONE);
            lytInventory.setVisibility(View.VISIBLE);
        }

        username.setText(session.getFullname());

        total_sales = findViewById(R.id.total_sales);
        total_product = findViewById(R.id.product_sell);
        versionText = findViewById(R.id.versionText);

        versionText.setText("Version "+ BuildConfig.VERSION_NAME);

        cashier.setOnClickListener(this);
        chasiers.setOnClickListener(this);
        orderHistory.setOnClickListener(this);
        admin.setOnClickListener(this);
        expense.setOnClickListener(this);
        purchase.setOnClickListener(this);
        inventoryReceive.setOnClickListener(this);
        inventoryProduction.setOnClickListener(this);
        inventory.setOnClickListener(this);
        inventoryTransfer.setOnClickListener(this);
        konveriStok.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        if(session.getOpenRegistration().isEmpty() || session.getOpenRegistration().equals("0")) {
            checkOpenRegistration(session.getId());
            Log.e("cek balance", session.getOpenRegistration());
        } else {
            if(!session.getOpenRegistration().equals("1")) {
                Intent intent = new Intent(this, OpenRegisterActivity.class);
                startActivity(intent);
                finish();
            }
        }

        int isConnected = MyApplication.getApplication().isConnected();
        if(!session.getPrinter().isEmpty()) {
            if(isConnected != 3) {
                //MyApplication.getApplication().setupBluetoothConnection(session.getPrinter());
                Log.e("Check Connection", String.valueOf(isConnected));
            }
        }
    }

    public void showLoading() {
        loadingDialog = ProgressDialog.show(this, "",
                "Mohon tunggu...", true);
        loadingDialog.show();
    }

    public void showLoadingDownload() {
        loadingDialog = ProgressDialog.show(this, "",
                "Sedang Mendownload...", true);
        loadingDialog.show();
    }
    public void hideLoading() {
        loadingDialog.dismiss();
    }

    @Override
    protected void onResume() {
        getTotalSales();
        getUpdate();
        super.onResume();
    }

    public void hideLoadingDownload() {
        loadingDialog.dismiss();
    }

    private void checkOpenRegistration(String id) {
        showLoading();
        StringRequest stringRequest = new StringRequest(URI.CHECK_OPEN_REGISTER(session.getPathUrl())+id, response -> {
            if(response.equals("0")) {
                Intent intent = new Intent(this, OpenRegisterActivity.class);
                startActivity(intent);
                finish();
            }
            session.setOpenRegistration(response);
            hideLoading();
        }, error -> {
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void getTotalSales()
    {
        StringRequest stringRequest = new StringRequest(URI.API_GET_TOTAL_SALES_TODAY(session.getPathUrl()), response -> {
            total_sales.setText("Rp. "+ response.replace('"', ' '));
            getSell();
        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void getSell()
    {
        StringRequest stringRequest = new StringRequest(URI.API_GET_PRODUCT_SALES_TODAY(session.getPathUrl()), response -> {
            total_product.setText(response.replace('"', ' '));
        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void getUpdate()
    {
        Log.e("URL LINK GET", URI.GET_APK_VERSION()+"?url="+session.getPathUrl()+"&type="+session.getMobileType());
        StringRequest stringRequest = new StringRequest(URI.GET_APK_VERSION()+"?url="+session.getPathUrl()+"&type="+session.getMobileType(), response -> {
            try {
                JSONObject jsonObj = new JSONObject(String.valueOf(response));
                Log.e("status", String.valueOf(jsonObj));
                if(jsonObj.getString("status").equals("hasChanged")) {
                    _versionMobile = jsonObj.getString("apk_version");
                    _versionName = jsonObj.getString("apk_name");
                    checkUpdate();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    public void checkUpdate()
    {
        if (checkPermissions()) {
            String fileName = _versionName;
            String url = URI.GET_APK_DOWNLOAD()+"?type="+session.getMobileType()+"&url="+session.getPathUrl();
            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
            destination += fileName;
            String updateDestination = destination;
            final Uri uri_current = Uri.parse("file://" + destination);

            file = new File(destination);
            if (!BuildConfig.VERSION_NAME.equals(_versionMobile)) {

                if (!file.exists()) {
                    new AlertDialog.Builder(MenuActivityTabs.this)
                            .setMessage("Tersedia Update Terbaru")
                            .setPositiveButton("Download Sekarang", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                    downloadUpdate();
                                }
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                } else {
                    new AlertDialog.Builder(MenuActivityTabs.this)
                            .setMessage("Update Aplikasi Terbaru")
                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    install_aplikasi();
                                }
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                }
            }
        }
        else{
            requestPermissions();
        }
    }

    public boolean checkPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MenuActivityTabs.this, new String[]{READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        if (v.getId() == R.id.cashier) {
            intent = new Intent(this, MainActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.cashiers) {
            intent = new Intent(this, MainActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.order_history) {
            intent = new Intent(this, OrderRunningActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.admin) {
            intent = new Intent(this, AdminActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.expense) {
            intent = new Intent(this, ExpenseActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.purchase) {
            intent = new Intent(this, PurchaseOrderListActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.inventory_receive) {
            intent = new Intent(this, InventoryReceiveListActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.inventory_production) {
            intent = new Intent(this, ProductionListActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.inventory) {
            intent = new Intent(this, MenuInventoryActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.inventory_transfer) {
            intent = new Intent(this, InventoryTransferListActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.konversi_stok) {
            intent = new Intent(this, ConversionActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.logout) {
            session.setLogin(false);
            intent = new Intent(this, LoginActivityTabs.class);
            startActivity(intent);
            finish();
        }
    }


    public void downloadUpdate()
    {
        String fileName = _versionName;
        String url = URI.GET_APK_DOWNLOAD()+"?type="+session.getMobileType()+"&url="+session.getPathUrl();
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        destination += fileName;
        String updateDestination = destination;
        final Uri uri_current = Uri.parse("file://" + destination);

        file = new File(destination);
        if (file.exists()) {
            file.delete();
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.addRequestHeader("Authorization", "Basic simplebase64tokenherefornexus");
        request.setDescription("App Update");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setTitle(fileName);
        request.setDestinationUri(uri_current);

        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        showLoadingDownload();
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                hideLoading();
                install_aplikasi();
                unregisterReceiver(this);
            }
        };
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void install_aplikasi()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (!getPackageManager().canRequestPackageInstalls()) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), 1234);
            } else {
                Uri uri = FileProvider.getUriForFile(this, "com.akukasir.mobile", new File(file.getAbsolutePath()));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(uri, "application/vnd.android" + ".package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        }
        else{
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android" + ".package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }


    public void logout(View view) {
        session.setLogin(false);
        Intent intent = new Intent(this, LoginActivityTabs.class);
        startActivity(intent);
        finish();
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
