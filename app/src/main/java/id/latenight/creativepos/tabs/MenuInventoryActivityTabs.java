package id.latenight.creativepos.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import id.latenight.creativepos.R;
import id.latenight.creativepos.util.SessionManager;

public class MenuInventoryActivityTabs extends AppCompatActivity implements View.OnClickListener {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inventory);

        session = new SessionManager(this);

        RadioButton inventoryData = findViewById(R.id.inventory_data);
        RadioButton productData = findViewById(R.id.product_data);

        inventoryData.setOnClickListener(this);
        productData.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        if (v.getId() == R.id.inventory_data) {
            intent = new Intent(this, IngredientListActivityTabs.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.product_data) {
            intent = new Intent(this, ProductListActivityTabs.class);
            startActivity(intent);
        }
    }
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MenuActivityTabs.class);
        startActivity(intent);
    }
}
