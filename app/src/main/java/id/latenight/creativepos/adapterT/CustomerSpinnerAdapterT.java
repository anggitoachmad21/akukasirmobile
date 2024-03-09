package id.latenight.creativepos.adapterT;

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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.adapter.CustomerSpinnerAdapter;
import id.latenight.creativepos.model.Customer;

public class CustomerSpinnerAdapterT extends RecyclerView.Adapter<CustomerSpinnerAdapterT.ViewHolder> implements Filterable {

    List<Customer> order, filterList;
    Context ct;
    CustomerAdapterListener listener;
    CustomFilter filter;

    public CustomerSpinnerAdapterT(List<Customer> order, Context ct, CustomerAdapterListener listener) {
        this.order = order;
        this.filterList = order;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_spinner,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Customer listData = order.get(position);

        NumberFormat formatRupiah = NumberFormat.getInstance();
        String rupiah = formatRupiah.format(listData.getPayable());

        holder.name.setText(listData.getName());
        holder.phone.setText(listData.getPhone());
        holder.payable.setText(ct.getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
    }

    @Override
    public int getItemCount() {
        return order.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name, phone, payable, btnPay;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.handphone);
            payable = itemView.findViewById(R.id.payable);
            btnPay = itemView.findViewById(R.id.btn_pay);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected berita in callback
                    listener.onCustomerSelected(order.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface CustomerAdapterListener {
        void onCustomerSelected(Customer item);
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

        CustomerSpinnerAdapterT adapter;
        List<Customer> filterList;

        public CustomFilter(List<Customer> filterList, CustomerSpinnerAdapterT adapter)
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
                List<Customer> filteredPlayers = new ArrayList<>();

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

            adapter.order= (List<Customer>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
