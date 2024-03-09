package id.latenight.creativepos.adapter;

import static id.latenight.creativepos.util.CapitalizeText.capitalizeText;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Conversion;

public class ConversionAdapter extends RecyclerView.Adapter<ConversionAdapter.ViewHolder> {

    private final List<Conversion> item;
    private final Context ct;
    AdapterListener listener;
    private Conversion listData;

    public ConversionAdapter(List<Conversion> item, Context ct, AdapterListener listener) {
        this.item = item;
        this.ct = ct;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.conversition_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        listData = item.get(position);

        Log.e("Unit Items", String.valueOf(listData.getList()));
        holder.unitList.clear();
        for(int i=0; i<listData.getList().size(); i++)
        {
            holder.unitList.add(listData.getList().get(i));
        }
        holder.unitConversion.setAdapter(new ArrayAdapter<>(ct.getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, holder.unitList));
        holder.unitConversion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String uni = holder.unitConversion.getSelectedItem().toString();
                listData.setUnit_text(uni);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        holder.ingredient.clear();
        for(int i=0; i<listData.getIngredientList().size(); i++)
        {
            holder.ingredient.add(listData.getIngredientList().get(i));
        }
        holder.ingredientList.setAdapter(new ArrayAdapter<>(ct.getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, holder.ingredient));
        holder.ingredientList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int positions, long id) {
                String uni = holder.ingredientList.getSelectedItem().toString();
//                String[] u = uni.split(" - ");
                item.get(position).setIngre(String.valueOf(positions));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        holder.number.setText((position + 1) + ".");

        String title_new = listData.getName().toLowerCase();
        String capitalize = capitalizeText(title_new);
        holder.name.setText(capitalize);
        holder.name.setEnabled(false);

        holder.unit_name.setText(listData.getUnit_name());
        holder.unit_name.setEnabled(false);

        if (holder.qty.getTag() instanceof TextWatcher) {
            holder.qty.removeTextChangedListener((TextWatcher) holder.qty.getTag());
        }
        if(listData.getQty() != 0.0) {
            holder.qty.setText(String.valueOf(listData.getQty()));
        } else {
            holder.qty.setText("");
        }

        TextWatcher watcher = new TextWatcher() {
            double product_quantity = 0;
            String angka;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")) {
                    angka = "1";
                } else {
                    angka = String.valueOf(s);
                }
                product_quantity = Double.parseDouble(angka);
                item.get(position).setQty(Double.parseDouble(angka));
            }
        };

        holder.qty.addTextChangedListener(watcher);
        holder.qty.setTag(watcher);

        if (holder.edit_qty_new.getTag() instanceof TextWatcher) {
            holder.edit_qty_new.removeTextChangedListener((TextWatcher) holder.qty.getTag());
        }
        if(!listData.getQty_new().equals("0.0")) {
            holder.edit_qty_new.setText(listData.getQty_new());
        } else {
            holder.edit_qty_new.setText("");
        }

        TextWatcher watchers = new TextWatcher() {
            double product_quantitys = 0;
            String angkas;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")) {
                    angkas = "1";
                } else {
                    angkas = String.valueOf(s);
                }
                item.get(position).setQty_new(angkas);
            }
        };
        holder.edit_qty_new.addTextChangedListener(watchers);
        holder.edit_qty_new.setTag(watchers);

        holder.qty.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_DONE){
                //Clear focus here from edittext
                holder.qty.clearFocus();
            }
            return false;
        });

        holder.btnDelete.setOnClickListener(view -> {
            item.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,item.size());
            listener.onRemoveItem(listData);
        });
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView number, name;
        private final EditText qty, unit_name, edit_qty_new;
        private final ImageView btnDelete;

        private final Spinner unitConversion, ingredientList;

        private final String unit_text;
        private String ingre;
        private final String satuan, qty_new;

        ArrayList<String> unitList, ingredient;
        ViewHolder(View itemView) {
            super(itemView);
            unitList         = new ArrayList<>();
            ingredient         = new ArrayList<>();
            unit_text = "";
            ingre  = "";
            satuan = "";
            number = itemView.findViewById(R.id.number);
            name = itemView.findViewById(R.id.name);
            qty = itemView.findViewById(R.id.qty);
            unit_name = itemView.findViewById(R.id.satuan);
            unitConversion = itemView.findViewById(R.id.unit_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            ingredientList = itemView.findViewById(R.id.ingredientList);
            edit_qty_new = itemView.findViewById(R.id.qty_new);
            qty_new = "";

        }

    }

    public interface AdapterListener {
        void onRemoveItem(Conversion item);
    }
}
