package id.latenight.creativepos.adapterT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import id.latenight.creativepos.R;
import id.latenight.creativepos.modelT.CartT;

public class CartAdapterTabs extends RecyclerView.Adapter<CartAdapterTabs.ViewHolder> {

    private List<CartT> item;
    private Context ct;
    AdapterListener listener;
    private CartT listData;
    private NumberFormat formatRupiah;

    public CartAdapterTabs(List<CartT> item, Context ct, AdapterListener listener) {
        this.item = item;
        this.ct = ct;
        this.listener = listener;
        this.formatRupiah = NumberFormat.getInstance();
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
        holder.price.setText(ct.getResources().getString(R.string.currency) +" "+ formatRupiah.format((listData.getOriPrice()*listData.getQty())).replace(',', '.'));

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
                if (s.toString().equals("")) {
                    angka = "1";
                } else {
                    angka = String.valueOf(s);
                }
                product_quantity = Integer.parseInt(angka);
                item.get(position).setQty(product_quantity);
                int new_price = item.get(position).getOriPrice() * product_quantity;
                Log.e("TOTAL ITEM: ", item.get(position).getName() +" "+ item.get(position).getQty());
                listData.setPrice(new_price);
                listener.onUpdateCartQty(listData, product_quantity);
                String rupiah = formatRupiah.format(new_price).replace(',', '.');
                holder.price.setText(ct.getResources().getString(R.string.currency) + " " + rupiah);
            }
        };
        holder.qty.addTextChangedListener(watcher);
        holder.qty.setTag(watcher);

        holder.btnDelete.setOnClickListener(view -> {
            int product_quantity = 1;
            try {
                product_quantity = Integer.parseInt(holder.qty.getText().toString());
            } catch(Exception e) {
                product_quantity = 1;
            }
            item.remove(position);

            notifyItemRemoved(position);
            notifyItemRangeChanged(position,item.size());
            listener.onRemoveItem(item);
            holder.qty.clearFocus();
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
        void onUpdateCartQty(CartT item, int product_quantity);
        void onRemoveItem(List<CartT> item);
    }
}
