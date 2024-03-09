package id.latenight.creativepos.mobile;

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

import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class InventoryReceiveDetailActivity extends AppCompatActivity {

    private String receive_id;
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
        setContentView(R.layout.activity_inventory_receive_detail);

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
            receive_id = extras.getString("receive_id");
            Log.e("receive_id", receive_id);
            getReceiveDetail(receive_id);
        }
    }

    @SuppressLint("SetTextI18n")
    private void getReceiveDetail(String receive_id) {
        Log.e("URL_", URI.API_INVENTORY_RECEIVE_DETAIL(session.getPathUrl())+receive_id);
        stringRequest = new StringRequest(URI.API_INVENTORY_RECEIVE_DETAIL(session.getPathUrl())+receive_id, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                Log.e("RESPONSE", jsonObject.toString());

                TextView receiveDate = findViewById(R.id.receive_date);
                TextView receiveCode = findViewById(R.id.receive_code);
                TextView totalItem = findViewById(R.id.total_item);
                TextView notes = findViewById(R.id.notes);

                receiveDate.setText(jsonObject.getString("date"));
                receiveCode.setText(jsonObject.getString("transfer_unique_id"));
                totalItem.setText(jsonObject.getString("total_qty"));
                notes.setText(jsonObject.getString("notes"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    @SuppressLint("InflateParams") View tableRow = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_receive_item,null,false);
                    TextView numbering = tableRow.findViewById(R.id.number);
                    TextView itemName  = tableRow.findViewById(R.id.item_name);
                    TextView itemCode  = tableRow.findViewById(R.id.item_code);
                    TextView qty  = tableRow.findViewById(R.id.qty);

                    String item_name = jo.getString("item_name");
                    String item_code = jo.getString("item_code");
                    int item_qty = jo.getInt("item_qty");

                    numbering.setText(String.valueOf(i+1)+".");

                    itemName.setText(Html.fromHtml(item_name));
                    itemCode.setText(item_code);
                    qty.setText(formatRupiah.format(item_qty).replace(',', '.'));
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
        intent.putExtra("receive_id", receive_id);
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