package id.latenight.creativepos.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.latenight.creativepos.R;
import id.latenight.creativepos.model.PaymentMethod;

import static id.latenight.creativepos.util.CapitalizeText.capitalizeText;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {

    List<PaymentMethod> paymentMethod;
    Context ct;
    ImageAdapterListener listener;
    int row_index;

    public PaymentMethodAdapter(List<PaymentMethod> paymentMethod, Context ct, ImageAdapterListener listener) {
        this.paymentMethod = paymentMethod;
        this.ct = ct;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentMethod listData = paymentMethod.get(position);

        //Log.e("NAME", listData.getName());
//        if(listData.getName().equals("TUNAI")) {
//            holder.btn.setBackgroundResource(R.drawable.tunai);
//        } else if(listData.getName().equals("DEBIT")) {
//            holder.btn.setBackgroundResource(R.drawable.debit);
//        } else if(listData.getName().equals("GOPAY")) {
//            holder.btn.setBackgroundResource(R.drawable.gopay);
//        } else if(listData.getName().equals("OVO")) {
//            holder.btn.setBackgroundResource(R.drawable.ovo);
//        } else if(listData.getName().equals("DANA")) {
//            holder.btn.setBackgroundResource(R.drawable.dana);
//        } else if(listData.getName().equals("LINK AJA")) {
//            holder.btn.setBackgroundResource(R.drawable.linkaja);
//        } else {
//
//        }

        String title_new = listData.getName().toLowerCase();
        String capitalize = capitalizeText(title_new);
        holder.btn.setText(capitalize);

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onImageSelected(paymentMethod.get(position));

                row_index = position;
                notifyDataSetChanged();
            }
        });

        if(row_index==position){
            holder.btn.setBackgroundResource(R.drawable.btn_filled);
            holder.btn.setTextColor(Color.WHITE);
        }
        else
        {
            holder.btn.setBackgroundResource(R.drawable.borderless);
            holder.btn.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return paymentMethod.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private Button btn;
        public ViewHolder(View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.btn_payment);

            itemView.setOnClickListener(view -> {
                listener.onImageSelected(paymentMethod.get(getAdapterPosition()));
            });
        }
    }

    public interface ImageAdapterListener {
        void onImageSelected(PaymentMethod item);
    }
}
