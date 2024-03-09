package id.latenight.creativepos.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> implements Filterable {

    List<Order> order, filterList;
    Context ct;
    OrderAdapterListener listener;
    CustomFilter filter;

    public OrderAdapter(List<Order> order, Context ct, OrderAdapterListener listener) {
        this.order = order;
        this.filterList = order;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Order listData = order.get(position);

        String orderType = "";
        if(listData.getOrderType().equals("1")) {
            orderType = ct.getString(R.string.dine_in);
        } else if(listData.getOrderType().equals("2")) {
            orderType = ct.getString(R.string.take_away);
        } else if(listData.getOrderType().equals("3")) {
            orderType = ct.getString(R.string.delivery);
        } else {
            orderType = ct.getString(R.string.taken);
        }

        String orderStatus = "";
        if(listData.getOrderStatus().equals("1")) {
            orderStatus = ct.getString(R.string.new_order);
        } else if(listData.getOrderStatus().equals("2")) {
            orderStatus = ct.getString(R.string.not_yet_paid_off);
        } else {
            orderStatus = ct.getString(R.string.paid_off);
        }

        NumberFormat formatRupiah = NumberFormat.getInstance();
        double totalPayable = Double.parseDouble(listData.getTotalPayable());
        String rupiah = formatRupiah.format(totalPayable);

        holder.sale_no.setText(listData.getSaleNo());
        holder.customer.setText(listData.getCustomerName());
        holder.order_time.setText(listData.getOrderTime());
        holder.price.setText(ct.getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
        holder.order_type.setText(orderType);
        holder.order_status.setText(orderStatus);
        if(listData.getOrderStatus().equals("1")) {
            //holder.order_status.setTextColor(ct.getColor(R.color.text_red));
        } else if(listData.getOrderStatus().equals("2")) {
            holder.order_status.setTextColor(ct.getColor(R.color.text_orange));
        } else {
            holder.order_status.setTextColor(ct.getColor(R.color.text_green));
        }


        JSONArray jsonarray = null;
        String tableName = "";
        try {
            jsonarray = new JSONArray(listData.getTableName());
            String hold = "";
            for(int i=0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                 if(i != jsonarray.length() -1) {
                   hold = ",";
                 }
                tableName += jsonobject.getString("table_name") + hold;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.table_name.setText(ct.getResources().getString(R.string.table_name) + " " + tableName);
    }

    @Override
    public int getItemCount() {
        return order.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView sale_no, customer, order_time, price, order_type, table_name, order_status;
        public ViewHolder(View itemView) {
            super(itemView);
            sale_no = itemView.findViewById(R.id.sale_no);
            customer = itemView.findViewById(R.id.customer);
            order_time = itemView.findViewById(R.id.order_time);
            price = itemView.findViewById(R.id.price);
            order_type = itemView.findViewById(R.id.order_type);
            table_name = itemView.findViewById(R.id.table_name);
            order_status = itemView.findViewById(R.id.order_status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected berita in callback
                    listener.onOrderSelected(order.get(getAdapterPosition()), "Click");
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {
                    listener.onOrderSelected(order.get(getAdapterPosition()), "Long");
                    return false;
                }
            });
        }
    }

    public interface OrderAdapterListener {
        void onOrderSelected(Order item, String s);
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

        OrderAdapter adapter;
        List<Order> filterList;

        public CustomFilter(List<Order> filterList, OrderAdapter adapter)
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
                List<Order> filteredPlayers = new ArrayList<>();

                for (int i=0;i<filterList.size();i++)
                {
                    if(filterList.get(i).getSaleNo().toUpperCase().contains(constraint))
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

            adapter.order= (List<Order>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
