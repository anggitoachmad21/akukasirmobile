package id.latenight.creativepos.mobile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import id.latenight.creativepos.BuildConfig;
import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class IncomeStatementActivity extends AppCompatActivity {

    private EditText startDate, endDate;
    private String param_start_date="", param_end_date = "", param_outlet_id="";
    private SearchableSpinner spinnerOutlet;
    private ArrayList<String> outletSpinnerList;
    private ProgressBar progressBar;
    private SessionManager session;
    private WebView webView;
    SwipeRefreshLayout swLayout;
    String url;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_statement);

        session = new SessionManager(this);

        LinearLayout lytFilter = findViewById(R.id.lyt_filter);
        if(session.getRole().equals("Admin")) {
            lytFilter.setVisibility(View.VISIBLE);
        }

        spinnerOutlet = findViewById(R.id.spinner_outlet);
        spinnerOutlet.setTitle(getResources().getString(R.string.select_outlet));
        outletSpinnerList = new ArrayList<>();

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new myWebclient());
        webView.getSettings().setJavaScriptEnabled(true);
        url = URI.API_INCOME_STATEMENT(session.getPathUrl())+session.getId() + "?outlet="+param_outlet_id+"&start_date=" + param_start_date + "&end_date=" + param_end_date;
        webView.loadUrl(url);
        progressBar = findViewById(R.id.progressBar);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        swLayout = findViewById(R.id.swlayout);
        swLayout.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary);
        swLayout.setOnRefreshListener(() -> {
            webView.reload();
        });

        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        ImageButton filter = findViewById(R.id.filter);

        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select date");
        MaterialDatePicker<Pair<Long, Long>> pickerRange = materialDateBuilder.build();

        startDate.setOnClickListener(v -> pickerRange.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        endDate.setOnClickListener(v -> pickerRange.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));

        pickerRange.addOnPositiveButtonClickListener(selection -> {
            Pair selectedDates = pickerRange.getSelection();
            final Pair<Date, Date> rangeDate = new Pair<>(new Date((Long) selectedDates.first), new Date((Long) selectedDates.second));
            Date start_date = rangeDate.first;
            Date end_date = rangeDate.second;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleFormat = new SimpleDateFormat("dd-MM-yyyy");
            startDate.setText(simpleFormat.format(start_date));
            endDate.setText(simpleFormat.format(end_date));

            param_start_date = (simpleFormat.format(start_date));
            param_end_date = (simpleFormat.format(end_date));
        });

        filter.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if(!spinnerOutlet.getSelectedItem().toString().equals(getResources().getString(R.string.select_outlet))) {
                param_outlet_id = spinnerOutlet.getSelectedItem().toString();
            } else {
                param_outlet_id = "";
            }
            url = URI.API_INCOME_STATEMENT(session.getPathUrl())+session.getId() + "?outlet="+param_outlet_id+"&start_date=" + param_start_date + "&end_date=" + param_end_date;
            Log.e("URL", url);
            webView.loadUrl(url);
        });

        getOutlets();
    }

    public class myWebclient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            swLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    public Bitmap createBitmapFromView(View view) {
        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        //Create a canvas with the specified bitmap to draw into
        Canvas c = new Canvas(bitmap);

        //Render this view (and all of its children) to the given Canvas
        view.draw(c);
        return bitmap;
    }

    public void shareInvoice(View view) {
        ProgressDialog dialog = ProgressDialog.show(IncomeStatementActivity.this, "",
                "Loading. Please wait...", true);
        dialog.show();
        Bitmap bm = createBitmapFromView(webView);

        // Get access to the URI for the bitmap
        if (bm != null) {
            try {
                File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
                FileOutputStream fOut = new FileOutputStream(file);

                bm.compress(Bitmap.CompressFormat.PNG, 50, fOut);
                fOut.flush();
                fOut.close();
                bm.recycle();
                Uri bmpUri = FileProvider.getUriForFile(IncomeStatementActivity.this, BuildConfig.APPLICATION_ID , file);
                // Construct a ShareIntent with link to image
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/*");
                // Launch sharing dialog for image
                startActivity(Intent.createChooser(shareIntent, "Share Image"));
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                dialog.dismiss();
            }
        }
    }

    private void getOutlets() {
        request = new JsonArrayRequest(URI.API_OUTLETS_LIST(session.getPathUrl()), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            outletSpinnerList.add(""+getResources().getString(R.string.select_outlet)+"");
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    outletSpinnerList.add(jsonObject.getString("outlet_name"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerOutlet.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, outletSpinnerList));
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode==KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
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
