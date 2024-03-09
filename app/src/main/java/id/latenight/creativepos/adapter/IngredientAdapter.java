package id.latenight.creativepos.adapter;

import static id.latenight.creativepos.util.CapitalizeText.capitalizeText;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Ingredient;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    private final List<Ingredient> item;
    private final Context ct;
    AdapterListener listener;
    private Ingredient listData;

    public IngredientAdapter(List<Ingredient> item, Context ct, AdapterListener listener) {
        this.item = item;
        this.ct = ct;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        listData = item.get(position);

        holder.number.setText((position + 1) + ".");

        String title_new = listData.getName().toLowerCase();
        String capitalize = capitalizeText(title_new);
        holder.name.setText(capitalize);

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
        private final EditText qty;
        private final ImageView btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            name = itemView.findViewById(R.id.name);
            qty = itemView.findViewById(R.id.qty);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface AdapterListener {
        void onRemoveItem(Ingredient item);
    }
}
