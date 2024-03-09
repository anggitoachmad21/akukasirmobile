package id.latenight.creativepos.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Ingredient;

public class PurchaseIngredientAdapter extends RecyclerView.Adapter<PurchaseIngredientAdapter.ViewHolder> {

    private final List<Ingredient> item;
    private final Context ct;
    AdapterListener listener;
    private Ingredient listData;

    public PurchaseIngredientAdapter(List<Ingredient> item, Context ct, AdapterListener listener) {
        this.item = item;
        this.ct = ct;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_ingredient_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        listData = item.get(position);

        holder.number.setText((position + 1) + ".");

        String title_new = listData.getName().toLowerCase();
        String capitalize = title_new.substring(0, 1).toUpperCase() + title_new.substring(1);
        holder.name.setText(Html.fromHtml(capitalize));

        if (holder.qty.getTag() instanceof TextWatcher) {
            holder.qty.removeTextChangedListener((TextWatcher) holder.qty.getTag());
        }
        holder.qty.setText(String.valueOf(listData.getQty()));
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

        holder.qty.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_DONE){
                //Clear focus here from edittext
                holder.qty.clearFocus();
            }
            return false;
        });

        if (holder.unitPrice.getTag() instanceof TextWatcher) {
            holder.unitPrice.removeTextChangedListener((TextWatcher) holder.unitPrice.getTag());
        }
        holder.unitPrice.setText(String.valueOf(listData.getPrice()));
        TextWatcher watcherUnitPrice = new TextWatcher() {
            String unit_price;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")) {
                    unit_price = "0";
                } else {
                    unit_price = String.valueOf(s);
                }
                item.get(position).setPrice(Double.parseDouble(unit_price));
            }
        };
        holder.unitPrice.addTextChangedListener(watcherUnitPrice);
        holder.unitPrice.setTag(watcherUnitPrice);

        holder.unitPrice.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_DONE){
                //Clear focus here from edittext
                holder.unitPrice.clearFocus();
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
        private final EditText qty, unitPrice;
        private final ImageView btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            name = itemView.findViewById(R.id.name);
            qty = itemView.findViewById(R.id.qty);
            unitPrice = itemView.findViewById(R.id.unit_price);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface AdapterListener {
        void onRemoveItem(Ingredient item);
    }
}
