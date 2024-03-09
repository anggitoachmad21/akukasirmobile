package id.latenight.creativepos.mobile;

import androidx.appcompat.app.AppCompatActivity;

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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class PurchaseOrderDetailActivity extends AppCompatActivity {

    private String purchase_id;
    private SessionManager session;
    private StringRequest stringRequest;
    private RequestQueue requestQueue;
    private NumberFormat formatRupiah;
    private TableLayout tableLayout;

    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private ProgressDialog loadingDialog;
    private Animation slideUp, slideDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_order_detail);

        session = new SessionManager(this);
        formatRupiah = NumberFormat.getInstance();
        Bundle extras = getIntent().getExtras();

        tableLayout = findViewById(R.id.tableLayout);

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
            purchase_id = extras.getString("purchase_id");
            Log.e("purchase_id", purchase_id);
            getPurchaseDetail(purchase_id);
        }
    }

    @SuppressLint("SetTextI18n")
    private void getPurchaseDetail(String purchase_id) {
        Log.e("URL_", URI.API_PURCHASE_DETAIL(session.getPathUrl())+purchase_id);
        stringRequest = new StringRequest(URI.API_PURCHASE_DETAIL(session.getPathUrl())+purchase_id, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                Log.e("RESPONSE", jsonObject.toString());

                TextView purchaseNumber = findViewById(R.id.purchase_number);
                TextView supplier = findViewById(R.id.supplier);
                TextView notes = findViewById(R.id.notes);
                TextView totalPurchase = findViewById(R.id.total_purchase);
                TextView outlet = findViewById(R.id.outlet);

                purchaseNumber.setText(jsonObject.getString("reference_no"));
                supplier.setText(jsonObject.getString("supplier_name"));
                notes.setText(jsonObject.getString("note"));
                totalPurchase.setText(jsonObject.getString("grand_total"));
                outlet.setText(jsonObject.getString("outlet_name"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    @SuppressLint("InflateParams") View tableRow = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_purchase_item,null,false);
                    TextView numbering = tableRow.findViewById(R.id.number);
                    TextView itemName  = tableRow.findViewById(R.id.item_name);
                    TextView itemPrice  = tableRow.findViewById(R.id.item_price);
                    TextView itemTotal  = tableRow.findViewById(R.id.item_total);

                    String item_name = jo.getString("name");
                    int item_price = jo.getInt("unit_price");
                    int item_qty = jo.getInt("quantity_amount");
                    int item_total = jo.getInt("total");

                    numbering.setText(String.valueOf(i+1)+".");

                    itemName.setText(Html.fromHtml(item_name));
                    itemPrice.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(item_price).replace(',', '.') + " x " + formatRupiah.format(item_qty).replace(',', '.'));
                    itemTotal.setText(getResources().getString(R.string.currency) +" "+ formatRupiah.format(item_total).replace(',', '.'));
                    tableLayout.addView(tableRow);
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
        intent.putExtra("purchase_id", purchase_id);
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