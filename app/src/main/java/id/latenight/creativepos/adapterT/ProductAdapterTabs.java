package id.latenight.creativepos.adapterT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Product;
import id.latenight.creativepos.modelT.CartT;
import id.latenight.creativepos.modelT.ProductT;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class ProductAdapterTabs extends RecyclerView.Adapter<ProductAdapterTabs.ViewHolder> implements Filterable {

    List<ProductT> product, filterList;
    Context ct;
    ImageAdapterListener listener;
    CustomFilter filter;
    private SessionManager sessionManager;
    private NumberFormat formatRupiah;
    private DatabaseHandler db;

    public ProductAdapterTabs(List<ProductT> product, Context ct, ImageAdapterListener listener) {
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
        final ProductT listData = product.get(position);
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
        private final TextView price;
        private final EditText qty;
        private final ImageButton btnPlus, btnMinus, btnDelete;
        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title_view);
            price = itemView.findViewById(R.id.price_view);
            qty = itemView.findViewById(R.id.qty);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                //listener.onImageSelected(product.get(getAdapterPosition()));
                if(product.get(getAdapterPosition()).getIngredientStock() > 0) {
                    int product_quantity = 1;
                    db.addCart(product.get(getAdapterPosition()).getId(), product.get(getAdapterPosition()).getTitle(), product.get(getAdapterPosition()).getPrice(), product.get(getAdapterPosition()).getPrice(), product_quantity);
                }
                listener.onImageSelected(product.get(getAdapterPosition()));
            });
        }
    }

    public interface ImageAdapterListener {
        void onImageSelected(Product item);

        void onRemove(Product item);

        void onUpdateQty(Product item, int qty);

        void onImageSelected(ProductT item);

        @SuppressLint("SetTextI18n")
        void onUpdateCartQty(CartT item, int product_quantity);

        @SuppressLint("SetTextI18n")
        void onRemoveItem(List<CartT> item);
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

        ProductAdapterTabs adapter;
        List<ProductT> filterList;

        public CustomFilter(List<ProductT> filterList, ProductAdapterTabs adapter)
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
                List<ProductT> filteredPlayers = new ArrayList<>();

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

            adapter.product= (List<ProductT>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
