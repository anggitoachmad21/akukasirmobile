package id.latenight.creativepos.adapter;

import static id.latenight.creativepos.util.CapitalizeText.capitalizeText;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Ingredient;
import id.latenight.creativepos.model.Product;
import id.latenight.creativepos.util.DatabaseHandler;
import id.latenight.creativepos.util.SessionManager;

public class DataInventoryAdapter extends RecyclerView.Adapter<DataInventoryAdapter.ViewHolder> implements Filterable {

    List<Ingredient> ingredient, filterList;
    Context ct;
    ImageAdapterListener listener;
    CustomFilter filter;
    private SessionManager sessionManager;
    private NumberFormat formatRupiah;
    private DatabaseHandler db;

    public DataInventoryAdapter(List<Ingredient> ingredient, Context ct, ImageAdapterListener listener) {
        this.ingredient = ingredient;
        this.filterList = ingredient;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        sessionManager = new SessionManager(ct);
        db = new DatabaseHandler(ct);
        formatRupiah = NumberFormat.getInstance();
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.data_inventory_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Ingredient listData = ingredient.get(position);
        String title_new = listData.getName().toLowerCase();
        String capitalize = capitalizeText(title_new);
        holder.number.setText((position + 1) +".");
        holder.name.setText(capitalize);
        holder.qty.setText(formatRupiah.format(listData.getQty()).replace(',', '.') +" "+ listData.getUnit());

        holder.edit.setOnClickListener(view -> {
            listener.onItemSelected(listData);
        });

        holder.delete.setOnClickListener(view -> {
            listener.onRemoveItem(ingredient, position);
        });

        if(listData.getQty() < listData.getAlertQty()) {
            holder.number.setTextColor(ct.getResources().getColor(R.color.text_red));
            holder.name.setTextColor(ct.getResources().getColor(R.color.text_red));
            holder.qty.setTextColor(ct.getResources().getColor(R.color.text_red));
        } else {
            holder.number.setTextColor(ct.getResources().getColor(R.color.colorTextPrimary));
            holder.name.setTextColor(ct.getResources().getColor(R.color.colorTextPrimary));
            holder.qty.setTextColor(ct.getResources().getColor(R.color.colorTextPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return ingredient.size();
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView number, name, qty, edit;
        private final CardView cartContainer;
        private final ImageView delete;
        public ViewHolder(View itemView) {
            super(itemView);
            cartContainer = itemView.findViewById(R.id.cartContainer);
            number = itemView.findViewById(R.id.number);
            name = itemView.findViewById(R.id.name);
            qty = itemView.findViewById(R.id.qty);
            edit = itemView.findViewById(R.id.edit);

            delete = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                //listener.onImageSelected(ingredient.get(getAdapterPosition()));
            });
        }
    }

    public interface ImageAdapterListener {
        void onItemSelected(Ingredient item);

        void onRemoveItem(List<Ingredient> ingredient, int position);
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

        DataInventoryAdapter adapter;
        List<Ingredient> filterList;

        public CustomFilter(List<Ingredient> filterList, DataInventoryAdapter adapter)
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
                List<Ingredient> filteredPlayers = new ArrayList<>();

                for (int i=0;i<filterList.size();i++)
                {
                    if(filterList.get(i).getName().toUpperCase().contains(constraint))
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

            adapter.ingredient= (List<Ingredient>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
