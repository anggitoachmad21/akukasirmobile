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
import id.latenight.creativepos.model.InventoryReceive;

public class InventoryReceiveAdapter extends RecyclerView.Adapter<InventoryReceiveAdapter.ViewHolder> implements Filterable {

    List<InventoryReceive> order, filterList;
    Context ct;
    InventoryReceiveAdapterListener listener;
    CustomFilter filter;

    public InventoryReceiveAdapter(List<InventoryReceive> order, Context ct, InventoryReceiveAdapterListener listener) {
        this.order = order;
        this.filterList = order;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_receive_list,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final InventoryReceive listData = order.get(position);

        NumberFormat formatRupiah = NumberFormat.getInstance();
        double totalPayable = Double.parseDouble(listData.getTotalQty());
        String rupiah = formatRupiah.format(totalPayable);

        holder.transferUniqueId.setText(listData.getTransferUniqueId());
        holder.date.setText(listData.getDate());
        holder.totalItem.setText(rupiah.replace(',', '.'));
        holder.notes.setText(listData.getNotes());
    }

    @Override
    public int getItemCount() {
        return order.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView transferUniqueId, date, totalItem, notes;
        public ViewHolder(View itemView) {
            super(itemView);
            transferUniqueId = itemView.findViewById(R.id.transfer_unique_id);
            date = itemView.findViewById(R.id.date);
            totalItem = itemView.findViewById(R.id.total_item);
            notes = itemView.findViewById(R.id.notes);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                listener.onOrderSelected(order.get(getAdapterPosition()));
            });
        }
    }

    public interface InventoryReceiveAdapterListener {
        void onOrderSelected(InventoryReceive item);
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

        InventoryReceiveAdapter adapter;
        List<InventoryReceive> filterList;

        public CustomFilter(List<InventoryReceive> filterList, InventoryReceiveAdapter adapter)
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
                List<InventoryReceive> filteredPlayers = new ArrayList<>();

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

            adapter.order= (List<InventoryReceive>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
