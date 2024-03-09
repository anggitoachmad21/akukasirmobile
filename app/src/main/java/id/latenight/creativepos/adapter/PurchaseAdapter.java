package id.latenight.creativepos.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import id.latenight.creativepos.model.Expense;
import id.latenight.creativepos.model.Purchase;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> implements Filterable {

    List<Purchase> order, filterList;
    Context ct;
    PurchaseAdapterListener listener;
    CustomFilter filter;

    public PurchaseAdapter(List<Purchase> order, Context ct, PurchaseAdapterListener listener) {
        this.order = order;
        this.filterList = order;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_list,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Purchase listData = order.get(position);

        NumberFormat formatRupiah = NumberFormat.getInstance();
        double totalPayable = Double.parseDouble(listData.getTotalPurchase());
        String rupiah = formatRupiah.format(totalPayable);

        holder.referenceNo.setText(listData.getReferenceNo());
        holder.date.setText(listData.getDate());
        holder.supplierName.setText(ct.getResources().getString(R.string.purchase_from) + " " + listData.getSupplierName());
        holder.totalPurchase.setText(ct.getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
        holder.notes.setText(listData.getNotes());
        holder.delete.setOnClickListener(view -> {
            // send selected berita in callback
            listener.onDelete(order.get(position), position);
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
        private TextView referenceNo, date, supplierName, totalPurchase, notes;
        private ImageButton delete;
        public ViewHolder(View itemView) {
            super(itemView);
            referenceNo = itemView.findViewById(R.id.reference_no);
            date = itemView.findViewById(R.id.date);
            supplierName = itemView.findViewById(R.id.supplier_name);
            totalPurchase = itemView.findViewById(R.id.total_purchase);
            notes = itemView.findViewById(R.id.notes);
            delete = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                listener.onOrderSelected(order.get(getAdapterPosition()));
            });
        }
    }

    public interface PurchaseAdapterListener {
        void onOrderSelected(Purchase item);
        void onDelete(Purchase item, int position);
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

        PurchaseAdapter adapter;
        List<Purchase> filterList;

        public CustomFilter(List<Purchase> filterList, PurchaseAdapter adapter)
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
                List<Purchase> filteredPlayers = new ArrayList<>();

                for (int i=0;i<filterList.size();i++)
                {
                    if(filterList.get(i).getSupplierName().toUpperCase().contains(constraint))
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

            adapter.order= (List<Purchase>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
