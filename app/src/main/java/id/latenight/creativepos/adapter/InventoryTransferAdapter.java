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
import id.latenight.creativepos.model.InventoryTransfer;

public class InventoryTransferAdapter extends RecyclerView.Adapter<InventoryTransferAdapter.ViewHolder> implements Filterable {

    List<InventoryTransfer> order, filterList;
    Context ct;
    InventoryTransferAdapterListener listener;
    CustomFilter filter;

    public InventoryTransferAdapter(List<InventoryTransfer> order, Context ct, InventoryTransferAdapterListener listener) {
        this.order = order;
        this.filterList = order;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_transfer_list,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final InventoryTransfer listData = order.get(position);

        NumberFormat formatRupiah = NumberFormat.getInstance();
        double totalPayable = Double.parseDouble(listData.getTotalQty());
        String rupiah = formatRupiah.format(totalPayable);

        holder.transferUniqueId.setText(listData.getTransferUniqueId());
        holder.date.setText(listData.getDate());
        holder.totalItem.setText(rupiah.replace(',', '.'));
        holder.notes.setText(listData.getNotes());
        holder.destinationOutlet.setText(listData.getDestinationOutlet());
    }

    @Override
    public int getItemCount() {
        return order.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView transferUniqueId, date, totalItem, notes, destinationOutlet;
        public ViewHolder(View itemView) {
            super(itemView);
            transferUniqueId = itemView.findViewById(R.id.transfer_unique_id);
            date = itemView.findViewById(R.id.date);
            destinationOutlet = itemView.findViewById(R.id.destination_outlet);
            totalItem = itemView.findViewById(R.id.total_item);
            notes = itemView.findViewById(R.id.notes);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                listener.onOrderSelected(order.get(getAdapterPosition()));
            });
        }
    }

    public interface InventoryTransferAdapterListener {
        void onOrderSelected(InventoryTransfer item);
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

        InventoryTransferAdapter adapter;
        List<InventoryTransfer> filterList;

        public CustomFilter(List<InventoryTransfer> filterList, InventoryTransferAdapter adapter)
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
                List<InventoryTransfer> filteredPlayers = new ArrayList<>();

                for (int i=0;i<filterList.size();i++)
                {
                    if(filterList.get(i).getTransferUniqueId().toUpperCase().contains(constraint))
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

            adapter.order= (List<InventoryTransfer>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
