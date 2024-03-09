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
import id.latenight.creativepos.model.Ingredient;
import id.latenight.creativepos.model.Product;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.SessionManager;
import id.latenight.creativepos.util.URI;

public class DataProductAdapter extends RecyclerView.Adapter<DataProductAdapter.ViewHolder> implements Filterable {

    List<Product> product, filterList;
    Context ct;
    ImageAdapterListener listener;
    CustomFilter filter;
    private SessionManager sessionManager;
    private NumberFormat formatRupiah;
    private DatabaseHandler db;

    public DataProductAdapter(List<Product> product, Context ct, ImageAdapterListener listener) {
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
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.data_product_list,parent,false);
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
        String capitalize = capitalizeText(title_new);
        holder.title.setText(capitalize);

        String rupiah = formatRupiah.format(listData.getPrice());
        holder.price.setText(ct.getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
        if(listData.getLabel() != "null") {
            holder.category_label.setText("( " + listData.getCategory() + " - " + listData.getLabel() + " )");
        }
        else {
            holder.category_label.setText("( " + listData.getCategory() + " )");
        }

        holder.delete.setOnClickListener(view -> {
            listener.onRemoveItem(product, position);
        });

        holder.edit.setOnClickListener(view -> {
            listener.onItemSelected(listData);
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
        private final ImageView img, delete;
        private final TextView title, price, edit;
        private final CardView cartContainer;
        private final TextView category_label;
        public ViewHolder(View itemView) {
            super(itemView);
            cartContainer = itemView.findViewById(R.id.cartContainer);
            img = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title_view);
            category_label = itemView.findViewById(R.id.category_label);
            price = itemView.findViewById(R.id.price_view);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                //listener.onImageSelected(product.get(getAdapterPosition()));
            });
        }
    }

    public interface ImageAdapterListener {
        void onItemSelected(Product item);
        void onRemoveItem(List<Product> Product, int position);
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

        DataProductAdapter adapter;
        List<Product> filterList;

        public CustomFilter(List<Product> filterList, DataProductAdapter adapter)
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
