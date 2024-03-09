package id.latenight.creativepos.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Production;

public class ProductionAdapter extends RecyclerView.Adapter<ProductionAdapter.ViewHolder> implements Filterable {

    List<Production> order, filterList;
    Context ct;
    ProductionAdapterListener listener;
    CustomFilter filter;

    public ProductionAdapter(List<Production> order, Context ct, ProductionAdapterListener listener) {
        this.order = order;
        this.filterList = order;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.production_list,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Production listData = order.get(position);

        NumberFormat formatRupiah = NumberFormat.getInstance();
        double totalPayable = Double.parseDouble(listData.getPrediction());
        String rupiah = formatRupiah.format(totalPayable);

        holder.productName.setText(listData.getProductName() +" ("+ listData.getUnitName() +")");
        holder.date.setText(listData.getDate());
        holder.totalProduction.setText(rupiah.replace(',', '.'));
        holder.notes.setText(listData.getNotes());
//        holder.delete.setOnClickListener(view -> {
//            // send selected berita in callback
//            listener.onDelete(order.get(position), position);
//        });
        holder.btnProductionAgain.setOnClickListener(view -> {
            listener.onProductionAgain(order.get(position));
        });
    }

    public void removeItem(int position) {
        order.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,order.size());
    }

    @Override
    public int getItemCount() {
        return order.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView productName, date, totalProduction, notes;
        private ImageButton delete;
        private Button btnProductionAgain;
        public ViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            date = itemView.findViewById(R.id.date);
            totalProduction = itemView.findViewById(R.id.total_production);
            notes = itemView.findViewById(R.id.notes);
            delete = itemView.findViewById(R.id.delete);
            btnProductionAgain = itemView.findViewById(R.id.btn_production_again);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                listener.onOrderSelected(order.get(getAdapterPosition()));
            });
        }
    }

    public interface ProductionAdapterListener {
        void onOrderSelected(Production item);
        void onDelete(Production item, int position);
        void onProductionAgain(Production item);
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

        ProductionAdapter adapter;
        List<Production> filterList;

        public CustomFilter(List<Production> filterList, ProductionAdapter adapter)
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
                List<Production> filteredPlayers = new ArrayList<>();

                for (int i=0;i<filterList.size();i++)
                {
                    if(filterList.get(i).getProductName().toUpperCase().contains(constraint))
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

            adapter.order= (List<Production>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
