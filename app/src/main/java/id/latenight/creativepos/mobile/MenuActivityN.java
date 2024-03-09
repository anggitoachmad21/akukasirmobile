package id.latenight.creativepos.mobile;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.CartAdapter;
import id.latenight.creativepos.adapter.ProductAdapter;
import id.latenight.creativepos.adapter.ProductAdapterN;
import id.latenight.creativepos.model.Cart;
import id.latenight.creativepos.model.Product;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;
import pub.devrel.easypermissions.EasyPermissions;

public class MenuActivityN extends AppCompatActivity implements ProductAdapterN.ImageAdapterListener{
    private List<Product> list_product;
    private List<Cart> list_cart;
    private ArrayList<String> outletList, customerList, tableList, categoryList;
    private ArrayAdapter<String> CustomerAdapter;

    private  SessionManager sessionManager;
    private ProductAdapterN productAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_theme_2);

        RecyclerView recyclerMenu = findViewById(R.id.recycler_menu);
        //recyclerMenu.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list_product = new ArrayList<>();
        customerList = new ArrayList<>();
        tableList = new ArrayList<>();
        categoryList = new ArrayList<>();
        productAdapter = new ProductAdapterN(list_product, getApplicationContext(), this);
        recyclerMenu.setAdapter(productAdapter);

        sessionManager = new SessionManager(this);

        menus();
    }

    public void menus()
    {
        String params = "/0/umum/0/0/0";
        Log.e("URL", URI.API_MENU(sessionManager.getPathUrl())+sessionManager.getId()+params);
        StringRequest stringRequest = new StringRequest(URI.API_MENU(sessionManager.getPathUrl())+sessionManager.getId()+params, response -> {
            try {
                JSONArray jsonObj = new JSONArray(response);
                Log.e("status", String.valueOf(jsonObj));
                for (int i=0; i<jsonObj.length(); i++)
                {
                    JSONObject jsonObject = jsonObj.getJSONObject(i);
                    Product listData = new Product(jsonObject.getInt("id"), jsonObject.getString("photo"), jsonObject.getString("name"), jsonObject.getString("category_name"), jsonObject.getString("label_name"), jsonObject.getInt("sale_price"), jsonObject.getInt("reseller_price"), jsonObject.getInt("outlet_price"), jsonObject.getInt("ingredient_stock"), jsonObject.getString("sku_number"), 0);
                    list_product.add(listData);
                }
                productAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }, error -> {

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onImageSelected(Product item) {

    }

    @Override
    public void onRemove(Product item) {

    }

    @Override
    public void onUpdateQty(Product item, int qty) {

    }
}
