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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Product;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class ProductAdapterN extends RecyclerView.Adapter<ProductAdapterN.ViewHolder> implements Filterable {

    List<Product> product, filterList;
    Context ct;
    ImageAdapterListener listener;
    CustomFilter filter;
    private SessionManager sessionManager;
    private NumberFormat formatRupiah;
    private DatabaseHandler db;

    public ProductAdapterN(List<Product> product, Context ct, ImageAdapterListener listener) {
        this.product = product;
        this.filterList = product;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        sessionManager = new SessionManager(ct);
        db = new DatabaseHandler(ct);
        formatRupiah = NumberFormat.getInstance();
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Product listData = product.get(position);
        Glide
                .with(ct)
                .load(URI.PATH_IMAGE(sessionManager.getPathUrl())+listData.getImageurl())
                .centerCrop()
                .into(holder.img);

        String title_new = listData.getTitle().toLowerCase();
        String capitalize = title_new.substring(0, 1).toUpperCase() + title_new.substring(1);
        holder.title.setText(Html.fromHtml(capitalize));

        String rupiah = formatRupiah.format(listData.getPrice());
        holder.price.setText(ct.getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
        if(listData.getLabel() != "null") {
            holder.category_label.setText("( " + listData.getCategory() + " - " + listData.getLabel() + " )");
        }
        else {
            holder.category_label.setText("( " + listData.getCategory() + " )");
        }

        if(listData.getCartQty() > 0) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnMinus.setVisibility(View.VISIBLE);
            holder.qty.setVisibility(View.VISIBLE);
//            holder.qty.setText(String.valueOf(listData.getCartQty()));
        }
//
        if(listData.getCartQty() == 0){
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnMinus.setVisibility(View.GONE);
            holder.qty.setVisibility(View.GONE);
        }

        if (holder.qty.getTag() instanceof TextWatcher) {
            holder.qty.removeTextChangedListener((TextWatcher) holder.qty.getTag());
        }
        holder.qty.setText(String.valueOf(listData.getCartQty()));
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
                listener.onUpdateQty(listData, product_quantity);
                listData.setCartQty(Integer.parseInt(angka));
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

        holder.btnPlus.setOnClickListener(view -> {
            if(listData.getIngredientStock() > 0) {
                int product_quantity = 1;
                try {
                    product_quantity = Integer.parseInt(holder.qty.getText().toString());
                } catch (Exception e) {
                    product_quantity = 1;
                }
                product_quantity++;
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnMinus.setVisibility(View.VISIBLE);
                holder.qty.setVisibility(View.VISIBLE);

                holder.qty.setText(String.valueOf(product_quantity));
                db.addCart(listData.getId(), listData.getTitle(), listData.getPrice(), listData.getPrice(), product_quantity);
            }
            listener.onImageSelected(listData);
        });

        holder.btnMinus.setOnClickListener(view -> {
            int product_quantity = 0;
            try {
                product_quantity = Integer.parseInt(holder.qty.getText().toString());
            } catch(Exception e) {
                product_quantity = 0;
            }
            product_quantity--;
            holder.qty.setText(String.valueOf (product_quantity));
            if(product_quantity <= 0) {
                db.deleteCart(listData.getId());
                holder.btnMinus.setVisibility(View.GONE);
                holder.btnDelete.setVisibility(View.GONE);
                holder.qty.setVisibility(View.GONE);
                holder.qty.setText("0");
            } else {
                db.addCart(listData.getId(), listData.getTitle(), listData.getPrice(), listData.getPrice(), product_quantity);
            }
            listener.onRemove(listData);
        });

        holder.btnDelete.setOnClickListener(view -> {
            holder.qty.setText("0");
            db.deleteCart(listData.getId());
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnMinus.setVisibility(View.GONE);
            holder.qty.setVisibility(View.GONE);
            holder.qty.setText("0");
            listener.onRemove(listData);
        });

        holder.title.setOnClickListener(view -> {
            if(listData.getIngredientStock() > 0) {
                int product_quantity = 1;
                try {
                    product_quantity = Integer.parseInt(holder.qty.getText().toString());
                } catch (Exception e) {
                    product_quantity = 1;
                }
                product_quantity++;
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnMinus.setVisibility(View.VISIBLE);
                holder.qty.setVisibility(View.VISIBLE);

                holder.qty.setText(String.valueOf(product_quantity));
                db.addCart(listData.getId(), listData.getTitle(), listData.getPrice(), listData.getPrice(), product_quantity);
            }
            listener.onImageSelected(listData);
        });
        holder.price.setOnClickListener(view -> {
            if(listData.getIngredientStock() > 0) {
                int product_quantity = 1;
                try {
                    product_quantity = Integer.parseInt(holder.qty.getText().toString());
                } catch (Exception e) {
                    product_quantity = 1;
                }
                product_quantity++;
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnMinus.setVisibility(View.VISIBLE);
                holder.qty.setVisibility(View.VISIBLE);

                holder.qty.setText(String.valueOf(product_quantity));
                db.addCart(listData.getId(), listData.getTitle(), listData.getPrice(), listData.getPrice(), product_quantity);
            }
            listener.onImageSelected(listData);
        });
        holder.img.setOnClickListener(view -> {
            if(listData.getIngredientStock() > 0) {
                int product_quantity = 1;
                try {
                    product_quantity = Integer.parseInt(holder.qty.getText().toString());
                } catch (Exception e) {
                    product_quantity = 1;
                }
                product_quantity++;
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnMinus.setVisibility(View.VISIBLE);
                holder.qty.setVisibility(View.VISIBLE);

                holder.qty.setText(String.valueOf(product_quantity));
                db.addCart(listData.getId(), listData.getTitle(), listData.getPrice(), listData.getPrice(), product_quantity);
            }
            listener.onImageSelected(listData);
        });
    }

    @Override
    public int getItemCount() {
        return product.size();
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView img;
        private final TextView title;
        private final TextView category_label;
        private final TextView price;
        private final EditText qty;
        private final ImageButton btnPlus, btnMinus, btnDelete;
        private final CardView cartContainer;
        public ViewHolder(View itemView) {
            super(itemView);
            cartContainer = itemView.findViewById(R.id.cartContainer);
            img = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title_view);
            category_label = itemView.findViewById(R.id.category_label);
            price = itemView.findViewById(R.id.price_view);
            qty = itemView.findViewById(R.id.qty);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                //listener.onImageSelected(product.get(getAdapterPosition()));
            });
        }
    }

    public interface ImageAdapterListener {
        void onImageSelected(Product item);
        void onRemove(Product item);
        void onUpdateQty(Product item, int qty);
    }


    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter=new CustomFilter(filterList,this);
        }

        return filter;
    }

    public class CustomFilter extends Filter{

        ProductAdapterN adapter;
        List<Product> filterList;

        public CustomFilter(List<Product> filterList, ProductAdapterN adapter)
        {
            this.adapter = adapter;
            this.filterList = filterList;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results=new FilterResults();

            if(constraint != null && constraint.length() > 0)
            {
                constraint=constraint.toString().toUpperCase();
                List<Product> filteredPlayers = new ArrayList<>();

                for (int i=0;i<filterList.size();i++)
                {
                    if(filterList.get(i).getTitle().toUpperCase().contains(constraint))
                    {
                        filteredPlayers.add(filterList.get(i));
                    }
                }

                results.count = filteredPlayers.size();
                results.values = filteredPlayers;
            }else
            {
                results.count = filterList.size();
                results.values = filterList;

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            adapter.product= (List<Product>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
