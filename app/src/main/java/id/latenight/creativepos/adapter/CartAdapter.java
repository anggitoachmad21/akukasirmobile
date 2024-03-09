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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Cart;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<Cart> item;
    private Context ct;
    AdapterListener listener;
    private Cart listData;

    public CartAdapter(List<Cart> item, Context ct, AdapterListener listener) {
        this.item = item;
        this.ct = ct;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        listData = item.get(position);
        String title_new = listData.getName().toLowerCase();
        String capitalize = title_new.substring(0, 1).toUpperCase() + title_new.substring(1);
        holder.name.setText(Html.fromHtml(capitalize));
        //holder.name.setText(listData.getName());
        holder.price.setText(ct.getResources().getString(R.string.currency) +" "+ String.valueOf(listData.getOriPrice()*listData.getQty()));

        if (holder.qty.getTag() instanceof TextWatcher) {
            holder.qty.removeTextChangedListener((TextWatcher) holder.qty.getTag());
        }
        holder.qty.setText(String.valueOf(listData.getQty()));
        TextWatcher watcher = new TextWatcher() {
            int product_quantity = 1;
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
                product_quantity = Integer.parseInt(angka);
                listener.onUpdateCartQty(listData, product_quantity);
                listData.setQty(Integer.parseInt(angka));
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

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int product_quantity = 1;
                try {
                    product_quantity = Integer.parseInt(holder.qty.getText().toString());
                } catch(Exception e) {
                    product_quantity = 1;
                }
                item.remove(position);

                notifyItemRemoved(position);
                notifyItemRangeChanged(position,item.size());
                listener.onRemoveItem(listData);
            }
        });
    }

    private int grandTotal(int price){
        return price;
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name,price;
        private EditText qty;
        private ImageButton btnPlus, btnMinus, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            qty = itemView.findViewById(R.id.qty);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface AdapterListener {
        void onUpdateCartQty(Cart item, int product_quantity);
        void onRemoveItem(Cart item);
    }
}
