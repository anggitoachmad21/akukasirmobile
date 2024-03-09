package id.latenight.creativepos.tabs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

import id.latenight.creativepos.mobile.PurchaseOrderActivity;
import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class ProductionDetailActivityTabs extends AppCompatActivity {

    private String production_id;
    private SessionManager session;
    private StringRequest stringRequest;
    private RequestQueue requestQueue;
    private NumberFormat formatRupiah;
    private TableLayout tableLayout, prTableLayout;

    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private ProgressDialog loadingDialog;
    private Animation slideUp, slideDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_detail);

        session = new SessionManager(this);
        formatRupiah = NumberFormat.getInstance();
        Bundle extras = getIntent().getExtras();

        tableLayout = findViewById(R.id.tableLayout);
        prTableLayout = findViewById(R.id.pr_tableLayout);

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);
        ImageButton btnCloseAlert = findViewById(R.id.btn_close_alert);
        btnCloseAlert.setOnClickListener(v -> lytAlert.setVisibility(View.GONE));
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        if (extras != null) {
            production_id = extras.getString("production_id");
            Log.e("production_id", production_id);
            getProductionDetail(production_id);
        }
    }

    @SuppressLint("SetTextI18n")
    private void getProductionDetail(String production_id) {
        Log.e("URL_", URI.API_PRODUCTION_DETAIL(session.getPathUrl())+production_id);
        stringRequest = new StringRequest(URI.API_PRODUCTION_DETAIL(session.getPathUrl())+production_id, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray pr_jsonArray = jsonObject.getJSONArray("items_pr");
                JSONArray jsonArray = jsonObject.getJSONArray("items_pi");
                Log.e("RESPONSE", jsonObject.toString());

                TextView productionDate = findViewById(R.id.production_date);
                TextView productName = findViewById(R.id.product_name);
                TextView totalProduction = findViewById(R.id.total_production);
                TextView notes = findViewById(R.id.notes);
                TextView piCode = findViewById(R.id.pi_code);
                TextView prCode = findViewById(R.id.pr_code);

                productionDate.setText(jsonObject.getString("date_formated"));
                productName.setText(jsonObject.getString("product_name"));
                totalProduction.setText(formatRupiah.format(jsonObject.getInt("prediction")).replace(',', '.'));
                notes.setText(jsonObject.getString("notes"));
                piCode.setText(jsonObject.getString("pi_code"));
                prCode.setText(jsonObject.getString("pr_code"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    @SuppressLint("InflateParams") View tableRow = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_production_item,null,false);
                    TextView numbering = tableRow.findViewById(R.id.number);
                    TextView itemName  = tableRow.findViewById(R.id.item_name);
                    TextView qty  = tableRow.findViewById(R.id.qty);

                    String item_name = jo.getString("name");
                    int item_qty = jo.getInt("consumption_amount");
                    String unit_name = jo.getString("unit_name");

                    numbering.setText(i + 1 +".");

                    itemName.setText(Html.fromHtml(item_name));
                    qty.setText(formatRupiah.format(item_qty).replace(',', '.') +" "+ unit_name);
                    tableLayout.addView(tableRow);
                }

                for (int i = 0; i < pr_jsonArray.length(); i++) {
                    JSONObject jo = pr_jsonArray.getJSONObject(i);
                    @SuppressLint("InflateParams") View tableRow = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_production_item,null,false);
                    TextView numbering = tableRow.findViewById(R.id.number);
                    TextView itemName  = tableRow.findViewById(R.id.item_name);
                    TextView qty  = tableRow.findViewById(R.id.qty);

                    String item_name = jo.getString("name");
                    int item_qty = jo.getInt("consumption_amount");
                    String unit_name = jo.getString("unit_name");

                    numbering.setText(i + 1 +".");

                    itemName.setText(Html.fromHtml(item_name));
                    qty.setText(formatRupiah.format(item_qty).replace(',', '.') +" "+ unit_name);
                    prTableLayout.addView(tableRow);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void updatePurchase(View view) {
        Intent intent = new Intent(this, PurchaseOrderActivity.class);
        intent.putExtra("production_id", production_id);
        startActivity(intent);
    }

    public void showLoading() {
        loadingDialog = ProgressDialog.show(this, "",
                "Mohon tunggu...", true);
        loadingDialog.show();
    }
    public void hideLoading() {
        loadingDialog.dismiss();
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
        lytAlert.setBackgroundResource(R.drawable.alert_error);
        txtAlert.setText(message);
        txtAlert.setTextColor(getResources().getColor(R.color.error));
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