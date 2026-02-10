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

import workers.works.invoice.MiscItem;

public class MiscAdapter extends RecyclerView.Adapter<MiscAdapter.MiscViewHolder> {
    
    private List<MiscItem> miscList;
    private OnItemActionListener listener;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    
    public interface OnItemActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }
    
    public MiscAdapter(List<MiscItem> miscList, OnItemActionListener listener) {
        this.miscList = miscList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MiscViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_misc, parent, false);
        return new MiscViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MiscViewHolder holder, int position) {
        MiscItem item = miscList.get(position);
        
        holder.tvDescription.setText(item.getDescription());
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
        return miscList.size();
    }
    
    static class MiscViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount;
        ImageButton btnEdit, btnDelete;
        
        public MiscViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
