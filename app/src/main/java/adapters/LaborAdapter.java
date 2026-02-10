package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;

import java.text.DecimalFormat;
import java.util.List;

import workers.works.invoice.LaborItem;

public class LaborAdapter extends RecyclerView.Adapter<LaborAdapter.LaborViewHolder> {
    
    private List<LaborItem> laborList;
    private OnItemActionListener listener;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    
    public interface OnItemActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }
    
    public LaborAdapter(List<LaborItem> laborList, OnItemActionListener listener) {
        this.laborList = laborList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public LaborViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_labor, parent, false);
        return new LaborViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LaborViewHolder holder, int position) {
        LaborItem item = laborList.get(position);
        
        holder.tvLaborType.setText(item.getLaborType());
        holder.tvHours.setText(String.valueOf(item.getHours()));
        holder.tvRate.setText("₱" + currencyFormat.format(item.getRate()));
        holder.tvAmount.setText("₱" + currencyFormat.format(item.getAmount()));
        
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(holder.getAdapterPosition());
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return laborList.size();
    }
    
    static class LaborViewHolder extends RecyclerView.ViewHolder {
        TextView tvLaborType, tvHours, tvRate, tvAmount;
        ImageButton btnEdit, btnDelete;
        
        public LaborViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLaborType = itemView.findViewById(R.id.tv_labor_type);
            tvHours = itemView.findViewById(R.id.tv_hours);
            tvRate = itemView.findViewById(R.id.tv_rate);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
