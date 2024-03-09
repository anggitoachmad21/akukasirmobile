package id.latenight.creativepos.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import id.latenight.creativepos.R;
import id.latenight.creativepos.tabs.IngredientListActivityTabs;
import id.latenight.creativepos.tabs.ProductListActivityTabs;
import id.latenight.creativepos.util.SessionManager;

public class MenuInventoryActivity extends AppCompatActivity implements View.OnClickListener {

    private SessionManager session;

    private boolean is_tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inventory);

        session = new SessionManager(this);

        RadioButton inventoryData = findViewById(R.id.inventory_data);
        RadioButton productData = findViewById(R.id.product_data);

        inventoryData.setOnClickListener(this);
        productData.setOnClickListener(this);
        if(session.getMobileType().equals("mobile"))
        {
            is_tabs = false;
        }
        else{
            is_tabs = true;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        if (v.getId() == R.id.inventory_data) {
            if(is_tabs == false) {
                intent = new Intent(this, IngredientListActivity.class);
                startActivity(intent);
            }
            else{
                intent = new Intent(this, IngredientListActivityTabs.class);
                startActivity(intent);
            }
        }
        if (v.getId() == R.id.product_data) {
            if(is_tabs == false) {
                intent = new Intent(this, ProductListActivity.class);
                startActivity(intent);
            }
            else{
                intent = new Intent(this, ProductListActivityTabs.class);
                startActivity(intent);
            }
        }
    }
}
