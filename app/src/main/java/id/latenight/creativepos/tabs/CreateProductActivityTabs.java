package id.latenight.creativepos.tabs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.IngredientAdapter;
import id.latenight.creativepos.model.Ingredient;
import id.latenight.creativepos.util.SearchableSpinner;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class CreateProductActivityTabs extends AppCompatActivity implements IngredientAdapter.AdapterListener {

    private int product_id;
    private boolean update_product = false;
    private String encodedImageString="", param_outlet_name="", param_category_name="", param_main_category_name="";
    private EditText productName, skuProduct, salePrice, resellerPrice, onlinePrice;
    private SearchableSpinner spinnerIngredient, spinnerOutlet, spinnerMainCategory, spinnerCategory;
    private ArrayList<String> ingredientSpinnerList, outletSpinnerList, mainCategorySpinnerList, categorySpinnerList;
    private RelativeLayout lytAlert;
    private TextView txtAlert;
    private SessionManager session;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private List<Ingredient> list_ingredient;
    private RecyclerView recyclerIngredient;
    private IngredientAdapter ingredientAdapter;
    private ProgressDialog loadingDialog;
    private ImageView photo;
    private ImageButton btnPhoto;

    private Animation slideUp, slideDown;

    private final int RESULT_LOAD_IMG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product);

        session = new SessionManager(this);

        productName = findViewById(R.id.product_name);
        skuProduct = findViewById(R.id.sku_number);
        salePrice = findViewById(R.id.sale_price);
        resellerPrice = findViewById(R.id.reseller_price);
        onlinePrice = findViewById(R.id.online_price);

        spinnerIngredient = findViewById(R.id.ingredient);
        ingredientSpinnerList = new ArrayList<>();
        spinnerIngredient.setTitle(getResources().getString(R.string.select_ingredient));

        spinnerOutlet = findViewById(R.id.spinner_outlet);
        outletSpinnerList = new ArrayList<>();
        spinnerOutlet.setTitle(getResources().getString(R.string.select_outlet));

        spinnerMainCategory = findViewById(R.id.spinner_main_category);
        mainCategorySpinnerList = new ArrayList<>();
        spinnerMainCategory.setTitle(getResources().getString(R.string.select_main_category));

        spinnerCategory = findViewById(R.id.spinner_category);
        categorySpinnerList = new ArrayList<>();
        spinnerCategory.setTitle(getResources().getString(R.string.select_product_category));

        recyclerIngredient = findViewById(R.id.recycler_ingredient);
        recyclerIngredient.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list_ingredient = new ArrayList<>();
        ingredientAdapter = new IngredientAdapter(list_ingredient, getApplicationContext(), this);
        recyclerIngredient.setAdapter(ingredientAdapter);

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> createProduct());

        lytAlert = findViewById(R.id.lyt_alert);
        txtAlert = findViewById(R.id.txt_alert);
        ImageButton btnCloseAlert = findViewById(R.id.btn_close_alert);
        btnCloseAlert.setOnClickListener(v -> lytAlert.setVisibility(View.GONE));
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // slide-up animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        photo = findViewById(R.id.photo);
        btnPhoto = findViewById(R.id.btn_photo);
        btnPhoto.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
        });

        getOutlets();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            product_id = extras.getInt("product_id");
        } else {
            getMainCategory();
            getCategory();
        }
    }

    private void getProductData(int product_id) {
        Log.e("URL_", URI.API_DETAIL_PRODUCT(session.getPathUrl())+product_id);
        StringRequest stringRequest = new StringRequest(URI.API_DETAIL_PRODUCT(session.getPathUrl())+product_id, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                Log.e("RESPONSE", jsonObject.toString());
                productName.setText(jsonObject.getString("name"));
                skuProduct.setText(jsonObject.getString("sku_number"));
                salePrice.setText(jsonObject.getString("sale_price"));
                resellerPrice.setText(jsonObject.getString("reseller_price"));
                onlinePrice.setText(jsonObject.getString("online_price"));

                param_outlet_name = jsonObject.getString("outlet_name");
                param_main_category_name = jsonObject.getString("main_category_name");
                param_category_name = jsonObject.getString("category_name");
                spinnerOutlet.setSelection(outletSpinnerList.indexOf(param_outlet_name));

                getMainCategory();
                getCategory();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    list_ingredient.add(new Ingredient(jo.getInt("id"), jo.getString("name"), 0, "unit", jo.getInt("consumption")));
                    ingredientAdapter.notifyDataSetChanged();
                }

                if (!jsonObject.getString("photo").equals("")) {
                    photo.setVisibility(View.VISIBLE);
                    Glide
                    .with(this)
                    .load(URI.PATH_IMAGE(session.getPathUrl()) + jsonObject.getString("photo"))
                    .centerCrop()
                    .placeholder(R.drawable.progress_animation)
                    .into(photo);
                }

                update_product = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void createProduct() {
        String value_product_name = productName.getText().toString();
        String value_sku_number = skuProduct.getText().toString();
        String value_sale_price = salePrice.getText().toString();
        String value_reseller_price = resellerPrice.getText().toString();
        String value_online_price = onlinePrice.getText().toString();
        String value_outlet = spinnerOutlet.getSelectedItem().toString();
        String value_main_category = spinnerMainCategory.getSelectedItem().toString();
        String value_category = spinnerCategory.getSelectedItem().toString();
        JSONObject obj = null;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list_ingredient.size(); i++) {
            obj = new JSONObject();
            try {
                String str = list_ingredient.get(i).getName();
                String[] splitStr = str.split(" - ");
                obj.put("id", list_ingredient.get(i).getId());
                obj.put("name", splitStr[0]);
                obj.put("price", list_ingredient.get(i).getPrice());
                obj.put("unit", list_ingredient.get(i).getUnit());
                obj.put("qty", list_ingredient.get(i).getQty());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(obj);
        }
        String ingredients = jsonArray.toString();

        if(!ingredients.equals("[]") && !value_product_name.equals("")
                && !value_sale_price.equals("") && !value_reseller_price.equals("")
                && !value_online_price.equals("") && !value_outlet.equals(getResources().getString(R.string.select_outlet))
                && !value_main_category.equals(getResources().getString(R.string.select_main_category))
                && !value_category.equals(getResources().getString(R.string.select_product_category))) {
            showLoading();
            String URL_PRODUCT = URI.API_CREATE_PRODUCT(session.getPathUrl());
            if(update_product) {
                URL_PRODUCT = URI.API_UPDATE_PRODUCT(session.getPathUrl());
            }
            StringRequest postRequest = new StringRequest(Request.Method.POST, URL_PRODUCT,
                    response -> {
                        Log.e("RESPONSE ", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                showSuccess(jsonObject.getString("message"));
                                list_ingredient.clear();
                                ingredientAdapter.notifyDataSetChanged();
                                productName.getText().clear();
                                skuProduct.getText().clear();
                                salePrice.getText().clear();
                                resellerPrice.getText().clear();
                                onlinePrice.getText().clear();
                                spinnerIngredient.setSelection(ingredientSpinnerList.indexOf(getResources().getString(R.string.select_ingredient)));
                                spinnerOutlet.setSelection(outletSpinnerList.indexOf(getResources().getString(R.string.select_outlet)));
                                spinnerMainCategory.setSelection(mainCategorySpinnerList.indexOf(getResources().getString(R.string.select_main_category)));
                                spinnerCategory.setSelection(categorySpinnerList.indexOf(getResources().getString(R.string.select_product_category)));
                            } else {
                                showError(jsonObject.getString("message"));
                            }

                            hideLoading();
                        } catch (JSONException e) {
                            showError("Terjadi kesalahan server");
                            hideLoading();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        hideLoading();

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            // Print Error!
                            Log.e("Error", jsonError);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    // the POST parameters:
                    if(update_product) {
                        params.put("product_id", String.valueOf(product_id));
                    }
                    params.put("user_id", session.getId());
                    params.put("product_name", value_product_name);
                    params.put("sku_number", value_sku_number);
                    params.put("sale_price", value_sale_price);
                    params.put("reseller_price", value_reseller_price);
                    params.put("online_price", value_online_price);
                    params.put("outlet", value_outlet);
                    params.put("main_category", value_main_category);
                    params.put("category", value_category);
                    params.put("ingredients", ingredients);
                    params.put("photo", encodedImageString);
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(postRequest);
        } else {
            showError("Lengkapi form diatas");
        }
    }

    private void getIngredients(String outlet) {
        ingredientSpinnerList.clear();
        request = new JsonArrayRequest(URI.API_INGREDIENTS_BY_OUTLET(session.getPathUrl())+"?outlet="+outlet, response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            ingredientSpinnerList.add(""+getResources().getString(R.string.select_ingredient)+"");
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    ingredientSpinnerList.add(jsonObject.getString("name"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerIngredient.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, ingredientSpinnerList));
            spinnerIngredient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String ingredient_name = spinnerIngredient.getSelectedItem().toString();
                    boolean exist = list_ingredient.stream().map(Ingredient::getName).anyMatch(ingredient_name::equals);
                    if(!exist && !ingredient_name.equals(getResources().getString(R.string.select_ingredient))) {
                        list_ingredient.add(new Ingredient(position, ingredient_name, 0, "unit", 0));
                    }
                    ingredientAdapter.notifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getOutlets() {
        outletSpinnerList.clear();
        request = new JsonArrayRequest(URI.API_OUTLETS_LIST(session.getPathUrl()), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            outletSpinnerList.add("" + getResources().getString(R.string.select_outlet) + "");
            for (int i = 0; i < response.length(); i++) {
                try {
                    jsonObject = response.getJSONObject(i);
                    if (!jsonObject.getString("outlet_name").equals(param_outlet_name)) {
                        outletSpinnerList.add(jsonObject.getString("outlet_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerOutlet.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, outletSpinnerList));
            spinnerOutlet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // your code here
                    String param_outlet = spinnerOutlet.getSelectedItem().toString();
                    if (!param_outlet.equals(getResources().getString(R.string.select_outlet))) {
                        getIngredients(param_outlet);
                    } else {
                        ingredientSpinnerList.add("" + getResources().getString(R.string.select_ingredient) + "");
                        spinnerIngredient.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, ingredientSpinnerList));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });
            if(product_id != 0) {
                getProductData(product_id);
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getMainCategory() {
        mainCategorySpinnerList.clear();
        Log.e("URL", URI.API_MAIN_CATEGORIES(session.getPathUrl())+session.getId());
        request = new JsonArrayRequest(URI.API_MAIN_CATEGORIES(session.getPathUrl())+session.getId(), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            mainCategorySpinnerList.add(""+getResources().getString(R.string.select_main_category)+"");
            if(!param_main_category_name.equals("")) {
                mainCategorySpinnerList.add(param_main_category_name);
            }
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    if(!jsonObject.getString("name").equals(param_main_category_name)) {
                        mainCategorySpinnerList.add(jsonObject.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerMainCategory.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, mainCategorySpinnerList));
            if(!param_main_category_name.equals("")) {
                spinnerMainCategory.setSelection(mainCategorySpinnerList.indexOf(param_main_category_name));
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getCategory() {
        categorySpinnerList.clear();
        Log.e("URL", URI.API_CATEGORIES(session.getPathUrl())+session.getId());
        request = new JsonArrayRequest(URI.API_CATEGORIES(session.getPathUrl())+session.getId(), response -> {
            JSONObject jsonObject;
            //Log.e("Response", response.toString());
            categorySpinnerList.add(""+getResources().getString(R.string.select_product_category)+"");
            if(!param_category_name.equals("")) {
                categorySpinnerList.add(param_category_name);
            }
            for (int i = 0; i < response.length(); i++){
                try {
                    jsonObject = response.getJSONObject(i);
                    if(!jsonObject.getString("category_name").equals(param_category_name)) {
                        categorySpinnerList.add(jsonObject.getString("category_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerCategory.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, categorySpinnerList));
            if(!param_category_name.equals("")) {
                spinnerCategory.setSelection(categorySpinnerList.indexOf(param_category_name));
            }
        }, error -> {
        });
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
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
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                photo.setVisibility(View.VISIBLE);
                photo.setImageBitmap(selectedImage);

                int imageWidth = selectedImage.getWidth();
                int imageHeight = selectedImage.getHeight();
                int newHeight = (imageHeight * 512)/imageWidth;

                Bitmap imageResized = Bitmap.createScaledBitmap(selectedImage, 512, newHeight, false);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                imageResized.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedImageString = Base64.encodeToString(byte_arr, 0);
                Log.e("IMAGEEnCode", encodedImageString);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
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

    @Override
    public void onRemoveItem(Ingredient item) {

    }
}