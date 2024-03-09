package id.latenight.creativepos.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.Expense;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> implements Filterable {

    List<Expense> expense, filterList;
    Context ct;
    ExpenseAdapter.ExpenseAdapterListener listener;
    CustomFilter filter;

    public ExpenseAdapter(List<Expense> expense, Context ct, ExpenseAdapterListener listener) {
        this.expense = expense;
        this.filterList = expense;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Expense listData = expense.get(position);

        NumberFormat formatRupiah = NumberFormat.getInstance();
        double totalAmount = Double.parseDouble(listData.getAmount());
        String rupiah = formatRupiah.format(totalAmount);

        holder.notes.setText(listData.getNote());
        holder.category.setText(listData.getCategory());
        holder.date.setText(listData.getDate());
        holder.amount.setText(ct.getResources().getString(R.string.currency) +" "+ rupiah.replace(',', '.'));
        holder.pic.setText(listData.getPic());

        holder.delete.setOnClickListener(view -> {
            // send selected berita in callback
            listener.onDelete(expense.get(position), position);
        });
    }

    public void removeItem(int position) {
        expense.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,expense.size());
    }

    @Override
    public int getItemCount() {
        return expense.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView notes, category, date, amount, pic;
        private ImageButton delete;
        public ViewHolder(View itemView) {
            super(itemView);
            notes = itemView.findViewById(R.id.notes);
            category = itemView.findViewById(R.id.category);
            date = itemView.findViewById(R.id.date);
            amount = itemView.findViewById(R.id.amount);
            pic = itemView.findViewById(R.id.pic);
            delete = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(view -> {
                // send selected berita in callback
                listener.onExpenseSelected(expense.get(getAdapterPosition()));
            });
        }
    }

    public interface ExpenseAdapterListener {
        void onExpenseSelected(Expense item);
        void onDelete(Expense item, int position);
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

        ExpenseAdapter adapter;
        List<Expense> filterList;

        public CustomFilter(List<Expense> filterList, ExpenseAdapter adapter)
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
                List<Expense> filteredPlayers = new ArrayList<>();

                for (int i=0;i<filterList.size();i++)
                {
                    if(filterList.get(i).getNote().toUpperCase().contains(constraint))
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

            adapter.expense= (List<Expense>) results.values;

            //REFRESH
            adapter.notifyDataSetChanged();
        }
    }
}
